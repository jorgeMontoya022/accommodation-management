package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ImageAccommodationDto;

import java.util.List;

/**
 * Servicio para gestionar la galería de imágenes de un alojamiento.
 * Reglas:
 *  - Máximo 6 imágenes por alojamiento.
 *  - Solo 1 imagen principal por alojamiento.
 *  - Ordenamiento por displayOrder (1..n).
 */
public interface ImageAccommodationService {

    /** Lista la galería completa (orden ascendente). */
    List<ImageAccommodationDto> listGallery(Long accommodationId);

    /** Agrega una imagen (opcionalmente principal), asignando displayOrder = max+1. */
    ImageAccommodationDto addImage(ImageAccommodationDto request);

    /** Marca una imagen como principal y desmarca las demás. */
    void setAsPrincipal(Long accommodationId, Long imageId);

    /** Reordena la galería según una lista de IDs. */
    void reorder(Long accommodationId, List<Long> imageIdsInOrder);

    /** Elimina una imagen (si era principal, puede promover otra según política). */
    void removeImage(Long accommodationId, Long imageId);

    /** Reemplaza la galería completa. Aplica las mismas validaciones. */
    void replaceGallery(Long accommodationId, List<ImageAccommodationDto> newImages);

    /** Utilidad: URL de la imagen principal (o null). */
    String getMainImageUrl(Long accommodationId);
}
