package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

/**
 * Controlador REST para gestión de Huéspedes.
 * - Algunas rutas son públicas (registro, verificación de email).
 * - Las rutas /me requieren autenticación; el userId se extrae desde Authentication.getPrincipal().
 */
@RestController
@RequestMapping("/api/v1/guests")
@Tag(name = "Huéspedes", description = "Endpoints para la gestión de huéspedes")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    // PUBLIC — Registro

    /**
     * Crea un nuevo huésped.
     * Validaciones de negocio: email único, formatos, reglas específicas (en el service).
     */
    @Operation(summary = "Registrar huésped")
    @PostMapping
    public ResponseEntity<ResponseGuestDto> createGuest(@Valid @RequestBody RequestGuestDto request) {
        ResponseGuestDto created = guestService.createGuest(request);
        return ResponseEntity
                .created(URI.create("/api/v1/guests/" + created.getId()))
                .body(created);
    }

    // PUBLIC — Verificación de email

    /**
     * Verifica disponibilidad de un email.
     * Retorna {"available": true/false}
     */
    @Operation(summary = "Verificar disponibilidad de email")
    @GetMapping("/email-availability")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean taken = guestService.isEmailTaken(email);
        return ResponseEntity.ok(Map.of("available", !taken));
    }

    // SECURED — Perfil del huésped autenticado

    @Operation(summary = "Obtener mi perfil", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ResponseGuestDto> getMyProfile(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseGuestDto dto = guestService.getGuestById(guestId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Actualiza datos del huésped autenticado.
     * Campos permitidos y validaciones gestionados por UpdateGuestDto + service.
     */
    @Operation(summary = "Actualizar mi perfil", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public ResponseEntity<ResponseGuestDto> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateGuestDto request
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseGuestDto updated = guestService.updateGuest(guestId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina la cuenta del huésped autenticado.
     * Reglas de negocio (reservas asociadas, etc.) se validan en el service.
     */
    @Operation(summary = "Eliminar mi cuenta", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        guestService.deleteGuest(guestId);
        return ResponseEntity.ok().build();
    }

    // SECURED — Contraseña

    /**
     * Cambiar la contraseña del huésped autenticado.
     * Valida currentPassword vs almacenada, y reglas de complejidad en el service.
     */
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
        Long guestId = extractUserIdFromAuthentication(authentication);
        guestService.changePassword(guestId, request);
        return ResponseEntity.ok().build();
    }

    // SECURED — Métricas básicas

    /**
     * Devuelve el número de reservas asociadas al huésped autenticado.
     */
    @Operation(summary = "Mis métricas: número de reservas", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/stats/bookings")
    public ResponseEntity<Map<String, Long>> getMyBookingCount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        Long count = guestService.getGuestBookingCount(guestId);
        return ResponseEntity.ok(Map.of("bookings", count));
    }

    /**
     * Indica si el huésped autenticado está activo.
     */
    @Operation(summary = "Mi estado (activo/inactivo)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/active")
    public ResponseEntity<Map<String, Boolean>> isActive(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        boolean active = guestService.isActiveGuest(guestId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    // Helpers

    // Extrae el id del usuario desde Authentication (igual patrón que en tus otros controllers).
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        //TODO: revisar que está duplicada
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
