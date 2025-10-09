package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear un comentario/review")
public class CreateCommentDto {
    @Schema(description = "Calificación de 1 a 5 estrellas", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    @Schema(description = "Texto del comentario", example = "Excelente alojamiento, muy recomendado", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El texto del comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String text;

    @Schema(description = "ID de la reserva completada", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long idBooking;
}
