package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true) // Comentario: por defecto solo lectura; los métodos de escritura anotan override
public class CommentDao {

    private final CommentRepository commentRepository;

    // Comentario: ajustado al nombre correcto del método (navega bookingEntity -> id)
    public boolean existsByBookingId(Long bookingId) {
        return commentRepository.existsByBookingEntity_Id(bookingId);
    }

    @Transactional // Comentario: escritura
    public CommentEntity saveEntity(CommentEntity commentEntity) {
        return commentRepository.save(commentEntity);
    }

    public Optional<CommentEntity> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional // Comentario: escritura
    public CommentEntity updateEntity(CommentEntity commentEntity) {
        return commentRepository.save(commentEntity);
    }

    @Transactional // Comentario: escritura
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    // Comentario: ajustado al método nuevo: accommodationEntity -> id + orden por fecha desc
    public List<CommentEntity> findByAccommodationId(Long accommodationId) {
        return commentRepository.findByAccommodationEntity_IdOrderByDateCreationDesc(accommodationId);
    }

    // Comentario: AVG puede venir null si no hay comentarios; devolvemos 0.0 en ese caso
    public double getAverageRatingByAccommodationId(Long accommodationId) {
        Double avg = commentRepository.getAverageRatingByAccommodationId(accommodationId);
        return avg != null ? avg : 0.0;
    }

    // Comentario: ajustado a countByAccommodationEntity_Id
    public long countCommentsByAccommodationId(Long accommodationId) {
        return commentRepository.countByAccommodationEntity_Id(accommodationId);
    }
}
