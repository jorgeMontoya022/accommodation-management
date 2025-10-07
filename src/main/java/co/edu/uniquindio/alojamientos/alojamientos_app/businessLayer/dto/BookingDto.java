package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de reserva")
public class BookingDto {

    @Schema(description = "ID único de la reserva", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Fecha y hora de check-in", example = "2025-06-15T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de check-in es obligatoria")
    @Future(message = "La fecha de check-in debe ser futura")
    private LocalDateTime dateCheckin;

    @Schema(description = "Fecha y hora de check-out", example = "2025-06-20T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de check-out es obligatoria")
    @Future(message = "La fecha de check-out debe ser futura")
    private LocalDateTime dateCheckout;

    @Schema(description = "Fecha de creación de la reserva", example = "2025-05-10T09:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateCreation;

    @Schema(description = "Fecha de cancelación de la reserva", example = "2025-06-10T16:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateCancellation;

    @Schema(description = "Fecha de la última actualización de la reserva", example = "2025-06-12T10:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateUpdate;

    @Schema(description = "Estado actual de la reserva", example = "CONFIRMED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El estado de la reserva es obligatorio")
    private StatusReservation statusReservation;

    @Schema(description = "ID del alojamiento asociado a la reserva", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del alojamiento es obligatorio")
    private Long idAccommodation;

    @Schema(description = "ID del huésped que realiza la reserva", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del huésped es obligatorio")
    private Long idGuest;

    @Schema(description = "Cantidad de personas para la reserva", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "La cantidad de personas debe ser al menos 1")
    @Max(value = 20, message = "La cantidad de personas no puede exceder 20")
    private int quantityPeople;

    @Schema(description = "Valor total de la reserva en pesos colombianos", example = "1250000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "El valor total debe ser un valor positivo")
    private double totalValue;

    @Schema(description = "Motivo de cancelación de la reserva", example = "Cambio de planes por situación personal")
    @Size(max = 500, message = "El motivo de cancelación no puede exceder 500 caracteres")
    private String reasonCancellation;
}