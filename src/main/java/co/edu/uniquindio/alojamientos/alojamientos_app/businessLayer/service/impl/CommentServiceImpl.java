package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CreateCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BookingService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.CommentService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.BookingDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.CommentDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentDao commentDao;
    private final CommentMapper commentMapper;
    private final BookingService bookingService;
    private final BookingDao bookingDao;

    @Override
    public ResponseCommentDto createComment(CreateCommentDto createCommentDto, Long authenticatedGuestId) {
        log.info("Creando comentario para la reserva ID: {}", createCommentDto.getIdBooking());

        BookingEntity booking = bookingDao.findById(createCommentDto.getIdBooking())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!booking.getGuestEntity().getId().equals(authenticatedGuestId)) {
            throw new RuntimeException("No tienes permiso para comentar esta reserva");
        }
        if (booking.getStatusReservation() != StatusReservation.COMPLETED) {
            throw new IllegalStateException(
                    "Solo puedes comentar reservas completadas. Estado actual: " + booking.getStatusReservation()
            );
        }
        // 4. Verificar que no ya existe un comentario para esta reserva
        if (commentDao.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("Ya existe un comentario para esta reserva");
        }

        // 5. Crear la entidad de comentario
        CommentEntity comment = new CommentEntity();
        comment.setRating(createCommentDto.getRating());
        comment.setText(createCommentDto.getText());
        comment.setAccommodationEntity(booking.getAccommodationAssociated());
        comment.setAuthorGuest((GuestEntity) booking.getGuestEntity());
        comment.setBookingEntity(booking);

        CommentEntity saved = commentDao.saveEntity(comment);

        log.info("Comentario creado exitosamente con ID: {}", saved.getId());

        return commentMapper.reviewEntityToReviewDto(saved);
    }

    @Override
    public ResponseCommentDto replyToComment(Long commentId, HostResponseDto hostResponseDto, Long authenticatedHostId) {
        log.info("Respondiendo comentario ID: {} por host ID: {}", commentId, authenticatedHostId);

        // 1. Obtener el comentario
        CommentEntity comment = commentDao.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // 2. Verificar que el host autenticado es el dueño del alojamiento
        if (!comment.getAccommodationEntity().getHostEntity().getId().equals(authenticatedHostId)) {
            throw new RuntimeException("No tienes permiso para responder este comentario");
        }

        // 3. Verificar que no ya existe una respuesta
        if (comment.getHostResponse() != null) {
            throw new IllegalStateException("Este comentario ya tiene una respuesta");
        }
        // 4. Asignar la respuesta
        comment.setHostResponse(hostResponseDto.getResponse());

        comment.setDateResponse(LocalDateTime.now());

        // 5. Guardar
        CommentEntity updated = commentDao.updateEntity(comment);

        log.info("Respuesta agregada exitosamente al comentario ID: {}", commentId);

        return commentMapper.reviewEntityToReviewDto(updated);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseCommentDto> getCommentsByAccommodation(Long accommodationId) {
        log.info("Obteniendo comentarios del alojamiento ID: {}", accommodationId);
        List<CommentEntity> comments = commentDao.findByAccommodationId(accommodationId);

        return commentMapper.getReviewsDto(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAccommodationAverageRating(Long accommodationId) {
        return commentDao.getAverageRatingByAccommodationId(accommodationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getAccommodationCommentCount(Long accommodationId) {
        return commentDao.countCommentsByAccommodationId(accommodationId);
    }

    @Override
    public void deleteComment(Long commentId, Long authenticatedGuestId) {
        log.info("Eliminando comentario ID: {}", commentId);

        // 1. Obtener el comentario
        CommentEntity comment = commentDao.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // 2. Verificar que es el autor del comentario
        if (!comment.getAuthorGuest().getId().equals(authenticatedGuestId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }

        // 3. No permitir eliminar si ya tiene respuesta del anfitrión
        if (comment.getHostResponse() != null) {
            throw new IllegalStateException("No puedes eliminar un comentario que ya tiene respuesta del anfitrión");
        }

        // 4. Eliminar
        commentDao.deleteById(commentId);

        log.info("Comentario eliminado exitosamente ID: {}", commentId);
    }
}
