package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostMetricsDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostMetricsService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.BookingRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostMetricsServiceImpl implements HostMetricsService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public HostMetricsDto getMetricsForHost(Long hostId, LocalDate from, LocalDate to) {
        log.info("Calculando métricas para host {} entre {} y {}", hostId, from, to);

        // Defaults si no vienen fechas
        LocalDate today = LocalDate.now();
        if (to == null) {
            to = today;
        }
        if (from == null) {
            from = to.minusMonths(3);
        }
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.atTime(23, 59, 59);

        List<StatusReservation> estadosActivos = List.of(
                StatusReservation.PAID,
                StatusReservation.CONFIRMED,
                StatusReservation.COMPLETED
        );

        // 1) Todos los alojamientos del host (no eliminados)
        List<AccommodationEntity> alojamientos = accommodationRepository
                .findAllByHostEntity_IdAndDeletedFalse(hostId);

        if (alojamientos.isEmpty()) {
            return HostMetricsDto.builder()
                    .totalReservas(0)
                    .ingresosTotales(0.0)
                    .ratingPromedio(0.0)
                    .ocupacionPromedio(0.0)
                    .reservasPorMes(Collections.emptyList())
                    .build();
        }

        // 2) Todas las reservas del host en el rango y con estados activos
        List<BookingEntity> reservas = bookingRepository.findByHostAndDateRange(
                hostId,
                startDateTime,
                endDateTime,
                estadosActivos
        );

        long totalReservas = reservas.size();
        double ingresosTotales = reservas.stream()
                .mapToDouble(BookingEntity::getTotalValue)
                .sum();

        // 3) Rating promedio del host (promedio de los promedios de cada alojamiento)
        double ratingPromedio = calcularRatingPromedio(alojamientos);

        // 4) Ocupación promedio (noches reservadas / (días rango × nº alojamientos) × 100)
        double ocupacionPromedio = calcularOcupacionPromedio(alojamientos, reservas, from, to);

        // 5) Reservas por mes
        List<HostMetricsDto.ReservasMesDto> reservasPorMes = calcularReservasPorMes(reservas);

        return HostMetricsDto.builder()
                .totalReservas(totalReservas)
                .ingresosTotales(ingresosTotales)
                .ratingPromedio(ratingPromedio)
                .ocupacionPromedio(ocupacionPromedio)
                .reservasPorMes(reservasPorMes)
                .build();
    }

    private double calcularRatingPromedio(List<AccommodationEntity> alojamientos) {
        double sumaRatings = 0.0;
        int alojamientosConRating = 0;

        for (AccommodationEntity acc : alojamientos) {
            Double avg = commentRepository.getAverageRatingByAccommodationId(acc.getId());
            if (avg != null) {
                sumaRatings += avg;
                alojamientosConRating++;
            }
        }

        if (alojamientosConRating == 0) return 0.0;
        return sumaRatings / alojamientosConRating;
    }

    private double calcularOcupacionPromedio(List<AccommodationEntity> alojamientos,
                                             List<BookingEntity> reservas,
                                             LocalDate from,
                                             LocalDate to) {
        long diasRango = ChronoUnit.DAYS.between(from, to) + 1;
        if (diasRango <= 0 || alojamientos.isEmpty()) {
            return 0.0;
        }

        long nochesReservadas = 0;

        for (BookingEntity b : reservas) {
            LocalDate inicio = b.getDateCheckin().toLocalDate();
            LocalDate fin = b.getDateCheckout().toLocalDate();

            // Intersección con el rango solicitado
            LocalDate inicioEf = inicio.isBefore(from) ? from : inicio;
            LocalDate finEf = fin.isAfter(to) ? to : fin;

            long noches = ChronoUnit.DAYS.between(inicioEf, finEf);
            if (noches > 0) {
                nochesReservadas += noches;
            }
        }

        long capacidadTotal = diasRango * alojamientos.size();
        if (capacidadTotal == 0) return 0.0;

        return (nochesReservadas * 100.0) / capacidadTotal;
    }

    private List<HostMetricsDto.ReservasMesDto> calcularReservasPorMes(List<BookingEntity> reservas) {
        Map<YearMonth, List<BookingEntity>> agrupado = reservas.stream()
                .collect(Collectors.groupingBy(b -> YearMonth.from(b.getDateCheckin().toLocalDate())));

        return agrupado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    YearMonth ym = entry.getKey();
                    List<BookingEntity> lista = entry.getValue();

                    long count = lista.size();
                    double ingresos = lista.stream()
                            .mapToDouble(BookingEntity::getTotalValue)
                            .sum();

                    String mesLabel = ym.toString(); // "2025-11"
                    return HostMetricsDto.ReservasMesDto.builder()
                            .mes(mesLabel)
                            .reservas(count)
                            .ingresos(ingresos)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
