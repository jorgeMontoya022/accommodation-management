package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    Optional<GuestEntity> findByEmail(String email);

    boolean exitsByEmail(String email);

    @Query("SELECT g FROM GuestEntity g WHERE SIZE(g.bookingEntityList) > 0 ")
    List<GuestEntity> findGuestsWithBookings();

    @Query("SELECT COUNT(b) FROM BookingEntity b WHERE b.guestEntity.id = :id")
    Long countBookingGuestById(Long id);

    @Query("SELECT g.active FROM GuestEntity g WHERE g.id = :id")
    Optional<Boolean> isActiveById(Long id);
}
