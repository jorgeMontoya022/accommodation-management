package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostMetricsDto {

    private long totalReservas;
    private double ingresosTotales;
    private double ratingPromedio;     // 0–5
    private double ocupacionPromedio;  // 0–100 (%)

    private List<ReservasMesDto> reservasPorMes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservasMesDto {
        private String mes;      // ej: "2025-11"
        private long reservas;
        private double ingresos;
    }
}
