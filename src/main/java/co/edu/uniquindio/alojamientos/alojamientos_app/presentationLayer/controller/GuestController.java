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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    private final GuestService guestService;

    // PUBLIC — Registro
    @Operation(
            summary = "Registrar nuevo huésped",
            description = "Crea una nueva cuenta de huésped. El ID será generado automáticamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Huésped creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseGuestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o email duplicado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping
    public ResponseEntity<ResponseGuestDto> createGuest(@Valid @RequestBody RequestGuestDto request) {
        ResponseGuestDto created = guestService.createGuest(request);
        return ResponseEntity
                .created(URI.create("/api/v1/guests/" + created.getId()))
                .body(created);
    }

    // PUBLIC — Verificación de email
    @Operation(
            summary = "Verificar disponibilidad de email",
            description = "Verifica si un email está disponible o ya está registrado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad del email",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"available\": true}")
                    )
            )
    })
    @GetMapping("/email-availability")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean taken = guestService.isEmailTaken(email);
        return ResponseEntity.ok(Map.of("available", !taken));
    }

    // SECURED — Perfil del huésped autenticado
    @Operation(
            summary = "Obtener mi perfil",
            description = "Obtiene la información del perfil del huésped autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil del huésped",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseGuestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseGuestDto> getMyProfile(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        ResponseGuestDto dto = guestService.getGuestById(guestId);
        return ResponseEntity.ok(dto);
    }

    // SECURED — Actualizar perfil
    @Operation(
            summary = "Actualizar mi perfil",
            description = "Actualiza la información del perfil del huésped autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseGuestDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
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
    @Operation(
            summary = "Eliminar mi cuenta",
            description = "Inactiva la cuenta del huésped (soft delete). La cuenta no se elimina permanentemente.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cuenta eliminada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        guestService.deleteGuest(guestId);
        return ResponseEntity.ok().build();
    }

    // SECURED — Cambiar contraseña
    @Operation(
            summary = "Cambiar mi contraseña",
            description = "Cambia la contraseña del huésped autenticado. Debe proporcionar la contraseña actual.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o contraseña actual incorrecta"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDto request
    ) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        guestService.changePassword(guestId, request);
        return ResponseEntity.ok().build();
    }

    // SECURED — Métricas: número de reservas
    @Operation(
            summary = "Obtener número de mis reservas",
            description = "Retorna la cantidad total de reservas realizadas por el huésped autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cantidad de reservas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"bookings\": 5}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @GetMapping("/me/stats/bookings")
    public ResponseEntity<Map<String, Long>> getMyBookingCount(Authentication authentication) {
        Long guestId = extractUserIdFromAuthentication(authentication);
        Long count = guestService.getGuestBookingCount(guestId);
        return ResponseEntity.ok(Map.of("bookings", count));
    }

    // SECURED — Estado de la cuenta
    @Operation(
            summary = "Verificar si mi cuenta está activa",
            description = "Retorna si la cuenta del huésped autenticado está activa o inactiva",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de la cuenta",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"active\": true}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Huésped no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
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