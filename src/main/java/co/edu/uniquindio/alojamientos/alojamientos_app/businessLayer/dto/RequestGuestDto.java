package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del huésped")
public class RequestGuestDto {

    @Schema(description = "Nombre completo del huésped", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Schema(description = "Correo electrónico del huésped", example = "juan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Schema(description = "Fecha de nacimiento del huésped", example = "1990-05-15")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate dateBirth;


    @Schema(description = "Número de teléfono del huésped", example = "+573001234567")
    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{7,15}$", message = "Teléfono inválido")
    private String phone;

    @Schema(description = "URL de la foto de perfil del huésped", example = "https://example.com/photos/juan.jpg")
    @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
    private String photoProfile;

    //TODO: Crear la contrasena en el dto y sus llamados.
    @Schema(description = "Contraseña del huésped", example = "Mg3lC@f3_2024!", accessMode = Schema.AccessMode.WRITE_ONLY  )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$", message = "La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un símbolo (@#$%^&+=!)")
    private String password;
}