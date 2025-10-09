package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CreateCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BookingService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.CommentService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.CommentDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentDao commentDao;
    private final CommentMapper commentMapper;
    //private final BookingService bookingService;
    @Override
    public ResponseCommentDto createComment(CreateCommentDto createCommentDto, Long authenticatedGuestId) {
        return null;
    }

    @Override
    public ResponseCommentDto replyToComment(Long commentId, HostResponseDto hostResponseDto, Long authenticatedHostId) {
        return null;
    }

    @Override
    public List<ResponseCommentDto> getCommentsByAccommodation(Long accommodationId) {
        return null;
    }

    @Override
    public double getAccommodationAverageRating(Long accommodationId) {
        return 0;
    }

    @Override
    public long getAccommodationCommentCount(Long accommodationId) {
        return 0;
    }

    @Override
    public void deleteComment(Long commentId, Long authenticatedGuestId) {

    }
}
