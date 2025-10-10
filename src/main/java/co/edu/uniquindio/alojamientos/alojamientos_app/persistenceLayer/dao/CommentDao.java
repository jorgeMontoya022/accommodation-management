package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentDao {

    private final CommentRepository commentRepository;

    public boolean existsByBookingId(Long bookingId) {
        return commentRepository.existsByBookingEntityId(bookingId);
    }

    public CommentEntity saveEntity(CommentEntity commentEntity) {
        return commentRepository.save(commentEntity);
    }

    public Optional<CommentEntity> findById(Long id) {
        return commentRepository.findById(id);
    }

    public CommentEntity updateEntity(CommentEntity commentEntity) {
        return commentRepository.save(commentEntity);
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    public List<CommentEntity> findByAccommodationId(Long accommodationId) {
        return commentRepository.findByAccommodationIdOrderByDateCreationDesc(accommodationId);
    }

    public double getAverageRatingByAccommodationId(Long accommodationId) {
        return commentRepository.getAverageRatingByAccommodationId(accommodationId);
    }

    public long countCommentsByAccommodationId(Long accommodationId) {
        return commentRepository.countByAccommodationEntityId(accommodationId);
    }
}
