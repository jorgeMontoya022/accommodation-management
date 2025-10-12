package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inicio de sesión")
public class LoginDto {

    @Schema(description = "Correo electrónico del usuario", example = "juan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "Mg3lC@f3_2024!", accessMode = Schema.AccessMode.WRITE_ONLY  )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$", message = "La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un símbolo (@#$%^&+=!)")
    private String password;
}
