package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CancelBookingRequestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.BookingServicesImpl; // ✅ usar la impl
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusReservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservas", description = "Endpoints para la gestión de reservas")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BookingController {

    // Inyectar implementación concreta
    private final BookingServicesImpl bookingService;

    // CREATE
    @Operation(summary = "Crear reserva (huésped autenticado)")
    @PostMapping
    public ResponseEntity<ResponseBookingDto> createBooking(
            @Valid @RequestBody RequestBookingDto requestDto,
            Authentication authentication
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseBookingDto created = bookingService.createBooking(requestDto, guestId);
        return ResponseEntity
                .created(URI.create("/api/v1/reservations/" + created.getId()))
                .body(created);
    }

    // READ
    @Operation(summary = "Obtener reserva por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBookingDto> getById(@PathVariable Long id) {
        ResponseBookingDto dto = bookingService.getBookingById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Listar mis reservas (huésped autenticado)")
    @GetMapping
    public ResponseEntity<List<ResponseBookingDto>> listMyBookings(
            Authentication authentication,
            @RequestParam(required = false) StatusReservation status,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        List<ResponseBookingDto> list = bookingService.getBookingsByGuest(guestId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Listar reservas por alojamiento (vista anfitrión)")
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<List<ResponseBookingDto>> listByAccommodation(
            @PathVariable Long accommodationId,
            Authentication authentication
    ) {
        extractUserIdFromAuthentication(authentication); // fuerza autenticación válida
        List<ResponseBookingDto> list = bookingService.getBookingsByAccommodation(accommodationId);
        return ResponseEntity.ok(list);
    }

    // UPDATE (datos)
    @Operation(summary = "Actualizar reserva (solo estado PENDING)")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseBookingDto> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingDto updateDto,
            Authentication authentication
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseBookingDto updated = bookingService.updateBooking(id, updateDto, guestId);
        return ResponseEntity.ok(updated);
    }

    // STATE TRANSITIONS (host)
    @Operation(summary = "Cambiar estado (host): confirmar o rechazar")
    @PutMapping("/{id}/state")
    public ResponseEntity<ResponseBookingDto> changeState(
            @PathVariable Long id,
            @RequestParam StatusReservation state,           // CONFIRMED o CANCELED
            @RequestParam(required = false) String reason,   // razón opcional
            Authentication authentication
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);

        ResponseBookingDto result;
        if (state == StatusReservation.CONFIRMED || state == StatusReservation.PAID) {
            result = bookingService.confirmBooking(id, hostId);
        } else if (state == StatusReservation.CANCELED) {
            result = bookingService.rejectBooking(id, reason, hostId);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(result);
    }

    // CANCEL (guest)
    @Operation(summary = "Cancelar mi reserva (huésped)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseBookingDto> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequestDto cancelDto,
            Authentication authentication
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseBookingDto canceled = bookingService.cancelBooking(id, cancelDto, guestId);
        return ResponseEntity.ok(canceled);
    }

    // COMPLETE (system/host) — opcional
    @Operation(summary = "Marcar reserva como completada (opcional)")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ResponseBookingDto> completeBooking(@PathVariable Long id) {
        ResponseBookingDto completed = bookingService.completeBooking(id);
        return ResponseEntity.ok(completed);
    }

    // Helpers
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String s) {
            return Long.valueOf(s);
        }
        throw new RuntimeException("No se pudo extraer el ID del usuario");
    }
}
