package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para registro de usuario")
public class RegisterRequest {

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Correo electrónico único", example = "juan@ejemplo.com", required = true)
    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @Schema(description = "Número telefónico", example = "+57 300 1234567")
    private String telefono;

    @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "Rol del usuario en la plataforma",
            example = "huésped",
            allowableValues = {"huésped", "anfitrión"})
    private String rol;

    @Schema(description = "Contraseña de acceso", example = "miPassword123", minLength = 6, required = true)
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
