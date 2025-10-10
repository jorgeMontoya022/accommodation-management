package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.AccommodationService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.AccommodationDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.AccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccommodationServicesImpl implements AccommodationService {

    private final AccommodationDao accommodationDao;
    private final HostService hostService;
    private final AccommodationMapper accommodationMapper;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;

    @Override
    public ResponseAccommodationDto createAccommodation(
            RequestAccommodationDto requestAccommodationDto,
            Long authenticatedHostId) {

        log.info("Creando alojamiento para host ID: {}", authenticatedHostId);

        // 1) Validar host
        HostEntity host = hostService.getHostEntityById(authenticatedHostId);
        if (!host.isActive()) {
            throw new IllegalArgumentException("El anfitrión no está activo");
        }

        // 2) Mapear DTO -> Entity
        AccommodationEntity accommodation =
                accommodationMapper.accommodationDtoToAccommodationEntity(requestAccommodationDto);

        // 3) Setear datos de dominio que no vienen en el request
        accommodation.setHostEntity(host);
        accommodation.setStatusAccommodation(StatusAccommodation.ACTIVE);

        // 4) Reglas de negocio
        validateAccommodationRules(accommodation);

        // 5) Persistir
        AccommodationEntity saved = accommodationDao.saveEntity(accommodation);

        log.info("Alojamiento creado exitosamente con ID: {}", saved.getId());
        return accommodationMapper.accommodationEntityToAccommodationDto(saved);
    }

    /** Reglas de negocio del alojamiento */
    private void validateAccommodationRules(AccommodationEntity accommodation) {
        // Precio techo
        if (accommodation.getPriceNight() > 10_000_000) {
            throw new IllegalArgumentException("El precio por noche excede el máximo permitido");
        }
        // Capacidad válida
        if (accommodation.getMaximumCapacity() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0");
        }
    }

    @Override
    public ResponseAccommodationDto updateAccommodation(Long id, RequestAccommodationDto requestAccommodationDto) {
        log.info("Actualizando alojamiento ID: {}", id);

        AccommodationEntity accommodation = accommodationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        // No permitir actualizar si está marcado como eliminado (soft delete)
        if (accommodation.isDeleted()) {
            throw new IllegalStateException("No se puede actualizar un alojamiento eliminado");
        }

        // MapStruct actualiza en sitio
        accommodationMapper.updateEntityFromDto(requestAccommodationDto, accommodation);

        // @PreUpdate setea dateUpdate
        AccommodationEntity updated = accommodationDao.updateEntity(accommodation);

        log.info("Alojamiento actualizado exitosamente con ID: {}", id);
        return accommodationMapper.accommodationEntityToAccommodationDto(updated);
    }

    @Override
    public void deleteAccommodation(Long id) {
        if(!accommodationDao.findById(id).isPresent()) {
            throw new RuntimeException("No se encontró el alojamiento");
        }
        Long bookingCount = accommodationDao.countBookingsByAccommodationId(id);

        if(bookingCount > 0) {
            throw new IllegalArgumentException(String.format("No se puede eliminar el alojamiento porque tiene %d reserva(s) asociada(s)", bookingCount));
        }
        if(!accommodationDao.deleteById(id)) {
            throw new RuntimeException("Error al eliminar el alojamiento con ID: " + id);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public AccommodationEntity getAccommodationById(Long id) {
        return accommodationDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Alojamiento no encontrado con ID: {}", id);
                    return new RuntimeException("Alojamiento no encontrado con ID: " + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseAccommodationDto> getAccommodationsByCity(String city) {
        log.info("Buscando alojamientos en la ciudad: {}", city);

        List<AccommodationEntity> accommodations = accommodationDao.findByCity(city);

        if (accommodations.isEmpty()) {
            log.info("No se encontraron alojamientos en la ciudad: {}", city);
        }

        return accommodationMapper.getAccommodationsDto(accommodations);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResponseAccommodationDto> searchWithFilters(Pageable pageable) {
        // Implementación mínima: listado paginado sin filtros.
        // Próximo paso: Specifications para filtros combinables.
        Page<AccommodationEntity> page = accommodationRepository.findAll(pageable);
        return page.map(accommodationMapper::accommodationEntityToAccommodationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResponseAccommodationDto> listByHost(Long hostId, Pageable pageable) {
        // Lista paginada de alojamientos del host (respetando soft delete)
        Page<AccommodationEntity> page =
                accommodationRepository.findAllByHostEntity_IdAndDeletedFalse(hostId, pageable);
        return page.map(accommodationMapper::accommodationEntityToAccommodationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMainImageUrl(Long accommodationId) {
        // Cargar el alojamiento con su galería para evitar N+1
        AccommodationEntity accommodation = accommodationRepository
                .fetchWithImagesById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado con ID: " + accommodationId));

        // Regla de galería: solo una principal; si no hay principal, devolver null/"".
        return accommodation.getImages().stream()
                .filter(ImageAccommodation::isPrincipal)
                .sorted(Comparator.comparingInt(ImageAccommodation::getDisplayOrder))
                .map(ImageAccommodation::getUrl)
                .findFirst()
                .orElse(null); // o "", según prefieras
    }
}
