package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;

public interface AccommodationService {

    ResponseAccommodationDto createAccommodation(RequestAccommodationDto requestAccommodationDto, Long idHost);

    ResponseAccommodationDto updateAccommodation(Long id, RequestAccommodationDto requestAccommodationDto);

    void deleteAccommodation(Long id);

    AccommodationEntity getAccommodationById(Long id);
}
