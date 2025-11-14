package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de reserva")
public class ResponseBookingDto {

    @Schema(description = "ID único de la reserva", example = "1")
    private Long id;

    @Schema(description = "Fecha y hora de check-in", example = "2025-06-15T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCheckin;

    @Schema(description = "Fecha y hora de check-out", example = "2025-06-20T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCheckout;

    @Schema(description = "Fecha de creación de la reserva", example = "2025-05-10T09:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @Schema(description = "Estado actual de la reserva", example = "CONFIRMED")
    private StatusReservation statusReservation;

    @Schema(description = "Cantidad de personas", example = "3")
    private int quantityPeople;

    @Schema(description = "ID del huésped", example = "10")
    private Long idGuest;

    @Schema(description = "ID del alojamiento", example = "5")
    private Long idAccommodation;

    @Schema(description = "Valor total de la reserva en pesos colombianos", example = "1250000.00")
    private double totalValue;

    @Schema(description = "Nombre del huésped", example = "Juan Pérez")
    private String guestName;

    @Schema(description = "Email del huésped", example = "juan@example.com")
    private String guestEmail;

    @Schema(description = "Teléfono del huésped", example = "+57 300 123 4567")
    private String guestPhone;
}
