package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.*;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.AccommodationDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.BookingDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.*;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.BookingMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.BookingServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServicesImpl - Unit Tests")
public class BookingServicesImplTest {

    @Mock private BookingDao bookingDao;
    @Mock private AccommodationDao accommodationDao;
    @Mock private GuestService guestService;
    @Mock private BookingMapper bookingMapper;
    @Mock private co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService emailService;

    @InjectMocks
    private BookingServicesImpl bookingService;

    private GuestEntity guest;
    private AccommodationEntity accommodation;
    private BookingEntity bookingEntity;
    private RequestBookingDto requestDto;
    private UpdateBookingDto updateDto;
    private CancelBookingRequestDto cancelDto;

    @BeforeEach
    void setUp() {
        // Guest
        guest = new GuestEntity();
        guest.setId(10L);
        guest.setName("Pedro Huésped");
        guest.setEmail("pedro@host.com");
        guest.setActive(true);

        // Accommodation
        accommodation = new AccommodationEntity();
        accommodation.setId(5L);
        accommodation.setQualification("Casa Bella");
        accommodation.setPriceNight(200_000.0);
        accommodation.setMaximumCapacity(4);
        accommodation.setStatusAccommodation(StatusAccommodation.ACTIVE);

        // Booking entity
        bookingEntity = new BookingEntity();
        bookingEntity.setId(100L);
        bookingEntity.setGuestEntity(guest);
        bookingEntity.setAccommodationAssociated(accommodation);
        bookingEntity.setQuantityPeople(2);
        bookingEntity.setStatusReservation(StatusReservation.PENDING);
        bookingEntity.setDateCheckin(LocalDateTime.now().plusDays(10).withHour(14).withMinute(0));
        bookingEntity.setDateCheckout(LocalDateTime.now().plusDays(13).withHour(12).withMinute(0));
        bookingEntity.setTotalValue(accommodation.getPriceNight() * 3);

        // Request DTO
        requestDto = new RequestBookingDto(
                bookingEntity.getDateCheckin(),
                bookingEntity.getDateCheckout(),
                accommodation.getId(),
                guest.getId(),
                bookingEntity.getQuantityPeople()
        );

        // Update DTO
        updateDto = new UpdateBookingDto(
                bookingEntity.getDateCheckin().plusDays(1),
                bookingEntity.getDateCheckout().plusDays(1),
                3
        );

        // Cancel DTO
        cancelDto = new CancelBookingRequestDto("Motivo urgente");
    }


    // CREATE: success + validations
    @Test
    @DisplayName("CREATE - Flujo OK: crea reserva y envía emails")
    void createBooking_Success_SendsEmails() throws Exception {
        // Mocks
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(accommodationDao.findById(requestDto.getIdAccommodation())).thenReturn(Optional.of(accommodation));
        when(bookingDao.findOverlappingBookings(anyLong(), any(), any(), anyList())).thenReturn(List.of());
        // map dto -> entity and persisted result
        when(bookingMapper.bookingDtoToBookingEntity(requestDto)).thenReturn(new BookingEntity());
        // Simulate saved booking returned with id/populated fields
        BookingEntity saved = new BookingEntity();
        saved.setId(101L);
        saved.setGuestEntity(guest);
        saved.setAccommodationAssociated(accommodation);
        saved.setQuantityPeople(requestDto.getQuantityPeople());
        saved.setDateCheckin(requestDto.getDateCheckin());
        saved.setDateCheckout(requestDto.getDateCheckout());
        saved.setStatusReservation(StatusReservation.PENDING);
        saved.setTotalValue(accommodation.getPriceNight() * 3);
        when(bookingDao.saveEntity(any(BookingEntity.class))).thenReturn(saved);
        when(bookingMapper.bookingEntityToBookingDto(saved)).thenReturn(new ResponseBookingDto(
                saved.getId(), saved.getDateCheckin(), saved.getDateCheckout(), saved.getDateCreation(),
                saved.getStatusReservation(), saved.getQuantityPeople(),
                saved.getGuestEntity().getId(), saved.getAccommodationAssociated().getId(), saved.getTotalValue()
        ));

        // doNothing for email sending (service throws checked exception; mock default is fine)
        doNothing().when(emailService).sendMail(any(SendEmailDto.class));

        // execute
        ResponseBookingDto result = bookingService.createBooking(requestDto, guest.getId());

        // asserts
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getTotalValue()).isEqualTo(accommodation.getPriceNight() * 3);

        // verify interactions
        verify(guestService).getGuestEntityById(guest.getId());
        verify(accommodationDao).findById(accommodation.getId());
        verify(bookingDao).findOverlappingBookings(eq(accommodation.getId()), any(), any(), anyList());
        verify(bookingDao).saveEntity(any(BookingEntity.class));
        // verify emails attempted twice (guest + host)
        verify(emailService, atLeastOnce()).sendMail(any(SendEmailDto.class));
    }

    @Test
    @DisplayName("CREATE - Huésped inactivo lanza IllegalStateException")
    void createBooking_GuestInactive_Throws() {
        guest.setActive(false);
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);

        assertThatThrownBy(() -> bookingService.createBooking(requestDto, guest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está activo");

        verifyNoInteractions(accommodationDao, bookingDao, bookingMapper);
    }

    @Test
    @DisplayName("CREATE - Alojamiento no encontrado lanza RuntimeException")
    void createBooking_AccommodationNotFound_Throws() {
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(accommodationDao.findById(requestDto.getIdAccommodation())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(requestDto, guest.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(bookingDao, never()).saveEntity(any());
    }

    @Test
    @DisplayName("CREATE - Fechas inválidas: checkin pasado lanza IllegalArgumentException")
    void createBooking_CheckinInPast_Throws() {
        RequestBookingDto bad = new RequestBookingDto(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                accommodation.getId(),
                guest.getId(),
                1
        );
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(accommodationDao.findById(bad.getIdAccommodation())).thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.createBooking(bad, guest.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check-in debe ser en el futuro");
    }

    @Test
    @DisplayName("CREATE - No disponibilidad lanza IllegalStateException")
    void createBooking_NoAvailability_Throws() {
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(accommodationDao.findById(requestDto.getIdAccommodation())).thenReturn(Optional.of(accommodation));
        // simulate overlapping booking
        when(bookingDao.findOverlappingBookings(eq(accommodation.getId()), any(), any(), anyList()))
                .thenReturn(List.of(new BookingEntity()));

        assertThatThrownBy(() -> bookingService.createBooking(requestDto, guest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("CREATE - Excede capacidad lanza IllegalArgumentException")
    void createBooking_ExceedsCapacity_Throws() {
        RequestBookingDto big = new RequestBookingDto(
                requestDto.getDateCheckin(),
                requestDto.getDateCheckout(),
                accommodation.getId(),
                guest.getId(),
                accommodation.getMaximumCapacity() + 1
        );
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(accommodationDao.findById(big.getIdAccommodation())).thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.createBooking(big, guest.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede la capacidad máxima");
    }

    // UPDATE booking
    @Test
    @DisplayName("UPDATE - Flujo OK: actualiza reserva PENDING")
    void updateBooking_Success() {
        bookingEntity.setStatusReservation(StatusReservation.PENDING);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingDao.updateEntity(bookingEntity)).thenReturn(bookingEntity);
        when(bookingMapper.bookingEntityToBookingDto(bookingEntity)).thenReturn(new ResponseBookingDto());

        ResponseBookingDto res = bookingService.updateBooking(bookingEntity.getId(), updateDto, guest.getId());

        assertThat(res).isNotNull();
        verify(bookingDao).updateEntity(bookingEntity);
    }

    @Test
    @DisplayName("UPDATE - No propietario lanza RuntimeException")
    void updateBooking_NotOwner_Throws() {
        bookingEntity.setGuestEntity(new GuestEntity());
        bookingEntity.getGuestEntity().setId(999L); // different guest
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));

        assertThatThrownBy(() -> bookingService.updateBooking(bookingEntity.getId(), updateDto, guest.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }

    @Test
    @DisplayName("UPDATE - Estado no PENDING lanza IllegalStateException")
    void updateBooking_NotPending_Throws() {
        bookingEntity.setGuestEntity(guest);
        bookingEntity.setStatusReservation(StatusReservation.PAID);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));

        assertThatThrownBy(() -> bookingService.updateBooking(bookingEntity.getId(), updateDto, guest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo reservas PENDING pueden ser actualizadas");
    }


    // CONFIRM

    @Test
    @DisplayName("CONFIRM - Flujo OK cambia a PAID y envía email")
    void confirmBooking_Success() throws Exception {
        // set up booking whose accommodation host id will match authenticatedHostId
        HostEntity host = new HostEntity();
        host.setId(55L);
        accommodation.setHostEntity(host);

        bookingEntity.setAccommodationAssociated(accommodation);
        bookingEntity.setGuestEntity(guest);
        bookingEntity.setStatusReservation(StatusReservation.PENDING);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingDao.updateEntity(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.bookingEntityToBookingDto(any(BookingEntity.class))).thenReturn(new ResponseBookingDto());

        doNothing().when(emailService).sendMail(any(SendEmailDto.class));

        bookingService.confirmBooking(bookingEntity.getId(), host.getId());

        assertThat(bookingEntity.getStatusReservation()).isEqualTo(StatusReservation.PAID);
        verify(emailService).sendMail(any(SendEmailDto.class));
    }

    @Test
    @DisplayName("CONFIRM - Sin permisos lanza RuntimeException")
    void confirmBooking_NoPermission_Throws() {
        HostEntity host = new HostEntity();
        host.setId(99L);
        accommodation.setHostEntity(host);
        bookingEntity.setAccommodationAssociated(accommodation);
        bookingEntity.setStatusReservation(StatusReservation.PENDING);

        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));

        // authenticated host is different
        assertThatThrownBy(() -> bookingService.confirmBooking(bookingEntity.getId(), 123L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }


    // REJECT, CANCEL, COMPLETE
    @Test
    @DisplayName("REJECT - Flujo OK cambia a CANCELED y envía email")
    void rejectBooking_Success() throws Exception {
        HostEntity host = new HostEntity();
        host.setId(55L);
        accommodation.setHostEntity(host);

        bookingEntity.setAccommodationAssociated(accommodation);
        bookingEntity.setGuestEntity(guest);
        bookingEntity.setStatusReservation(StatusReservation.PENDING);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingDao.updateEntity(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.bookingEntityToBookingDto(any(BookingEntity.class))).thenReturn(new ResponseBookingDto());
        doNothing().when(emailService).sendMail(any(SendEmailDto.class));

        ResponseBookingDto dto = bookingService.rejectBooking(bookingEntity.getId(), "Motivo", host.getId());

        assertThat(dto).isNotNull();
        assertThat(bookingEntity.getStatusReservation()).isEqualTo(StatusReservation.CANCELED);
        verify(emailService).sendMail(any(SendEmailDto.class));
    }

    @Test
    @DisplayName("CANCEL - Flujo OK: cancela reserva correctamente y envía email")
    void cancelBooking_Success() throws Exception {
        HostEntity host = new HostEntity();
        host.setEmail("host@example.com");
        AccommodationEntity accommodation = new AccommodationEntity();
        accommodation.setHostEntity(host);

        bookingEntity.setAccommodationAssociated(accommodation);
        bookingEntity.setStatusReservation(StatusReservation.PAID);
        bookingEntity.setGuestEntity(guest);

        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingDao.updateEntity(bookingEntity)).thenReturn(bookingEntity);
        when(bookingMapper.bookingEntityToBookingDto(bookingEntity)).thenReturn(new ResponseBookingDto());

        CancelBookingRequestDto cancelDto = new CancelBookingRequestDto();
        cancelDto.setReasonCancellation("Cambio de planes");

        ResponseBookingDto res = bookingService.cancelBooking(
                bookingEntity.getId(),
                cancelDto,
                guest.getId()
        );

        assertThat(res).isNotNull();
        verify(bookingDao).updateEntity(bookingEntity);
        verify(emailService).sendMail(any(SendEmailDto.class));
    }

    @Test
    @DisplayName("COMPLETE - Solo PAID puede completarse")
    void completeBooking_ValidatesState() throws Exception {
        bookingEntity.setStatusReservation(StatusReservation.PAID);
        bookingEntity.setGuestEntity(guest);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingDao.updateEntity(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.bookingEntityToBookingDto(any(BookingEntity.class))).thenReturn(new ResponseBookingDto());
        doNothing().when(emailService).sendMail(any(SendEmailDto.class));

        ResponseBookingDto resp = bookingService.completeBooking(bookingEntity.getId());

        assertThat(resp).isNotNull();
        assertThat(bookingEntity.getStatusReservation()).isEqualTo(StatusReservation.COMPLETED);
        verify(emailService).sendMail(any(SendEmailDto.class));
    }

    @Test
    @DisplayName("COMPLETE - Estado inválido lanza IllegalStateException")
    void completeBooking_InvalidState_Throws() {
        bookingEntity.setStatusReservation(StatusReservation.PENDING);
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));

        assertThatThrownBy(() -> bookingService.completeBooking(bookingEntity.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo reservas PAID pueden ser completadas");
    }


    // UTILITIES
    @Test
    @DisplayName("isAccommodationAvailable - retorna verdadero cuando no hay superposición")
    void isAccommodationAvailable_NoOverlap_ReturnsTrue() {
        when(bookingDao.findOverlappingBookings(eq(accommodation.getId()), any(), any(), anyList())).thenReturn(List.of());
        boolean avail = bookingService.isAccommodationAvailable(accommodation.getId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        assertThat(avail).isTrue();
    }

    @Test
    @DisplayName("getUnavailableDates - convierte bookings en fechas no disponibles")
    void getUnavailableDates_ReturnsDates() {
        BookingEntity b = new BookingEntity();
        b.setDateCheckin(LocalDateTime.of(2025, 6, 10, 14, 0));
        b.setDateCheckout(LocalDateTime.of(2025, 6, 12, 12, 0));
        when(bookingDao.findOverlappingBookings(eq(accommodation.getId()), any(), any(), anyList())).thenReturn(List.of(b));

        List<LocalDate> list = bookingService.getUnavailableDates(accommodation.getId());

        assertThat(list).contains(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 11), LocalDate.of(2025, 6, 12));
    }

    @Test
    @DisplayName("getBookingById - retorna DTO")
    void getBookingById_ReturnsDto() {
        when(bookingDao.findById(bookingEntity.getId())).thenReturn(Optional.of(bookingEntity));
        when(bookingMapper.bookingEntityToBookingDto(bookingEntity)).thenReturn(new ResponseBookingDto());

        ResponseBookingDto dto = bookingService.getBookingById(bookingEntity.getId());
        assertThat(dto).isNotNull();
        verify(bookingDao).findById(bookingEntity.getId());
    }

    @Test
    @DisplayName("getBookingsByGuest - delega en DAO y mapper")
    void getBookingsByGuest_Delegates() {
        when(guestService.getGuestEntityById(guest.getId())).thenReturn(guest);
        when(bookingDao.findByGuestId(guest.getId())).thenReturn(List.of(bookingEntity));
        when(bookingMapper.getBookingsDto(anyList())).thenReturn(List.of(new ResponseBookingDto()));

        List<ResponseBookingDto> dtos = bookingService.getBookingsByGuest(guest.getId());
        assertThat(dtos).isNotEmpty();
        verify(bookingDao).findByGuestId(guest.getId());
    }

    @Test
    @DisplayName("getBookingsByAccommodation - delega en DAO y mapper")
    void getBookingsByAccommodation_Delegates() {
        when(accommodationDao.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(bookingDao.findByAccommodationId(accommodation.getId())).thenReturn(List.of(bookingEntity));
        when(bookingMapper.getBookingsDto(anyList())).thenReturn(List.of(new ResponseBookingDto()));

        List<ResponseBookingDto> dtos = bookingService.getBookingsByAccommodation(accommodation.getId());
        assertThat(dtos).isNotEmpty();
        verify(bookingDao).findByAccommodationId(accommodation.getId());
    }

    @Test
    @DisplayName("getAvailableDates - devuelve fechas disponibles dentro del rango")
    void getAvailableDates_ReturnsAvailableBetweenRange() {
        // Suppose getUnavailableDates returns 1 date blocked
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(4);
        when(accommodationDao.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(bookingDao.findOverlappingBookings(eq(accommodation.getId()), any(), any(), anyList())).thenReturn(List.of());

        List<LocalDate> available = bookingService.getAvailableDates(accommodation.getId(), start, end);
        // no unavailable -> available should equal full range
        assertThat(available.size()).isEqualTo((int) (end.toEpochDay() - start.toEpochDay()) + 1);
    }
}
