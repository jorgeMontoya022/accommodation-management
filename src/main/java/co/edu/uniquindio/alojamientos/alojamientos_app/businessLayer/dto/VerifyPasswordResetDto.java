package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Verificación de código y nueva contraseña")
public class VerifyPasswordResetDto {

    @Schema(description = "Email del usuario", example = "usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @Schema(description = "Código de verificación de 6 dígitos", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe ser de 6 dígitos")
    private String code;

    @Schema(description = "Nueva contraseña", example = "NuevaPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String newPassword;

    @Schema(description = "Confirmación de contraseña", example = "NuevaPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La confirmación es obligatoria")
    private String confirmPassword;
}
