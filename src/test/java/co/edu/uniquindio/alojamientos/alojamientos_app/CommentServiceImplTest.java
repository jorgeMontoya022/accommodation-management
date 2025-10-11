package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CreateCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.CommentServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.BookingDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.CommentDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.*;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.CommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CommentServiceImpl
 */
class CommentServiceImplTest {

    @Mock
    private CommentDao commentDao;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingDao bookingDao;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // createComment()
    @Test
    @DisplayName("createComment - Crea comentario exitosamente")
    void testCreateCommentSuccess() {
        CreateCommentDto dto = new CreateCommentDto(5, "Excelente", 1L);

        GuestEntity guest = new GuestEntity();
        guest.setId(10L);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setGuestEntity(guest);
        booking.setStatusReservation(StatusReservation.COMPLETED);
        booking.setAccommodationAssociated(new AccommodationEntity());

        CommentEntity saved = new CommentEntity();
        saved.setId(100L);
        saved.setTex("Excelente");

        ResponseCommentDto responseMock = new ResponseCommentDto();
        responseMock.setId(100L);

        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));
        when(commentDao.existsByBookingId(1L)).thenReturn(false);
        when(commentDao.saveEntity(any(CommentEntity.class))).thenReturn(saved);
        when(commentMapper.reviewEntityToReviewDto(saved)).thenReturn(responseMock);

        ResponseCommentDto result = commentService.createComment(dto, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(commentDao).saveEntity(any(CommentEntity.class));
    }

    @Test
    @DisplayName("createComment - Lanza excepción si reserva no existe")
    void testCreateCommentBookingNotFound() {
        CreateCommentDto dto = new CreateCommentDto(4, "Texto", 99L);
        when(bookingDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.createComment(dto, 10L));
    }

    @Test
    @DisplayName("createComment - Lanza excepción si no es el autor de la reserva")
    void testCreateCommentUnauthorizedGuest() {
        CreateCommentDto dto = new CreateCommentDto(4, "Texto", 1L);

        GuestEntity guest = new GuestEntity();
        guest.setId(10L);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setGuestEntity(guest);
        booking.setStatusReservation(StatusReservation.COMPLETED);

        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> commentService.createComment(dto, 99L));
    }


    // replyToComment()

    @Test
    @DisplayName("replyToComment - Responde comentario exitosamente")
    void testReplyToCommentSuccess() {
        HostResponseDto responseDto = new HostResponseDto("Gracias por tu comentario");

        HostEntity host = new HostEntity();
        host.setId(5L);

        AccommodationEntity accommodation = new AccommodationEntity();
        accommodation.setHostEntity(host);

        CommentEntity comment = new CommentEntity();
        comment.setId(1L);
        comment.setAccommodationEntity(accommodation);

        when(commentDao.findById(1L)).thenReturn(Optional.of(comment));
        when(commentDao.updateEntity(any(CommentEntity.class))).thenReturn(comment);
        when(commentMapper.reviewEntityToReviewDto(any(CommentEntity.class)))
                .thenReturn(new ResponseCommentDto());

        commentService.replyToComment(1L, responseDto, 5L);

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentDao).updateEntity(captor.capture());
        assertEquals("Gracias por tu comentario", captor.getValue().getHostResponse());
    }

    @Test
    @DisplayName("replyToComment - Lanza excepción si host no es el dueño")
    void testReplyToCommentUnauthorizedHost() {
        HostResponseDto responseDto = new HostResponseDto("Respuesta");

        HostEntity host = new HostEntity();
        host.setId(99L);

        AccommodationEntity accommodation = new AccommodationEntity();
        accommodation.setHostEntity(host);

        CommentEntity comment = new CommentEntity();
        comment.setId(1L);
        comment.setAccommodationEntity(accommodation);

        when(commentDao.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> commentService.replyToComment(1L, responseDto, 5L));
    }


    // getCommentsByAccommodation()


    @Test
    @DisplayName("getCommentsByAccommodation - Retorna lista de comentarios")
    void testGetCommentsByAccommodation() {
        CommentEntity comment = new CommentEntity();

        when(commentDao.findByAccommodationId(1L)).thenReturn(List.of(comment));
        when(commentMapper.getReviewsDto(anyList())).thenReturn(List.of(new ResponseCommentDto()));

        List<ResponseCommentDto> result = commentService.getCommentsByAccommodation(1L);

        assertEquals(1, result.size());
        verify(commentDao).findByAccommodationId(1L);
    }


    // 🧩 deleteComment()

    @Test
    @DisplayName("deleteComment - Elimina comentario exitosamente")
    void testDeleteCommentSuccess() {
        GuestEntity guest = new GuestEntity();
        guest.setId(10L);

        CommentEntity comment = new CommentEntity();
        comment.setId(1L);
        comment.setAuthorGuest(guest);

        when(commentDao.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 10L);

        verify(commentDao).deleteById(1L);
    }

    @Test
    @DisplayName("deleteComment - Lanza excepción si usuario no es autor")
    void testDeleteCommentUnauthorized() {
        GuestEntity guest = new GuestEntity();
        guest.setId(10L);

        CommentEntity comment = new CommentEntity();
        comment.setId(1L);
        comment.setAuthorGuest(guest);

        when(commentDao.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(1L, 20L));
    }
}
