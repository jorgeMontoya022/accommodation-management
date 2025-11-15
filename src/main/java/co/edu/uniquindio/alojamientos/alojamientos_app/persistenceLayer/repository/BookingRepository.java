package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gestionar reservas.
 */
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    /**
     * Buscar reservas que se solapan con un rango de fechas para un alojamiento específico.
     * Se consideran únicamente las reservas con estado activo (por ejemplo, PAGADO o CONFIRMADO).
     *
     * @param accommodationId ID del alojamiento asociado
     * @param startDate fecha inicial del rango a verificar
     * @param endDate fecha final del rango a verificar
     * @param statuses lista de estados válidos a considerar (ej. PAID, CONFIRMED)
     * @return lista de reservas que se solapan con el rango indicado
     */
    @Query("SELECT b FROM BookingEntity b WHERE b.accommodationAssociated.id = :accommodationId " +
            "AND b.statusReservation IN :statuses " +
            "AND ((b.dateCheckin <= :endDate AND b.dateCheckout >= :startDate))")
    List<BookingEntity> findOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<StatusReservation> statuses);

    /**
     * Contar el número total de reservas asociadas a un alojamiento.
     *
     * @param accommodationId ID del alojamiento
     * @return cantidad total de reservas realizadas sobre ese alojamiento
     */
    @Query("SELECT COUNT(b) FROM BookingEntity b WHERE b.accommodationAssociated.id = :accommodationId")
    Long countBookingsByAccommodationId(@Param("accommodationId") Long accommodationId);

    List<BookingEntity> findByGuestEntityIdOrderByDateCreationDesc(Long guestId);

    List<BookingEntity> findByAccommodationAssociatedIdOrderByDateCreationDesc(Long accommodationId);

    @Query("""
        SELECT b 
        FROM BookingEntity b
        WHERE b.accommodationAssociated.hostEntity.id = :hostId
          AND b.statusReservation IN :statuses
          AND b.dateCheckin <= :endDate
          AND b.dateCheckout >= :startDate
        """)
    List<BookingEntity> findByHostAndDateRange(
            @Param("hostId") Long hostId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<StatusReservation> statuses
    );

}
