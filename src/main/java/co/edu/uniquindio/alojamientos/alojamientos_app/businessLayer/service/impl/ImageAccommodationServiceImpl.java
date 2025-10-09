package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ImageAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BusinessException;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.ImageAccommodationService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.ImageAccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.ImageAccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Implementación de reglas para la galería de imágenes */
@Service
@RequiredArgsConstructor
public class ImageAccommodationServiceImpl implements ImageAccommodationService {

    private static final int MAX_IMAGES = 6;

    private final ImageAccommodationRepository imageRepo;
    private final AccommodationRepository accommodationRepo;
    private final ImageAccommodationMapper mapper;

    /** Obtiene alojamiento o lanza error si no existe */
    private AccommodationEntity getAccommodationRef(Long accommodationId) {
        return accommodationRepo.findById(accommodationId)
                .orElseThrow(() -> new BusinessException("El alojamiento no existe: id=" + accommodationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageAccommodationDto> listGallery(Long accommodationId) {
        var entities = imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(accommodationId);
        return mapper.getImageAccommodationsDto(entities);
    }

    @Override
    @Transactional
    public ImageAccommodationDto addImage(ImageAccommodationDto request) {
        if (request == null || request.getIdAccommodation() == null) {
            throw new BusinessException("Debe indicar el id del alojamiento");
        }
        if (request.getUrl() == null || request.getUrl().isBlank()) {
            throw new BusinessException("La URL de la imagen es obligatoria");
        }

        Long accommodationId = request.getIdAccommodation();
        var accommodation = getAccommodationRef(accommodationId);

        long count = imageRepo.countByAccommodationEntity_Id(accommodationId);
        if (count >= MAX_IMAGES) {
            throw new BusinessException("El alojamiento ya tiene el máximo de " + MAX_IMAGES + " imágenes permitidas");
        }

        boolean wantPrincipal = Boolean.TRUE.equals(request.getIsPrincipal());
        if (wantPrincipal && imageRepo.existsByAccommodationEntity_IdAndIsPrincipalTrue(accommodationId)) {
            throw new BusinessException("Ya existe una imagen principal para este alojamiento");
        }

        int nextOrder = imageRepo.findMaxDisplayOrderByAccommodationId(accommodationId) + 1;
        Integer order = request.getDisplayOrder() != null ? request.getDisplayOrder() : nextOrder;

        ImageAccommodation entity = mapper.imageAccommodationDtoToImageAccommodationEntity(request);
        entity.setAccommodationEntity(accommodation);
        entity.setDisplayOrder(order);

        ImageAccommodation saved = imageRepo.save(entity);
        return mapper.imageAccommodationEntityToImageAccommodationDto(saved);
    }

    @Override
    @Transactional
    public void setAsPrincipal(Long accommodationId, Long imageId) {
        ImageAccommodation img = imageRepo.findByIdAndAccommodationEntity_Id(imageId, accommodationId)
                .orElseThrow(() -> new BusinessException("La imagen no pertenece al alojamiento o no existe"));

        imageRepo.unsetPrincipalForAccommodation(accommodationId, img.getId()); // desmarca otras
        img.setPrincipal(true);
        imageRepo.save(img);
    }

    @Override
    @Transactional
    public void reorder(Long accommodationId, List<Long> imageIdsInOrder) {
        if (imageIdsInOrder == null || imageIdsInOrder.isEmpty()) {
            throw new BusinessException("Debe enviar al menos una imagen para reordenar");
        }

        var current = imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(accommodationId);
        if (current.isEmpty()) {
            throw new BusinessException("El alojamiento no tiene imágenes para reordenar");
        }

        Set<Long> currentIds = new HashSet<>();
        current.forEach(i -> currentIds.add(i.getId()));

        if (!currentIds.containsAll(imageIdsInOrder) || imageIdsInOrder.size() != current.size()) {
            throw new BusinessException("La lista de imágenes no coincide con la galería actual");
        }

        int order = 1;
        Map<Long, ImageAccommodation> byId = new HashMap<>();
        current.forEach(i -> byId.put(i.getId(), i));

        for (Long id : imageIdsInOrder) {
            ImageAccommodation img = byId.get(id);
            img.setDisplayOrder(order++);
            imageRepo.save(img);
        }
    }

    @Override
    @Transactional
    public void removeImage(Long accommodationId, Long imageId) {
        ImageAccommodation img = imageRepo.findByIdAndAccommodationEntity_Id(imageId, accommodationId)
                .orElseThrow(() -> new BusinessException("La imagen no pertenece al alojamiento o no existe"));

        boolean wasPrincipal = img.isPrincipal();
        imageRepo.deleteById(img.getId());

        if (wasPrincipal) {
            imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(accommodationId).stream().findFirst()
                    .ifPresent(first -> {
                        first.setPrincipal(true);
                        imageRepo.unsetPrincipalForAccommodation(accommodationId, first.getId());
                        imageRepo.save(first);
                    });
        }
    }

    @Override
    @Transactional
    public void replaceGallery(Long accommodationId, List<ImageAccommodationDto> newImages) {
        getAccommodationRef(accommodationId); // valida existencia

        List<ImageAccommodationDto> imgs = (newImages == null) ? List.of() : newImages;
        if (imgs.size() > MAX_IMAGES) {
            throw new BusinessException("No puede subir más de " + MAX_IMAGES + " imágenes");
        }

        imageRepo.deleteByAccommodationEntity_Id(accommodationId);

        long principals = imgs.stream().filter(dto -> Boolean.TRUE.equals(dto.getIsPrincipal())).count();
        if (principals > 1) {
            throw new BusinessException("Solo se permite una imagen principal");
        }

        int order = 1;
        ImageAccommodation principal = null;

        for (ImageAccommodationDto dto : imgs) {
            if (dto.getUrl() == null || dto.getUrl().isBlank()) {
                throw new BusinessException("La URL de cada imagen es obligatoria");
            }
            ImageAccommodation entity = mapper.imageAccommodationDtoToImageAccommodationEntity(dto);
            entity.setAccommodationEntity(getAccommodationRef(accommodationId));
            entity.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : order++);

            ImageAccommodation saved = imageRepo.save(entity);
            if (saved.isPrincipal()) principal = saved;
        }

        if (!imgs.isEmpty() && principal == null) {
            imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(accommodationId).stream().findFirst()
                    .ifPresent(first -> {
                        first.setPrincipal(true);
                        imageRepo.unsetPrincipalForAccommodation(accommodationId, first.getId());
                        imageRepo.save(first);
                    });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getMainImageUrl(Long accommodationId) {
        return imageRepo.findFirstByAccommodationEntity_IdAndIsPrincipalTrue(accommodationId)
                .map(ImageAccommodation::getUrl)
                .orElse(null);
    }
}
