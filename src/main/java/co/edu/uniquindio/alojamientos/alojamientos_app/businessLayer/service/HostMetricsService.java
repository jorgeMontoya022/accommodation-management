package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostMetricsDto;

import java.time.LocalDate;

public interface HostMetricsService {

    HostMetricsDto getMetricsForHost(Long hostId, LocalDate from, LocalDate to);

}
