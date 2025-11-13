package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.TypeServicesEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccommodationDto {

    @Schema(description = "Título del alojamiento", example = "Casa rural con vista al mar", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String qualification;

    @Schema(description = "Descripción del alojamiento", example = "Hermoso apartamento totalmente equipado en el corazón de la ciudad. Perfecto para una estancia\n" +
            "cómoda con todas las comodidades necesarias.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La descripción es es obligatoria")
    @Size(max = 1000, message = "El título no puede exceder 1000 caracteres")
    private String description;

    @Schema(description = "Ciudad del alojamiento", example = "Salento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;

    @Schema(description = "Latitud del alojamiento", example = "4.6374", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = -180, message = "La latitud es obligatoria")
    private float latitude;

    @Schema(description = "Longitud del alojamiento", example = "-75.5705", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = -180, message = "La longitud es obligatoria")
    private float longitude;

    @Schema(description = "Precio por noche del alojamiento en pesos colombianos", example = "250000", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "El precio por noche debe ser un valor positivo")
    private double priceNight;

    @Schema(description = "Capacidad máxima de personas que admite el alojamiento", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1 persona")
    private int maximumCapacity;

    @Schema(description = "Servicios del alojamiento", example = "[\"WIFI\", \"PARKING\"]")
    @NotEmpty(message = "Debe tener al menos un servicio")
    private java.util.List<TypeServicesEnum> services;

    @Schema(description = "Id del anfitrión asociado al alojamiento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El id del anfitrión es obligatorio")
    private Long idHost;

    @Schema(description = "Imágenes del alojamiento")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<ImageAccommodationDto> images;

    @Schema(description = "Dirección completa del alojamiento",
            example = "Calle 10 #15-23, Barrio Centro")
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String address;

}
