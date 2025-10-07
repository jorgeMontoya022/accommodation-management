package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de reseña")
public class CommentDto {

    @Schema(description = "ID único de la reseña", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Calificación de la reseña en estrellas", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    private Integer rating;

    @Schema(description = "Texto de la reseña", example = "Excelente alojamiento, muy limpio y cómodo. La ubicación es perfecta.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El texto de la reseña es obligatorio")
    @Size(max = 1000, message = "El texto de la reseña no puede exceder 1000 caracteres")
    private String text;

    @Schema(description = "Respuesta del anfitrión a la reseña", example = "Muchas gracias por tu comentario. Fue un placer tenerte como huésped.")
    @Size(max = 1000, message = "La respuesta del anfitrión no puede exceder 1000 caracteres")
    private String hostResponse;

    @Schema(description = "Fecha de creación de la reseña", example = "2025-06-25T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateCreation;

    @Schema(description = "Fecha de respuesta del anfitrión", example = "2025-06-26T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateResponse;

    @Schema(description = "ID del alojamiento asociado a la reseña", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del alojamiento es obligatorio")
    private Long idAccommodation;

    @Schema(description = "ID del huésped autor de la reseña", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del huésped es obligatorio")
    private Long idGuest;

    @Schema(description = "ID de la reserva asociada a la reseña", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long idBooking;
}