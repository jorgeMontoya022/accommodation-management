package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ImageAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.ImageAccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.ImageAccommodationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accommodation")
@Tag(name = "Imágenes de alojamientos", description = "Gestión de imágenes de un alojamiento")
@RequiredArgsConstructor
public class ImageAccommodationController {

    private final AccommodationRepository accommodationRepository;
    private final ImageAccommodationRepository imageAccommodationRepository;
    private final ImageAccommodationMapper imageAccommodationMapper;

    // ----------------------------------------------------------------
    //  POST /api/v1/accommodation/{id}/images  -> añadir imagen
    // ----------------------------------------------------------------
    @Operation(
            summary = "Añadir imagen a un alojamiento",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Imagen creada",
                            content = @Content(schema = @Schema(implementation = ImageAccommodationDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            }
    )
    @PostMapping("/{id}/images")
    public ResponseEntity<ImageAccommodationDto> addImageToAccommodation(
            @PathVariable("id") Long accommodationId,
            @Valid @RequestBody ImageAccommodationDto dto
    ) {
        // 1. Verificar que el alojamiento existe
        AccommodationEntity accommodation = accommodationRepository.findByIdAndDeletedFalse(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + accommodationId));

        // 2. Mapear DTO -> Entity (sin relación aún)
        ImageAccommodation image = imageAccommodationMapper.imageAccommodationDtoToImageAccommodationEntity(dto);

        // 3. Asociar al alojamiento
        image.setAccommodationEntity(accommodation);

        // 4. Si no viene displayOrder, lo calculamos (max + 1)
        if (image.getDisplayOrder() == null) {
            int maxOrder = imageAccommodationRepository.findMaxDisplayOrderByAccommodationId(accommodationId);
            image.setDisplayOrder(maxOrder + 1);
        }

        // 5. Si es principal, desmarcamos las demás como principales
        if (image.isPrincipal()) {
            imageAccommodationRepository.unsetPrincipalForAccommodation(accommodationId, null);
        }

        // 6. Guardar
        ImageAccommodation saved = imageAccommodationRepository.save(image);

        // 7. Mapear a DTO de respuesta
        ImageAccommodationDto response = imageAccommodationMapper.imageAccommodationEntityToImageAccommodationDto(saved);

        return ResponseEntity
                .created(URI.create("/api/v1/accommodation/" + accommodationId + "/images/" + saved.getId()))
                .body(response);
    }

    // ----------------------------------------------------------------
    //  GET /api/v1/accommodation/{id}/images  -> listar imágenes
    // ----------------------------------------------------------------
    @Operation(
            summary = "Listar imágenes de un alojamiento",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de imágenes",
                            content = @Content(schema = @Schema(implementation = ImageAccommodationDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
            }
    )
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageAccommodationDto>> listImagesByAccommodation(
            @PathVariable("id") Long accommodationId
    ) {
        // Validamos que el alojamiento exista (opcional, pero mejor mensajes claros)
        accommodationRepository.findByIdAndDeletedFalse(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + accommodationId));

        List<ImageAccommodation> images =
                imageAccommodationRepository.findByAccommodationEntity_IdOrderByDisplayOrderAsc(accommodationId);

        List<ImageAccommodationDto> dtoList =
                imageAccommodationMapper.getImageAccommodationsDto(images);

        return ResponseEntity.ok(dtoList);
    }
}
