package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.GuestServiceImpl; // ✅ usar la impl
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
 * - Rutas públicas: registro y verificación de email.
 * - Rutas /me requieren autenticación; el userId se extrae desde Authentication.getPrincipal().
 */
@RestController
@RequestMapping("/api/v1/guests")
@Tag(name = "Huéspedes", description = "Endpoints para la gestión de huéspedes")
@RequiredArgsConstructor
public class GuestController {

    // Inyectar la implementación concreta
    private final GuestServiceImpl guestService;

    // PUBLIC — Registro
    @Operation(summary = "Registrar huésped")
    @PostMapping
    public ResponseEntity<ResponseGuestDto> createGuest(@Valid @RequestBody RequestGuestDto request) {
        ResponseGuestDto created = guestService.createGuest(request);
        return ResponseEntity
                .created(URI.create("/api/v1/guests/" + created.getId()))
                .body(created);
    }
    // SECURED — Obtener huésped por ID (uso anfitrión)
    @Operation(summary = "Obtener huésped por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGuestDto> getGuestById(@PathVariable Long id) {
        ResponseGuestDto dto = guestService.getGuestById(id);
        return ResponseEntity.ok(dto);
    }

    // PUBLIC — Verificación de email
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

    // SECURED — Actualizar perfil
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

    // SECURED — Eliminar cuenta (soft delete/inactivar)
    @Operation(summary = "Eliminar mi cuenta", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        guestService.deleteGuest(guestId);
        return ResponseEntity.ok().build();
    }

    // SECURED — Cambiar contraseña
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

    // SECURED — Métricas
    @Operation(summary = "Mis métricas: número de reservas", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/stats/bookings")
    public ResponseEntity<Map<String, Long>> getMyBookingCount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        Long count = guestService.getGuestBookingCount(guestId);
        return ResponseEntity.ok(Map.of("bookings", count));
    }

    @Operation(summary = "Mi estado (activo/inactivo)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/active")
    public ResponseEntity<Map<String, Boolean>> isActive(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        boolean active = guestService.isActiveGuest(guestId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    // Helper
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
