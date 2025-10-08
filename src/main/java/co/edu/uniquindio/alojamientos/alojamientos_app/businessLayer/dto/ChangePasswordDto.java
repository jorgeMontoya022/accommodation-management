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
@Schema(description = "DTO para cambiar la contraseña")
public class ChangePasswordDto {
    @Schema(description = "Contraseña actual", example = "MiContraseña123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;

    @Schema(description = "Nueva contraseña", example = "NuevaContraseña456!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String newPassword;

    @Schema(description = "Confirmación de la nueva contraseña", example = "NuevaContraseña456!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
}
