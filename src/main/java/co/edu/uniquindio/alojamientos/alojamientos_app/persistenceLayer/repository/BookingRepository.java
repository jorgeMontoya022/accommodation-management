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

    // DISPONIBILIDAD / SOLAPAMIENTOS
    // Regla de solape (inclusive en bordes):
    //   (existing.dateCheckin <= end) AND (existing.dateCheckout >= start)

    boolean existsByAccommodationAssociated_IdAndStatusReservationInAndDateCheckinLessThanEqualAndDateCheckoutGreaterThanEqual(
            Long accommodationId,
            Collection<StatusReservation> statuses,
            LocalDateTime end,
            LocalDateTime start
    );

    List<BookingEntity> findByAccommodationAssociated_IdAndStatusReservationInAndDateCheckinLessThanEqualAndDateCheckoutGreaterThanEqual(
            Long accommodationId,
            Collection<StatusReservation> statuses,
            LocalDateTime end,
            LocalDateTime start
    );

    // LISTADOS POR HUÉSPED

    Page<BookingEntity> findByGuestEntity_IdOrderByDateCheckinDesc(Long guestId, Pageable pageable);

    Page<BookingEntity> findByGuestEntity_IdAndStatusReservationInOrderByDateCheckinDesc(
            Long guestId,
            Collection<StatusReservation> statuses,
            Pageable pageable
    );

    // LISTADOS POR ALOJAMIENTO

    Page<BookingEntity> findByAccommodationAssociated_IdOrderByDateCheckinDesc(
            Long accommodationId, Pageable pageable);

    Page<BookingEntity> findByAccommodationAssociated_IdAndStatusReservationInOrderByDateCheckinDesc(
            Long accommodationId, Collection<StatusReservation> statuses, Pageable pageable);

    Page<BookingEntity> findByAccommodationAssociated_IdAndDateCheckinGreaterThanEqualAndDateCheckoutLessThanEqualOrderByDateCheckinDesc(
            Long accommodationId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // LISTADOS / MÉTRICAS POR HOST

    @Query("select b from BookingEntity b " +
            "where b.accommodationAssociated.hostEntity.id = :hostId " +
            "order by b.dateCheckin desc")
    Page<BookingEntity> findByHostIdOrderByDateCheckinDesc(@Param("hostId") Long hostId, Pageable pageable);

    @Query("select b from BookingEntity b " +
            "where b.accommodationAssociated.hostEntity.id = :hostId " +
            "and b.statusReservation in :statuses " +
            "order by b.dateCheckin desc")
    Page<BookingEntity> findByHostIdAndStatusesOrderByDateCheckinDesc(@Param("hostId") Long hostId,
                                                                      @Param("statuses") Collection<StatusReservation> statuses,
                                                                      Pageable pageable);

    long countByAccommodationAssociated_HostEntity_IdAndStatusReservation(Long hostId, StatusReservation status);

    long countByAccommodationAssociated_IdAndStatusReservation(Long accommodationId, StatusReservation status);

    // CONSULTAS AUXILIARES

    Optional<BookingEntity> findByIdAndGuestEntity_Id(Long bookingId, Long guestId);

    List<BookingEntity> findByAccommodationAssociated_IdAndDateCheckinGreaterThanEqualOrderByDateCheckinAsc(
            Long accommodationId, LocalDateTime from);

    List<BookingEntity> findByDateCheckinGreaterThanEqualAndDateCheckoutLessThanEqualOrderByDateCheckinDesc(
            LocalDateTime start, LocalDateTime end);

    /** IDs de alojamientos ocupados en un rango (útil para excluir en búsquedas de disponibilidad). */
    @Query("select distinct b.accommodationAssociated.id " +
            "from BookingEntity b " +
            "where b.statusReservation in :statuses " +
            "and b.dateCheckin <= :end " +
            "and b.dateCheckout >= :start")
    List<Long> findOccupiedAccommodationIdsBetween(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("statuses") Collection<StatusReservation> statuses);

    /** ¿Existen reservas futuras (>= now) para un alojamiento? Útil para impedir eliminar el alojamiento. */
    boolean existsByAccommodationAssociated_IdAndDateCheckinGreaterThanEqual(Long accommodationId, LocalDateTime now);
}
