package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CancelBookingRequestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateBookingDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /**
     * Crea una nueva reserva
     *
     * @param requestBookingDto    DTO con datos de la reserva
     * @param authenticatedGuestId ID del huésped autenticado
     * @return ResponseBookingDto con los datos de la reserva creada
     */
    ResponseBookingDto createBooking(RequestBookingDto requestBookingDto, Long authenticatedGuestId);

    /**
     * Actualiza los detalles de una reserva PENDING
     *
     * @param id                   ID de la reserva
     * @param updateBookingDto    DTO con los nuevos datos
     * @param authenticatedGuestId ID del huésped autenticado
     * @return ResponseBookingDto con los datos actualizados
     */
    ResponseBookingDto updateBooking(Long id, UpdateBookingDto updateBookingDto, Long authenticatedGuestId)

    /**
     * Obtiene una reserva por su ID
     *
     * @param id ID de la reserva
     * @return ResponseBookingDto con los datos de la reserva
     */
    ResponseBookingDto getBookingById(Long id);

    /**
     * Lista todas las reservas de un huésped
     *
     * @param guestId ID del huésped
     * @return Lista de ResponseBookingDto
     */
    List<ResponseBookingDto> getBookingsByGuest(Long guestId);

    /**
     * Lista todas las reservas de un alojamiento
     *
     * @param accommodationId ID del alojamiento
     * @return Lista de ResponseBookingDto
     */
    List<ResponseBookingDto> getBookingsByAccommodation(Long accommodationId);

    /**
     * El ANFITRIÓN confirma una reserva (PENDING → PAID)
     *
     * @param bookingId           ID de la reserva
     * @param authenticatedHostId ID del anfitrión autenticado
     * @return ResponseBookingDto con el nuevo estado
     */
    ResponseBookingDto confirmBooking(Long bookingId, Long authenticatedHostId);

    /**
     * El ANFITRIÓN rechaza una reserva (PENDING → CANCELED)
     *
     * @param bookingId           ID de la reserva
     * @param reason              Razón del rechazo
     * @param authenticatedHostId ID del anfitrión autenticado
     * @return ResponseBookingDto con el nuevo estado
     */
    ResponseBookingDto rejectBooking(Long bookingId, String reason, Long authenticatedHostId);

    /**
     * El HUÉSPED cancela su reserva (PENDING/PAID → CANCELED)
     *
     * @param bookingId            ID de la reserva
     * @param cancelBookingRequestDto               Razón de la cancelación
     * @param authenticatedGuestId ID del huésped autenticado
     * @return ResponseBookingDto con el nuevo estado
     */
    ResponseBookingDto cancelBooking(Long bookingId, CancelBookingRequestDto cancelBookingRequestDto, Long authenticatedGuestId);

    /**
     * Marca una reserva como completada (PAID → COMPLETED)
     * Se ejecuta automáticamente después del checkout
     *
     * @param bookingId ID de la reserva
     * @return ResponseBookingDto con el nuevo estado
     */
    ResponseBookingDto completeBooking(Long bookingId);

    /**
     * Verifica si un alojamiento está disponible para las fechas especificadas
     *
     * @param accommodationId ID del alojamiento
     * @param startDate       Fecha de inicio (check-in)
     * @param endDate         Fecha de fin (check-out)
     * @return true si está disponible, false en caso contrario
     */
    boolean isAccommodationAvailable(Long accommodationId, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene las fechas no disponibles de un alojamiento
     *
     * @param accommodationId ID del alojamiento
     * @return Lista de LocalDate con las fechas no disponibles
     */
    List<LocalDate> getUnavailableDates(Long accommodationId);

    /**
     * Obtiene las fechas disponibles de un alojamiento para un rango
     *
     * @param accommodationId ID del alojamiento
     * @param startDate       Fecha de inicio
     * @param endDate         Fecha de fin
     * @return Lista de LocalDate con las fechas disponibles
     */
    List<LocalDate> getAvailableDates(Long accommodationId, LocalDate startDate, LocalDate endDate);




}
