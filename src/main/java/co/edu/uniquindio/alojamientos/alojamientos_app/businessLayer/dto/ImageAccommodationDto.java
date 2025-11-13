package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de imagen de alojamiento")
public class ImageAccommodationDto {

    @Schema(description = "ID único de la imagen", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "URL de la imagen", example = "https://example.com/images/accommodation/room-001.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|webp|gif)$", message = "La URL debe ser válida y terminar en .jpg, .jpeg, .png, .webp o .gif", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String url;

    @Schema(description = "Indica si es la imagen principal del alojamiento", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Debe indicar si es la imagen principal")
    private Boolean isPrincipal;

    @Schema(description = "Orden de visualización de la imagen (1-6)", example = "1")
    @Min(value = 1, message = "El orden mínimo es 1")
    @Max(value = 6, message = "El orden máximo es 6")
    private Integer displayOrder;

    @Schema(description = "ID del alojamiento asociado a la imagen", example = "10",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idAccommodation;
}