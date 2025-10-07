package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingDto {
    @Schema(description = "Fecha y hora de check-in", example = "2025-06-15T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de check-in es obligatoria")
    @Future(message = "La fecha de check-in debe ser futura")
    private LocalDateTime dateCheckin;

    @Schema(description = "Fecha y hora de check-out", example = "2025-06-20T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de check-out es obligatoria")
    @Future(message = "La fecha de check-out debe ser futura")
    private LocalDateTime dateCheckout;

    @Schema(description = "Cantidad de personas para la reserva", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "La cantidad de personas debe ser al menos 1")
    @Max(value = 20, message = "La cantidad de personas no puede exceder 20")
    private int quantityPeople;
}
