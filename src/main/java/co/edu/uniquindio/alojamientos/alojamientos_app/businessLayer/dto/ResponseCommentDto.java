package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para comentario")
public class ResponseCommentDto {

    @Schema(description = "ID del comentario", example = "1")
    private Long id;

    @Schema(description = "Calificación", example = "5")
    private Integer rating;

    @Schema(description = "Texto del comentario")
    private String text;

    @Schema(description = "Respuesta del anfitrión")
    private String hostResponse;

    @Schema(description = "Fecha de creación")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @Schema(description = "Fecha de respuesta del anfitrión")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateResponse;

    @Schema(description = "Nombre del autor")
    private String authorName;

    @Schema(description = "Nombre del alojamiento")
    private String accommodationName;
}
