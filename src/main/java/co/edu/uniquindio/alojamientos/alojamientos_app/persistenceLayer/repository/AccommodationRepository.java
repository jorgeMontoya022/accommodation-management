package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends
        JpaRepository<AccommodationEntity, Long>,
        JpaSpecificationExecutor<AccommodationEntity> {

    /**
     * Buscar alojamientos por ciudad y estado ACTIVE.
     * Nota: @Where(deleted=false) en la entidad ya filtra los eliminados.
     */
    @Query("SELECT a FROM AccommodationEntity a " +
            "WHERE a.city = :city AND a.statusAccommodation = 'ACTIVE'")
    List<AccommodationEntity> findByCity(@Param("city") String city);

    /** Versión paginada */
    @Query("SELECT a FROM AccommodationEntity a " +
            "WHERE a.city = :city AND a.statusAccommodation = 'ACTIVE'")
    Page<AccommodationEntity> findByCity(@Param("city") String city, Pageable pageable);

    /**
     * Contar reservas asociadas a un alojamiento (métrica).
     */
    @Query("SELECT COUNT(r) FROM BookingEntity r WHERE r.accommodationAssociated.id = :id")
    Long countBookingsByAccommodationId(@Param("id") Long id);

    /**
     * Encontrar por id solo si no está eliminado.
     */
    Optional<AccommodationEntity> findByIdAndDeletedFalse(Long id);

    /**
     * Verificar que el alojamiento pertenece a un host (autorización).
     */
    boolean existsByIdAndHostEntity_Id(Long accommodationId, Long hostId);

    /**
     * Listar alojamientos de un host (paginado).
     */
    @EntityGraph(attributePaths = {"images"})
    Page<AccommodationEntity> findAllByHostEntity_IdAndDeletedFalse(Long hostId, Pageable pageable);

    /**
     * Bloqueo pesimista para evitar sobreventa durante creación de reservas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccommodationEntity a WHERE a.id = :id")
    Optional<AccommodationEntity> findByIdForUpdate(@Param("id") Long id);

    /**
     * Búsqueda básica por rango de precios (types alineados con double).
     * Para filtros avanzados, usar Specifications.
     */
    Page<AccommodationEntity> findAllByPriceNightBetweenAndDeletedFalse(
            double min,
            double max,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT a FROM AccommodationEntity a " +
            "LEFT JOIN FETCH a.images " +
            "WHERE a.id = :id AND a.deleted = false")
    Optional<AccommodationEntity> fetchWithImagesById(@Param("id") Long id);
    /** Listado general con imágenes (para la búsqueda paginada) */
    @EntityGraph(attributePaths = {"images"})
    Page<AccommodationEntity> findAllByDeletedFalse(Pageable pageable);

    List<AccommodationEntity> findAllByHostEntity_IdAndDeletedFalse(Long hostId);


}
