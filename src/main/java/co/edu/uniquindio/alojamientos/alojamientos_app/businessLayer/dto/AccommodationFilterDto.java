package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccommodationFilterDto {

    @Schema(description = "Ciudad exacta a filtrar")
    private String city;

    @Schema(description = "Precio mínimo por noche")
    @PositiveOrZero
    private Double priceMin;

    @Schema(description = "Precio máximo por noche")
    @PositiveOrZero
    private Double priceMax;

    @Schema(description = "Capacidad mínima requerida")
    @Min(1)
    private Integer capacityMin;

    @Schema(description = "Fecha de check-in para validar disponibilidad")
    private LocalDate checkin;

    @Schema(description = "Fecha de check-out para validar disponibilidad")
    private LocalDate checkout;

    @Schema(description = "Servicios (enum) como texto exacto; opcional")
    private String typeServicesEnum; // si luego quieres una lista, lo cambiamos a Set<String>
}
