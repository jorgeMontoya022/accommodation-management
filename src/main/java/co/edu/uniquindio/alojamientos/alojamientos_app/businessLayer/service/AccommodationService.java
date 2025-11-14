package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccommodationService {

    ResponseAccommodationDto createAccommodation(RequestAccommodationDto requestAccommodationDto, Long idHost);

    ResponseAccommodationDto updateAccommodation(Long id, RequestAccommodationDto requestAccommodationDto);

    void deleteAccommodation(Long id, Long hostId); // Validará reservas futuras en la impl

    public ResponseAccommodationDto getAccommodationById(Long id);

    List<ResponseAccommodationDto> getAccommodationsByCity(String city);

    // NUEVOS — útiles para Specifications y para pantallas de host
    Page<ResponseAccommodationDto> searchWithFilters(/* AccommodationFilterDto filter, */ Pageable pageable);

    Page<ResponseAccommodationDto> listByHost(Long hostId, Pageable pageable);

    // (Opcional) obtener la URL de la imagen principal
    String getMainImageUrl(Long accommodationId);
}
