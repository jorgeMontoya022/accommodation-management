package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.AccommodationService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Anfitriones (Host).
 * - Rutas públicas: registro y verificación de email.
 * - Rutas /me: requieren autenticación; el userId se extrae del Authentication.
 */
@RestController
@RequestMapping("/api/v1/hosts")
@Tag(name = "Anfitriones", description = "Endpoints para la gestión de anfitriones")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;
    private final AccommodationService accommodationService;
    private final BookingService bookingService;

    // PUBLIC — Registro

    @Operation(summary = "Registrar anfitrión")
    @PostMapping
    public ResponseEntity<ResponseHostDto> createHost(@Valid @RequestBody RequestHostDto request) {
        ResponseHostDto created = hostService.createHost(request);
        return ResponseEntity
                .created(URI.create("/api/v1/hosts/" + created.getId()))
                .body(created);
    }

    // PUBLIC — Verificación de email

    @Operation(summary = "Verificar disponibilidad de email")
    @GetMapping("/email-availability")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean taken = hostService.isEmailTaken(email);
        return ResponseEntity.ok(Map.of("available", !taken));
    }

    // SECURED — Perfil del host autenticado

    @Operation(summary = "Obtener mi perfil", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ResponseHostDto> getMyProfile(Authentication authentication) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        ResponseHostDto dto = hostService.getHostById(hostId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Actualizar mi perfil", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public ResponseEntity<ResponseHostDto> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateHostDto request
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        ResponseHostDto updated = hostService.updateHost(hostId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar mi cuenta", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        hostService.deleteHost(hostId);
        return ResponseEntity.ok().build();
    }

    // SECURED — Contraseña

    @Operation(
            summary = "Cambiar mi contraseña",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema()))
            }
    )
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDto request
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        hostService.changePassword(hostId, request);
        return ResponseEntity.ok().build();
    }

    // SECURED — Métricas y estado

    @Operation(summary = "Mis métricas: número de alojamientos", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/stats/accommodations-count")
    public ResponseEntity<Map<String, Long>> getMyAccommodationsCount(Authentication authentication) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        Long count = hostService.getHostAccommodationCount(hostId);
        return ResponseEntity.ok(Map.of("accommodations", count));
    }

    @Operation(summary = "Mi estado (activo/inactivo)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/active")
    public ResponseEntity<Map<String, Boolean>> isActive(Authentication authentication) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        boolean active = hostService.isActiveHost(hostId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    // SECURED — Mis alojamientos

    @Operation(summary = "Listar mis alojamientos (paginado)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/accommodations")
    public ResponseEntity<Page<ResponseAccommodationDto>> listMyAccommodations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<ResponseAccommodationDto> result = accommodationService.listByHost(hostId, pageable);
        return ResponseEntity.ok(result);
    }

    // SECURED — Reservas de un alojamiento mío

    /**
     * Lista las reservas de un alojamiento del host autenticado.
     * La validación de propiedad del alojamiento debe hacerse en el service.
     */
    @Operation(summary = "Listar reservas de un alojamiento mío", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/accommodations/{accommodationId}/bookings")
    public ResponseEntity<List<ResponseBookingDto>> listBookingsOfMyAccommodation(
            Authentication authentication,
            @PathVariable Long accommodationId
    ) {
        Long hostId = extractUserIdFromAuthentication(authentication);
        // el service debe validar que accommodationId pertenece a hostId
        List<ResponseBookingDto> list = bookingService.getBookingsByAccommodation(accommodationId);
        return ResponseEntity.ok(list);
    }

    // Helper

    // Extrae el id del usuario desde Authentication.
    private Long extractUserIdFromAuthentication(Authentication authentication) {

        //TODO: verificar que está repetido
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
