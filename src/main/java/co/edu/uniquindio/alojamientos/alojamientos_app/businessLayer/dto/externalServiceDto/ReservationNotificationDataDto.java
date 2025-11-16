package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para agrupar la información necesaria para los correos
 * relacionados con reservas (confirmación, nueva reserva, cancelación).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para notificaciones por correo de reservas")
public class ReservationNotificationDataDto {

    @Schema(description = "ID de la reserva", example = "123")
    private Long reservationId;

    @Schema(description = "Estado de la reserva", example = "CONFIRMED")
    private String status;

    @Schema(description = "Fecha de check-in", example = "2025-01-10")
    private LocalDate checkInDate;

    @Schema(description = "Fecha de check-out", example = "2025-01-15")
    private LocalDate checkOutDate;

    @Schema(description = "Nombre del alojamiento")
    private String accommodationTitle;

    @Schema(description = "Dirección del alojamiento")
    private String accommodationAddress;

    @Schema(description = "Ciudad del alojamiento")
    private String accommodationCity;

    @Schema(description = "Nombre del huésped")
    private String guestName;

    @Schema(description = "Email del huésped")
    private String guestEmail;

    @Schema(description = "Nombre del anfitrión")
    private String hostName;

    @Schema(description = "Email del anfitrión")
    private String hostEmail;
}
