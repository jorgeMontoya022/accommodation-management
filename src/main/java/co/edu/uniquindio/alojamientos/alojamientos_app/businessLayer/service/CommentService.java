package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CreateCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;

import java.util.List;

public interface CommentService {
    ResponseCommentDto createComment(CreateCommentDto createCommentDto, Long authenticatedGuestId);

    ResponseCommentDto replyToComment(Long commentId, HostResponseDto hostResponseDto, Long authenticatedHostId);

    List<ResponseCommentDto> getCommentsByAccommodation(Long accommodationId);

    double getAccommodationAverageRating(Long accommodationId);

    long getAccommodationCommentCount(Long accommodationId);

    void deleteComment(Long commentId, Long authenticatedGuestId);
}
