package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CreateCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.CommentServiceImpl; // ✅ usar la impl
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gestión de comentarios.
 * - Usa Authentication para extraer el ID del usuario (huésped/host).
 * - La validación y reglas de negocio se delegan a CommentServiceImpl.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comentarios", description = "Endpoints para comentarios y calificaciones")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CommentController {

    // Inyectar la implementación concreta
    private final CommentServiceImpl commentService;

    // CREATE (guest)
    @Operation(summary = "Crear comentario (huésped) — solo después del checkout")
    @PostMapping("/comments")
    public ResponseEntity<ResponseCommentDto> createComment(
            @Valid @RequestBody CreateCommentDto createCommentDto,
            Authentication authentication
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseCommentDto created = commentService.createComment(createCommentDto, guestId);
        return ResponseEntity
                .created(URI.create("/api/v1/comments/" + created.getId()))
                .body(created);
    }

    // REPLY (host)
    @Operation(summary = "Responder comentario (anfitrión)")
    @PostMapping("/comments/{id}/answer")
    public ResponseEntity<ResponseCommentDto> replyToComment(
            @PathVariable Long id,
            @Valid @RequestBody HostResponseDto hostResponseDto,
            Authentication authentication
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        ResponseCommentDto updated = commentService.replyToComment(id, hostResponseDto, hostId);
        return ResponseEntity.ok(updated);
    }

    // LIST
    @Operation(summary = "Listar comentarios de un alojamiento (más recientes primero)")
    @GetMapping("/accommodation/{id}/comments")
    public ResponseEntity<List<ResponseCommentDto>> getCommentsByAccommodation(@PathVariable("id") Long accommodationId) {
        List<ResponseCommentDto> list = commentService.getCommentsByAccommodation(accommodationId);
        return ResponseEntity.ok(list);
    }

    // METRICS
    @Operation(summary = "Promedio de calificaciones de un alojamiento")
    @GetMapping("/accommodation/{id}/ratings/average")
    public ResponseEntity<Double> getAccommodationAverageRating(@PathVariable("id") Long accommodationId) {
        long count = commentService.getAccommodationCommentCount(accommodationId);
        if (count == 0) {
            return ResponseEntity.noContent().build();
        }
        double avg = commentService.getAccommodationAverageRating(accommodationId);
        return ResponseEntity.ok(avg);
    }

    // DELETE (guest)
    @Operation(summary = "Eliminar mi comentario (huésped)")
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        commentService.deleteComment(id, guestId);
        return ResponseEntity.ok().build();
    }

    // Helper
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String s) {
            return Long.valueOf(s);
        }
        throw new RuntimeException("No se pudo extraer el ID del usuario");
    }
}
