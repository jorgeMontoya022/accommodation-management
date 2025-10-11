package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.AccommodationServicesImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.AccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accommodation")
@Tag(name = "Alojamientos", description = "Endpoints para la gestión de alojamientos")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AccommodationController {

    //  Usar la implementación concreta del servicio
    private final AccommodationServicesImpl accommodationService;

    //  Inyectar el mapper
    private final AccommodationMapper accommodationMapper;

    private final JWTUtils jwtUtils;

    // CREATE
    @Operation(summary = "Crear alojamiento",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alojamiento creado",
                            content = @Content(schema = @Schema(implementation = ResponseAccommodationDto.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            })
    @PostMapping
    public ResponseEntity<ResponseAccommodationDto> createAccommodation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody RequestAccommodationDto requestDto
    ) {
        Long hostId = extractUserIdFromAuthorization(authorization);
        ResponseAccommodationDto created = accommodationService.createAccommodation(requestDto, hostId);
        return ResponseEntity
                .created(URI.create("/api/v1/accommodation/" + created.getId()))
                .body(created);
    }

    // READ
    @Operation(summary = "Obtener detalles del alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalles del alojamiento",
                            content = @Content(schema = @Schema(implementation = ResponseAccommodationDto.class))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseAccommodationDto> getById(@PathVariable Long id) {
        AccommodationEntity entity = accommodationService.getAccommodationById(id);
        return ResponseEntity.ok(accommodationMapper.accommodationEntityToAccommodationDto(entity));
    }

    @Operation(summary = "Listar alojamientos por ciudad")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<ResponseAccommodationDto>> getByCity(@PathVariable String city) {
        List<ResponseAccommodationDto> list = accommodationService.getAccommodationsByCity(city);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Búsqueda paginada de alojamientos (filtros próximamente)")
    @GetMapping
    public ResponseEntity<Page<ResponseAccommodationDto>> searchPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ResponseAccommodationDto> result = accommodationService.searchWithFilters(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Listar alojamientos del anfitrión autenticado (paginado)")
    @GetMapping("/host")
    public ResponseEntity<Page<ResponseAccommodationDto>> listByHost(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long hostId = extractUserIdFromAuthorization(authorization);
        Pageable pageable = PageRequest.of(page, size);
        Page<ResponseAccommodationDto> result = accommodationService.listByHost(hostId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener URL de la imagen principal del alojamiento")
    @GetMapping("/{id}/main-image")
    public ResponseEntity<String> getMainImageUrl(@PathVariable Long id) {
        String url = accommodationService.getMainImageUrl(id);
        return ResponseEntity.ok(url);
    }

    // UPDATE
    @Operation(summary = "Editar alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento actualizado",
                            content = @Content(schema = @Schema(implementation = ResponseAccommodationDto.class))),
                    @ApiResponse(responseCode = "403", description = "No es propietario"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no existe"),
                    @ApiResponse(responseCode = "409", description = "No es posible actualizar, existen reservas futuras")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseAccommodationDto> updateAccommodation(
            @PathVariable Long id,
            @Valid @RequestBody RequestAccommodationDto requestDto
    ) {
        ResponseAccommodationDto updated = accommodationService.updateAccommodation(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    // DELETE (soft delete)
    @Operation(summary = "Eliminar alojamiento (soft delete)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alojamiento marcado como eliminado"),
                    @ApiResponse(responseCode = "403", description = "No es propietario"),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no existe"),
                    @ApiResponse(responseCode = "409", description = "No es posible eliminar, existen reservas futuras")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.ok().build();
    }

    // Helpers
    private Long extractUserIdFromAuthorization(String authorization) {
        String token = (authorization != null && authorization.startsWith("Bearer "))
                ? authorization.substring(7)
                : authorization;
        String subject = jwtUtils.parseJwt(token).getPayload().getSubject();
        return Long.valueOf(subject);
    }
}
