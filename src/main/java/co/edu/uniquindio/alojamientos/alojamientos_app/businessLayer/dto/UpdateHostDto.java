package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHostDto {
    @Schema(description = "Nombre completo del anfitrión", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Schema(description = "Número de teléfono del anfitrión", example = "+573001234567")
    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{7,15}$", message = "Teléfono inválido")
    private String phone;

    @Schema(description = "URL de la foto de perfil", example = "https://example.com/photos/juan.jpg")
    @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
    private String photoProfile;
}
