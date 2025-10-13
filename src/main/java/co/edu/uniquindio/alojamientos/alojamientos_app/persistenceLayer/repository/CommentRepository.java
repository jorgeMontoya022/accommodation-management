package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // Comentario: Navega la relación accommodationEntity -> id y ordena por dateCreation desc
    List<CommentEntity> findByAccommodationEntity_IdOrderByDateCreationDesc(Long accommodationId);

    // (Opcional) Versión paginada para listas largas
    Page<CommentEntity> findByAccommodationEntity_Id(Long accommodationId, Pageable pageable);

    // Comentario: Verifica existencia por la relación bookingEntity -> id
    boolean existsByBookingEntity_Id(Long bookingId);

    // Comentario: Promedio de calificación (puede ser null si no hay comentarios)
    @Query("SELECT AVG(c.rating) FROM CommentEntity c WHERE c.accommodationEntity.id = :accommodationId")
    Double getAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);

    // Comentario: Conteo por alojamiento usando relación accommodationEntity -> id
    long countByAccommodationEntity_Id(Long accommodationId);
}
