package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para envío de correos electrónicos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para envío de correo electrónico")
public class SendEmailDto {

    @Schema(description = "Asunto del correo", example = "Confirmación de reserva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    @Schema(description = "Cuerpo del mensaje", example = "Estimado usuario, su reserva ha sido confirmada...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El cuerpo del mensaje es obligatorio")
    @Size(max = 10000, message = "El cuerpo no puede exceder 10000 caracteres")
    private String body;

    @Schema(description = "Destinatario del correo", example = "usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "Email del destinatario inválido")
    private String recipient;
}