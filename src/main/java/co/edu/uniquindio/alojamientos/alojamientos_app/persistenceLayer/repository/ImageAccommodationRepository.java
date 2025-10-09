package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad ImageAccommodation.
 * Nota: solo contiene operaciones de acceso a datos, sin lógica de negocio.
 */
@Repository
public interface ImageAccommodationRepository extends JpaRepository<ImageAccommodation, Long> {

    /** Obtiene todas las imágenes de un alojamiento ordenadas por displayOrder ascendente. */
    List<ImageAccommodation> findByAccommodationEntity_IdOrderByDisplayOrderAsc(Long accommodationId);

    /** Obtiene la imagen principal (si existe) de un alojamiento. */
    Optional<ImageAccommodation> findFirstByAccommodationEntity_IdAndIsPrincipalTrue(Long accommodationId);

    /** Cuenta cuántas imágenes tiene actualmente un alojamiento. */
    long countByAccommodationEntity_Id(Long accommodationId);

    /** Verifica si ya existe una imagen principal para el alojamiento. */
    boolean existsByAccommodationEntity_IdAndIsPrincipalTrue(Long accommodationId);

    /** Obtiene la mayor posición (displayOrder) usada en un alojamiento (0 si no hay imágenes). */
    @Query("select coalesce(max(i.displayOrder), 0) " +
            "from ImageAccommodation i " +
            "where i.accommodationEntity.id = :accommodationId")
    int findMaxDisplayOrderByAccommodationId(@Param("accommodationId") Long accommodationId);

    /** Recupera una imagen por id validando que pertenezca al alojamiento dado. */
    Optional<ImageAccommodation> findByIdAndAccommodationEntity_Id(Long imageId, Long accommodationId);

    /** Elimina todas las imágenes asociadas a un alojamiento. */
    @Transactional
    long deleteByAccommodationEntity_Id(Long accommodationId);

    /** Desmarca todas las imágenes como principales para un alojamiento (opcionalmente conserva una). */
    @Modifying
    @Transactional
    @Query("update ImageAccommodation i " +
            "set i.isPrincipal = false " +
            "where i.accommodationEntity.id = :accommodationId " +
            "and (:keepId is null or i.id <> :keepId)")
    int unsetPrincipalForAccommodation(@Param("accommodationId") Long accommodationId,
                                       @Param("keepId") Long keepId);
}
