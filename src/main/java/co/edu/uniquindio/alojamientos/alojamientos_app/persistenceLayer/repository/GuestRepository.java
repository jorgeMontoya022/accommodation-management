package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    /**
     * Verifica si existe un huésped con el email dado
     */
    Optional<GuestEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Busca huespedes que tienen al menos una reserva
     */
    @Query("SELECT g FROM GuestEntity g WHERE SIZE(g.bookingEntityList) > 0 ")
    List<GuestEntity> findGuestsWithBookings();

    /**
     * Cuenta la cantidad de reservas que tiene un huésped
     */
    @Query("SELECT COUNT(b) FROM BookingEntity b WHERE b.guestEntity.id = :id")
    Long countBookingGuestById(Long id);

    /**
     * Verifica si un huésped está activo
     */
    @Query("SELECT g.active FROM GuestEntity g WHERE g.id = :id")
    Optional<Boolean> isActiveById(Long id);
}
