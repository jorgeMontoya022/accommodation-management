package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    /**
     * Buscar comentarios por alojamiento (ordenados por fecha descendente)
     */
    List<CommentEntity> findByAccommodationEntityIdOrderByDateCreationDesc(Long accommodationId);

    /**
     * Verificar si existe comentario para una reserva
     */
    boolean existsByBookingEntityId(Long bookingId);

    /**
     * Obtener promedio de calificación de un alojamiento
     */
    @Query("SELECT AVG(c.rating) FROM CommentEntity c WHERE c.accommodationEntity.id = :accommodationId")
    double getAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);

    /**
     * Contar comentarios por alojamiento
     */
    long countByAccommodationEntityId(Long accommodationId);
}