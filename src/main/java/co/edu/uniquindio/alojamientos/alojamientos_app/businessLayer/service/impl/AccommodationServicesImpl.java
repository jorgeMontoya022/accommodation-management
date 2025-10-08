package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;


import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.AccommodationService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BookingService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.AccommodationDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.StatusAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.AccommodationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccommodationServicesImpl implements AccommodationService {
    private final AccommodationDao accommodationDao;
    private final HostService hostService;
    private final AccommodationMapper accommodationMapper;


    @Override
    public ResponseAccommodationDto createAccommodation(
            RequestAccommodationDto requestAccommodationDto,
            Long authenticatedHostId) {

        log.info("Creando alojamiento para host ID: {}", authenticatedHostId);

        // 1. Obtener y validar host
        HostEntity host = hostService.getHostEntityById(authenticatedHostId);

        if (!host.isActive()) {
            throw new RuntimeException("El anfitrión no está activo");
        }

        AccommodationEntity accommodation = accommodationMapper
                .accommodationDtoToAccommodationEntity(requestAccommodationDto);

        // 3. Establecer campos que no vienen del request
        accommodation.setHostEntity(host);
        accommodation.setStatusAccommodation(StatusAccommodation.ACTIVE);


        validateAccommodationRules(accommodation);

        // 5. Guardar (@PrePersist automáticamente asigna dateCreation)
        AccommodationEntity saved = accommodationDao.saveEntity(accommodation);

        log.info("Alojamiento creado exitosamente con ID: {}", saved.getId());

        return accommodationMapper.accommodationEntityToAccommodationDto(saved);
    }

    private void validateAccommodationRules(AccommodationEntity accommodation) {

        if (accommodation.getPriceNight() > 10_000_000) {
            throw new IllegalArgumentException("El precio por noche excede el máximo permitido");
        }

    }

    @Override
    public ResponseAccommodationDto updateAccommodation(Long id, RequestAccommodationDto requestAccommodationDto) {
        log.info("Actualizando alojamiento ID: {}", id);

        AccommodationEntity accommodation = accommodationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        if (accommodation.getStatusAccommodation() == StatusAccommodation.DELETED) {
            throw new RuntimeException("No se puede actualizar un alojamiento eliminado");
        }

        accommodationMapper.updateEntityFromDto(requestAccommodationDto, accommodation);

        AccommodationEntity updated = accommodationDao.updateEntity(accommodation);

        log.info("Alojamiento actualizado exitosamente con ID: {}", id);

        return accommodationMapper.accommodationEntityToAccommodationDto(updated);
    }

    @Override
    public void deleteAccommodation(Long id) {

        AccommodationEntity accommodation = accommodationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el alojamiento"));

        Long bookingCount = accommodationDao.countBookingsByAccommodationId(id);

        if(bookingCount > 0) {
            throw new IllegalArgumentException(
                    String.format("No se puede eliminar el alojamiento porque tiene %d reserva(s) activa(s)", bookingCount)
            );
        }

        accommodation.setStatusAccommodation(StatusAccommodation.DELETED);
        accommodation.setDateUpdate(LocalDateTime.now());

        accommodationDao.save(accommodation);

        log.info("Alojamiento eliminado (soft delete) con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationEntity getAccommodationById(Long id) {
        return accommodationDao.findById(id)
                .orElseThrow(()-> {
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

}
