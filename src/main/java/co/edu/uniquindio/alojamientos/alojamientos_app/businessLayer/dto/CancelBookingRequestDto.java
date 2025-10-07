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
@Schema(description = "Datos para cancelar una reserva")
public class CancelBookingRequestDto {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reasonCancellation;
}
