package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.AccommodationService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final JWTUtils jwtUtils;

    public AccommodationController(AccommodationService accommodationService, JWTUtils jwtUtils) {
        this.accommodationService = accommodationService;
        this.jwtUtils = jwtUtils;
    }

    // CREATE

    /**
     * Crea un alojamiento.
     * Regla RN-A-01: campos obligatorios
     * Regla RN-A-02: validación de imágenes (si aplica, en el service)
     * Regla RN-A-05: el anfitrión solo gestiona sus alojamientos (en service/seguridad)
     */
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
        // Extraer id del host autenticado desde el JWT, ignorando el idHost del body para evitar suplantación.
        Long hostId = extractUserIdFromAuthorization(authorization);

        ResponseAccommodationDto created = accommodationService.createAccommodation(requestDto, hostId);
        // Location: /api/v1/accommodation/{id}
        return ResponseEntity
                .created(URI.create("/api/v1/accommodation/" + created.getId()))
                .body(created);
    }

    // READ

    /**
     * Obtiene un alojamiento por su ID.
     * Aplica RN-S-04: excluir eliminados (lo maneja @Where en la entidad).
     */
    @Operation(summary = "Obtener detalles del alojamiento",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalles del alojamiento",
                            content = @Content(schema = @Schema(implementation = ResponseAccommodationDto.class))),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseAccommodationDto> getById(@PathVariable Long id) {
        AccommodationEntity entity = accommodationService.getAccommodationById(id);
        return ResponseEntity.ok(mapToDto(entity));
    }

    /**
     * Lista alojamientos por ciudad (búsqueda simple).
     * Relacionado con US-020 autocompletar/ciudad y RN-S-02 filtros combinables (aquí solo ciudad).
     */
    @Operation(summary = "Listar alojamientos por ciudad")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<ResponseAccommodationDto>> getByCity(@PathVariable String city) {
        List<ResponseAccommodationDto> list = accommodationService.getAccommodationsByCity(city);
        return ResponseEntity.ok(list);
    }

    /**
     * Búsqueda paginada genérica (placeholder para Specifications/filtros).
     * RN-S-03: Paginación por defecto (10 por página).
     * Nota: aún no hay AccommodationFilterDto activo, solo exponemos la paginación.
     */
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

    /**
     * Lista paginada de alojamientos por anfitrión autenticado.
     * RN-A-05: Un anfitrión solo gestiona sus alojamientos.
     */
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

    /**
     * Devuelve la URL de la imagen principal del alojamiento (si aplica en tu front).
     */
    @Operation(summary = "Obtener URL de la imagen principal del alojamiento")
    @GetMapping("/{id}/main-image")
    public ResponseEntity<String> getMainImageUrl(@PathVariable Long id) {
        String url = accommodationService.getMainImageUrl(id);
        return ResponseEntity.ok(url);
    }

    // UPDATE

    /**
     * Actualiza un alojamiento.
     * RN-A-04: No actualizar/eliminar si hay reservas futuras (validación en el service).
     * RN-A-05: Validar propiedad del alojamiento (service/seguridad).
     */
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

    /**
     * Elimina (soft delete) un alojamiento.
     * RN-A-03: soft delete.
     * RN-A-04: No eliminar si hay reservas futuras (service).
     * RN-A-05: Validar propiedad (service/seguridad).
     */
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

    // Extrae el id del usuario (anfitrión) del token JWT (claim 'sub')
    private Long extractUserIdFromAuthorization(String authorization) {
        // El header llega como "Bearer <token>"
        String token = (authorization != null && authorization.startsWith("Bearer "))
                ? authorization.substring(7)
                : authorization;

        String subject = jwtUtils.parseJwt(token).getPayload().getSubject();
        return Long.valueOf(subject); // subject debe ser el id del usuario/anfitrión
    }

    // Mapper mínimo para no depender de MapStruct aquí
    private ResponseAccommodationDto mapToDto(AccommodationEntity e) {
        ResponseAccommodationDto dto = new ResponseAccommodationDto();
        dto.setId(e.getId());
        dto.setQualification(e.getQualification());
        dto.setDescription(e.getDescription());
        dto.setCity(e.getCity());
        dto.setLatitude(e.getLatitude());
        dto.setLongitude(e.getLongitude());
        dto.setPriceNight(e.getPriceNight());
        dto.setMaximumCapacity(e.getMaximumCapacity());
        dto.setDateCreation(e.getDateCreation());
        dto.setStatusAccommodation(e.getStatusAccommodation());
        dto.setTypeServicesEnum(e.getTypeServicesEnum());
        dto.setIdHost(
                e.getHostEntity() != null ? String.valueOf(e.getHostEntity().getId()) : null
        );
        return dto;
    }
}
