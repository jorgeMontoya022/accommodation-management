package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para respuesta del anfitrión a un comentario")
public class HostResponseDto {

    @Schema(description = "Respuesta del anfitrión", example = "¡Gracias por tu comentario!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La respuesta es obligatoria")
    @Size(max = 1000, message = "La respuesta no puede exceder 1000 caracteres")
    private String response;
}
