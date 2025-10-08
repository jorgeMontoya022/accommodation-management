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
    public ResponseAccommodationDto createAccommodation(RequestAccommodationDto requestAccommodationDto, Long authenticatedHostId) {
        HostEntity host = hostService.getHostEntityById(authenticatedHostId);

        if (!host.isActive()) {
            throw new RuntimeException("El anfitrión no existe");
        }
        AccommodationEntity accommodation = accommodationMapper.accommodationDtoToAccommodationEntity(requestAccommodationDto);
        accommodation.setHostEntity(host);
        accommodation.setDateCreation(LocalDateTime.now());
        accommodation.setStatusAccommodation(StatusAccommodation.ACTIVE);
        validateAccommodationRules(accommodation);

        return accommodationDao.save(accommodation);


    }

    private void validateAccommodationRules(AccommodationEntity accommodation) {

        if (accommodation.getPriceNight() > 10_000_000) {
            throw new IllegalArgumentException("El precio por noche excede el máximo permitido");
        }

    }

    @Override
    public ResponseAccommodationDto updateAccommodation(Long id, RequestAccommodationDto requestAccommodationDto) {
        if(!accommodationDao.findById(id).isPresent()) {
            throw new RuntimeException("Alojamiento no encontrado");

        }
        return accommodationDao.update(id, requestAccommodationDto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar al anfitrión"));
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
                .orElseThrow(()-> {
                    log.warn("Alojamiento no encontrado con ID: {}", id);
                    return new RuntimeException("Alojamiento no encontrado con ID: " + id);
                });
    }


}
