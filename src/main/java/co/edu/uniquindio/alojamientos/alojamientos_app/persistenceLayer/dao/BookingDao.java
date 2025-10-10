package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingDao {
    private final BookingRepository bookingRepository;
    public BookingEntity saveEntity(BookingEntity booking) {
        return bookingRepository.save(booking);
    }


    public Optional<BookingEntity> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public BookingEntity updateEntity(BookingEntity bookingEntity) {
        return bookingRepository.save(bookingEntity);
    }

    public List<BookingEntity> findByGuestId(Long guestId) {
        return bookingRepository.findByGuestEntityIdOrderByDateCreationDesc(guestId);
    }

    public List<BookingEntity> findOverlappingBookings(Long accommodationId,
                                                       LocalDateTime startDate, LocalDateTime endDate, List<StatusReservation> statuses) {
        return bookingRepository.findOverlappingBookings(accommodationId, startDate, endDate, statuses);
    }

    public List<BookingEntity> findByAccommodationId(Long accommodationId) {
        return bookingRepository.findByAccommodationAssociatedIdOrderByDateCreationDesc(accommodationId);
    }
}
