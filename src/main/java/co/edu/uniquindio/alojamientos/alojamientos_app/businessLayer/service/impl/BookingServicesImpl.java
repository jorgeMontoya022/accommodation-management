package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CancelBookingRequestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BookingService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.AccommodationDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.BookingDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.BookingMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServicesImpl implements BookingService {
    private final BookingDao bookingDao;
    private final AccommodationDao accommodationDao;
    private final GuestService guestService;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    @Override
    public ResponseBookingDto createBooking(RequestBookingDto requestBookingDto, Long authenticatedGuestId) {
        log.info("Creando reserva para guest ID: {}", authenticatedGuestId);

        // 1. Validar que el huésped existe y está activo
        GuestEntity guest = guestService.getGuestEntityById(authenticatedGuestId);

        if (!guest.isActive()) {
            throw new IllegalStateException("El huésped no está activo");
        }

        // 2. Validar que el alojamiento existe
        AccommodationEntity accommodation = accommodationDao.findById(requestBookingDto.getIdAccommodation())
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        // 3. Validar que el alojamiento está activo
        if (accommodation.getStatusAccommodation().name().equals("DELETED")) {
            throw new IllegalStateException("El alojamiento no está disponible");
        }

        // 4. Validar fechas
        validateBookingDates(requestBookingDto);

        // 5. Validar que hay disponibilidad
        if (!isAccommodationAvailable(
                requestBookingDto.getIdAccommodation(),
                requestBookingDto.getDateCheckin().toLocalDate(),
                requestBookingDto.getDateCheckout().toLocalDate())) {
            throw new IllegalStateException("El alojamiento no está disponible para estas fechas");
        }

        // 6. Validar capacidad
        if (requestBookingDto.getQuantityPeople() > accommodation.getMaximumCapacity()) {
            throw new IllegalArgumentException(
                    "La cantidad de personas (" + requestBookingDto.getQuantityPeople() +
                            ") excede la capacidad máxima (" + accommodation.getMaximumCapacity() + ")"
            );
        }

        // 7. Calcular valor total
        double totalValue = calculateTotalValue(accommodation, requestBookingDto);

        // 8. Crear la reserva
        BookingEntity booking = bookingMapper.bookingDtoToBookingEntity(requestBookingDto);
        booking.setGuestEntity(guest);
        booking.setAccommodationAssociated(accommodation);
        booking.setTotalValue(totalValue);
        booking.setStatusReservation(StatusReservation.PENDING);

        BookingEntity saved = bookingDao.saveEntity(booking);

        log.info("Reserva creada exitosamente con ID: {} - Estado: PENDING", saved.getId());

        // 9. Enviar emails
        sendBookingCreationEmails(saved);

        return bookingMapper.bookingEntityToBookingDto(saved);
    }

    /**
     * Valida que las fechas de la reserva sean válidas
     */
    private void validateBookingDates(RequestBookingDto requestBookingDto) {
        LocalDateTime checkin = requestBookingDto.getDateCheckin();
        LocalDateTime checkout = requestBookingDto.getDateCheckout();

        // Validar que check-in sea en el futuro
        if (checkin.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de check-in debe ser en el futuro");
        }

        // Validar que check-out sea después de check-in
        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("La fecha de check-out debe ser después de check-in");
        }

        // Validar que la diferencia sea al menos 1 día
        if (checkin.plusDays(1).isAfter(checkout)) {
            throw new IllegalArgumentException("La reserva debe ser de al menos 1 día");
        }

        // Validar máximo de días permitidos (ej: 365 días)
        if (checkin.plusDays(365).isBefore(checkout)) {
            throw new IllegalArgumentException("La reserva no puede exceder 365 días");
        }
    }

    /**
     * Calcula el valor total de la reserva
     */
    private double calculateTotalValue(AccommodationEntity accommodation, RequestBookingDto requestBookingDto) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                requestBookingDto.getDateCheckin().toLocalDate(),
                requestBookingDto.getDateCheckout().toLocalDate()
        );

        if (nights <= 0) {
            nights = 1;
        }

        return accommodation.getPriceNight() * nights;
    }

    /**
     * Calcula el valor total de la reserva para actualizaciones
     */
    private double calculateTotalValueUpdate(AccommodationEntity accommodation, UpdateBookingDto updateBookingDto) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                updateBookingDto.getDateCheckin().toLocalDate(),
                updateBookingDto.getDateCheckout().toLocalDate()
        );

        if (nights <= 0) {
            nights = 1;
        }

        return accommodation.getPriceNight() * nights;
    }

    /**
     * Envía emails de confirmación a huésped y anfitrión
     */
    private void sendBookingCreationEmails(BookingEntity booking) {
        try {
            // Email al huésped
            SendEmailDto guestEmail = SendEmailDto.builder()
                    .recipient(booking.getGuestEntity().getEmail())
                    .subject("Tu reserva está pendiente de confirmación")
                    .body(buildGuestBookingEmail(booking))
                    .build();
            emailService.sendMail(guestEmail);
            log.info("Email enviado al huésped: {}", booking.getGuestEntity().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email al huésped", e);
        }

        try {
            // Email al anfitrión
            SendEmailDto hostEmail = SendEmailDto.builder()
                    .recipient(booking.getAccommodationAssociated().getHostEntity().getEmail())
                    .subject("Nueva reserva pendiente de confirmación")
                    .body(buildHostBookingEmail(booking))
                    .build();
            emailService.sendMail(hostEmail);
            log.info("Email enviado al anfitrión: {}", booking.getAccommodationAssociated().getHostEntity().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email al anfitrión", e);
        }
    }
    /**
     * Construye el correo de confirmación de reserva para el huésped
     * con todos los detalles importantes.
     */
    private String buildGuestConfirmedBookingEmail(BookingEntity booking) {
        return "¡Hola " + booking.getGuestEntity().getName() + "!\n\n" +
                "¡Tu reserva ha sido CONFIRMADA por el anfitrión!\n\n" +
                "Detalles de tu reserva:\n" +
                "- Alojamiento: " + booking.getAccommodationAssociated().getQualification() + "\n" +
                "- Check-in: " + booking.getDateCheckin() + "\n" +
                "- Check-out: " + booking.getDateCheckout() + "\n" +
                "- Personas: " + booking.getQuantityPeople() + "\n" +
                "- Total: $" + String.format("%.2f", booking.getTotalValue()) + "\n" +
                "- Estado actual: " + booking.getStatusReservation() + "\n\n" +
                "Te esperamos. Recuerda revisar las normas del alojamiento antes de tu llegada.\n\n" +
                "Saludos,\n" +
                "El equipo de Alojamientos Úniquindío";
    }


    private String buildGuestBookingEmail(BookingEntity booking) {
        return "¡Hola " + booking.getGuestEntity().getName() + "!\n\n" +
                "Tu reserva está pendiente de confirmación del anfitrión.\n\n" +
                "Detalles:\n" +
                "- Alojamiento: " + booking.getAccommodationAssociated().getQualification() + "\n" +
                "- Check-in: " + booking.getDateCheckin() + "\n" +
                "- Check-out: " + booking.getDateCheckout() + "\n" +
                "- Total: $" + String.format("%.2f", booking.getTotalValue()) + "\n\n" +
                "Te notificaremos cuando el anfitrión confirme tu reserva.\n\n" +
                "Saludos,\n" +
                "El equipo de Alojamientos Úniquindío";
    }

    private String buildHostBookingEmail(BookingEntity booking) {
        return "¡Hola " + booking.getAccommodationAssociated().getHostEntity().getName() + "!\n\n" +
                "Tienes una nueva reserva pendiente de confirmación.\n\n" +
                "Detalles:\n" +
                "- Huésped: " + booking.getGuestEntity().getName() + "\n" +
                "- Alojamiento: " + booking.getAccommodationAssociated().getQualification() + "\n" +
                "- Check-in: " + booking.getDateCheckin() + "\n" +
                "- Check-out: " + booking.getDateCheckout() + "\n" +
                "- Personas: " + booking.getQuantityPeople() + "\n" +
                "- Total: $" + String.format("%.2f", booking.getTotalValue()) + "\n\n" +
                "Confirma o rechaza la reserva en tu panel de anfitrión.\n\n" +
                "Saludos,\n" +
                "El equipo de Alojamientos Úniquindío";
    }

    @Override
    public ResponseBookingDto updateBooking(Long id, UpdateBookingDto updateBookingDto, Long authenticatedGuestId) {
        log.info("Actualizando reserva ID: {} para huésped: {}", id, authenticatedGuestId);

        BookingEntity booking = bookingDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar que el huésped autenticado es el dueño
        if (!booking.getGuestEntity().getId().equals(authenticatedGuestId)) {
            throw new RuntimeException("No tienes permiso para actualizar esta reserva");
        }

        // Solo se puede actualizar si está en estado PENDING
        if (booking.getStatusReservation() != StatusReservation.PENDING) {
            throw new IllegalStateException(
                    "Solo reservas PENDING pueden ser actualizadas. Estado actual: " + booking.getStatusReservation()
            );
        }

        // Validar nuevas fechas
        validateBookingDatesUpdate(updateBookingDto);

        // Validar disponibilidad con las nuevas fechas
        if (!isAccommodationAvailable(
                booking.getAccommodationAssociated().getId(),
                updateBookingDto.getDateCheckin().toLocalDate(),
                updateBookingDto.getDateCheckout().toLocalDate())) {
            throw new IllegalStateException("El alojamiento no está disponible para estas nuevas fechas");
        }

        // Validar capacidad
        if (updateBookingDto.getQuantityPeople() > booking.getAccommodationAssociated().getMaximumCapacity()) {
            throw new IllegalArgumentException("La cantidad de personas excede la capacidad máxima");
        }

        // Actualizar campos
        booking.setDateCheckin(updateBookingDto.getDateCheckin());
        booking.setDateCheckout(updateBookingDto.getDateCheckout());
        booking.setQuantityPeople(updateBookingDto.getQuantityPeople());
        booking.setTotalValue(calculateTotalValueUpdate(booking.getAccommodationAssociated(), updateBookingDto));

        BookingEntity updated = bookingDao.updateEntity(booking);

        log.info("Reserva actualizada. ID: {}", id);

        return bookingMapper.bookingEntityToBookingDto(updated);
    }

    /**
     * Valida que las fechas de actualización sean válidas
     */
    private void validateBookingDatesUpdate(UpdateBookingDto updateBookingDto) {
        LocalDateTime checkin = updateBookingDto.getDateCheckin();
        LocalDateTime checkout = updateBookingDto.getDateCheckout();

        // Validar que check-in sea en el futuro
        if (checkin.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de check-in debe ser en el futuro");
        }

        // Validar que check-out sea después de check-in
        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("La fecha de check-out debe ser después de check-in");
        }

        // Validar que la diferencia sea al menos 1 día
        if (checkin.plusDays(1).isAfter(checkout)) {
            throw new IllegalArgumentException("La reserva debe ser de al menos 1 día");
        }

        // Validar máximo de días permitidos (ej: 365 días)
        if (checkin.plusDays(365).isBefore(checkout)) {
            throw new IllegalArgumentException("La reserva no puede exceder 365 días");
        }
    }

    @Override
    public ResponseBookingDto confirmBooking(Long bookingId, Long authenticatedHostId) {
        log.info("Anfitrión {} confirmando reserva ID: {}", authenticatedHostId, bookingId);

        BookingEntity booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar permisos
        if (!booking.getAccommodationAssociated().getHostEntity().getId().equals(authenticatedHostId)) {
            throw new RuntimeException("No tienes permiso para confirmar esta reserva");
        }

        // Validar estado
        if (booking.getStatusReservation() != StatusReservation.PENDING) {
            throw new IllegalStateException(
                    "Solo reservas PENDING pueden ser confirmadas. Estado actual: " + booking.getStatusReservation()
            );
        }

        // Cambiar a PAID
        booking.setStatusReservation(StatusReservation.PAID);
        BookingEntity updated = bookingDao.updateEntity(booking);

        log.info("Reserva confirmada. ID: {} - Nuevo estado: PAID", bookingId);

        // Email de confirmación con detalles al huésped
        try {
            SendEmailDto email = SendEmailDto.builder()
                    .recipient(booking.getGuestEntity().getEmail())
                    .subject("¡Tu reserva ha sido confirmada!")
                    .body(buildGuestConfirmedBookingEmail(booking))
                    .build();
            emailService.sendMail(email);
            log.info("Email de confirmación enviado al huésped: {}", booking.getGuestEntity().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación al huésped", e);
        }


        return bookingMapper.bookingEntityToBookingDto(updated);
    }

    @Override
    public ResponseBookingDto rejectBooking(Long bookingId, String reason, Long authenticatedHostId) {
        log.info("Anfitrión {} rechazando reserva ID: {}", authenticatedHostId, bookingId);

        BookingEntity booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!booking.getAccommodationAssociated().getHostEntity().getId().equals(authenticatedHostId)) {
            throw new RuntimeException("No tienes permiso para rechazar esta reserva");
        }

        if (booking.getStatusReservation() != StatusReservation.PENDING) {
            throw new IllegalStateException(
                    "Solo reservas PENDING pueden ser rechazadas. Estado actual: " + booking.getStatusReservation()
            );
        }

        booking.setStatusReservation(StatusReservation.CANCELED);
        booking.setReasonCancellation(reason);
        booking.setDateCancellation(LocalDateTime.now());
        BookingEntity updated = bookingDao.updateEntity(booking);

        log.info("Reserva rechazada. ID: {}", bookingId);

        try {
            SendEmailDto email = SendEmailDto.builder()
                    .recipient(booking.getGuestEntity().getEmail())
                    .subject("Tu reserva ha sido rechazada")
                    .body("Lamentablemente, tu reserva ha sido rechazada.\n\nRazón: " + reason)
                    .build();
            emailService.sendMail(email);
        } catch (Exception e) {
            log.error("Error al enviar email", e);
        }

        return bookingMapper.bookingEntityToBookingDto(updated);
    }

    @Override
    public ResponseBookingDto cancelBooking(Long bookingId, CancelBookingRequestDto cancelBookingRequestDto, Long authenticatedGuestId) {
        log.info("Huésped {} cancelando reserva ID: {}", authenticatedGuestId, bookingId);

        BookingEntity booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!booking.getGuestEntity().getId().equals(authenticatedGuestId)) {
            throw new RuntimeException("No tienes permiso para cancelar esta reserva");
        }

        if (booking.getStatusReservation() == StatusReservation.COMPLETED ||
                booking.getStatusReservation() == StatusReservation.CANCELED) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva en estado: " + booking.getStatusReservation()
            );
        }

        booking.setStatusReservation(StatusReservation.CANCELED);
        booking.setReasonCancellation(cancelBookingRequestDto.getReasonCancellation());
        booking.setDateCancellation(LocalDateTime.now());
        BookingEntity updated = bookingDao.updateEntity(booking);

        log.info("Reserva cancelada. ID: {}", bookingId);

        try {
            SendEmailDto email = SendEmailDto.builder()
                    .recipient(booking.getAccommodationAssociated().getHostEntity().getEmail())
                    .subject("Una reserva ha sido cancelada")
                    .body("Hola " + booking.getAccommodationAssociated().getHostEntity().getName() + ",\n\n" +
                            "El huésped " + booking.getGuestEntity().getName() + " ha cancelado su reserva en tu alojamiento.\n\n" +
                            "Detalles de la reserva cancelada:\n" +
                            "- Alojamiento: " + booking.getAccommodationAssociated().getQualification() + "\n" +
                            "- Check-in: " + booking.getDateCheckin() + "\n" +
                            "- Check-out: " + booking.getDateCheckout() + "\n" +
                            "- Personas: " + booking.getQuantityPeople() + "\n" +
                            "- Total: $" + String.format("%.2f", booking.getTotalValue()) + "\n\n" +
                            "Razón indicada por el huésped:\n" +
                            cancelBookingRequestDto.getReasonCancellation() + "\n\n" +
                            "Te recomendamos revisar tu calendario y la disponibilidad del alojamiento.\n\n" +
                            "Saludos,\n" +
                            "El equipo de Alojamientos Úniquindío")
                    .build();
            emailService.sendMail(email);
        } catch (Exception e) {
            log.error("Error al enviar email", e);
        }


        return bookingMapper.bookingEntityToBookingDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccommodationAvailable(Long accommodationId, LocalDate startDate, LocalDate endDate) {

        // Estados que bloquean el calendario
        List<StatusReservation> activeStatuses = List.of(
                StatusReservation.PENDING,
                StatusReservation.CONFIRMED,
                StatusReservation.PAID
        );

        List<BookingEntity> overlappingBookings = bookingDao.findOverlappingBookings(
                accommodationId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59),
                activeStatuses
        );

        return overlappingBookings.isEmpty();
    }


    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> getUnavailableDates(Long accommodationId) {

        List<StatusReservation> activeStatuses = List.of(
                StatusReservation.PENDING,
                StatusReservation.CONFIRMED,
                StatusReservation.PAID
        );

        List<BookingEntity> bookings = bookingDao.findOverlappingBookings(
                accommodationId,
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now().plusYears(1),
                activeStatuses
        );

        List<LocalDate> unavailableDates = new ArrayList<>();

        for (BookingEntity booking : bookings) {
            LocalDate start = booking.getDateCheckin().toLocalDate();
            LocalDate end = booking.getDateCheckout().toLocalDate();

            while (!start.isAfter(end)) {
                unavailableDates.add(start);
                start = start.plusDays(1);
            }
        }

        return unavailableDates;
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseBookingDto getBookingById(Long id) {
        log.info("Obteniendo reserva ID: {}", id);

        BookingEntity booking = bookingDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        return bookingMapper.bookingEntityToBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseBookingDto> getBookingsByGuest(Long guestId) {
        log.info("Obteniendo reservas del huésped ID: {}", guestId);

        guestService.getGuestEntityById(guestId);

        List<BookingEntity> bookings = bookingDao.findByGuestId(guestId);

        return bookingMapper.getBookingsDto(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseBookingDto> getBookingsByAccommodation(Long accommodationId) {
        log.info("Obteniendo reservas del alojamiento ID: {}", accommodationId);

        accommodationDao.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        List<BookingEntity> bookings = bookingDao.findByAccommodationId(accommodationId);

        return bookingMapper.getBookingsDto(bookings);
    }

    @Override
    public ResponseBookingDto completeBooking(Long bookingId) {
        log.info("Completando reserva ID: {}", bookingId);

        BookingEntity booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (booking.getStatusReservation() != StatusReservation.PAID) {
            throw new IllegalStateException(
                    "Solo reservas PAID pueden ser completadas. Estado actual: " + booking.getStatusReservation()
            );
        }

        booking.setStatusReservation(StatusReservation.COMPLETED);
        BookingEntity updated = bookingDao.updateEntity(booking);

        log.info("Reserva completada. ID: {}", bookingId);

        try {
            SendEmailDto email = SendEmailDto.builder()
                    .recipient(booking.getGuestEntity().getEmail())
                    .subject("¡Tu estancia ha finalizado! Cuéntanos tu experiencia")
                    .body("Tu estancia en " + booking.getAccommodationAssociated().getQualification() +
                            " ha finalizado.\n\n" +
                            "Ahora puedes dejar un comentario/review sobre tu experiencia.")
                    .build();
            emailService.sendMail(email);
        } catch (Exception e) {
            log.error("Error al enviar email", e);
        }

        return bookingMapper.bookingEntityToBookingDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDates(Long accommodationId, LocalDate startDate, LocalDate endDate) {
        log.info("Obteniendo fechas disponibles del alojamiento ID: {} entre {} y {}",
                accommodationId, startDate, endDate);

        accommodationDao.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        List<LocalDate> unavailableDates = getUnavailableDates(accommodationId);

        List<LocalDate> availableDates = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!unavailableDates.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        return availableDates;
    }
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void changeState(Long bookingId, String newState) {
        log.info("Cambiando estado de reserva {} a {}", bookingId, newState);

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id " + bookingId));

        StatusReservation status;
        try {
            status = StatusReservation.valueOf(newState);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de reserva inválido: " + newState);
        }

        booking.setStatusReservation(status);
        bookingRepository.save(booking);

        log.info("Estado de reserva {} actualizado a {}", bookingId, status);
    }


}