package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.AccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccommodationDao {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    public ResponseAccommodationDto save(AccommodationEntity entity) {
        AccommodationEntity saved = accommodationRepository.save(entity);
        return accommodationMapper.accommodationEntityToAccommodationDto(saved);
    }


    public Optional<AccommodationEntity> findById(Long id){
        return accommodationRepository.findById(id);
    }

    public Optional<ResponseAccommodationDto> update(Long id, RequestAccommodationDto requestAccommodationDto) {
        return accommodationRepository.findById(id)
                .map(existingEntity -> {
                    accommodationMapper.updateEntityFromDto(requestAccommodationDto, existingEntity);
                    AccommodationEntity accommodationUpdate = accommodationRepository.save(existingEntity);
                    return accommodationMapper.accommodationEntityToAccommodationDto(accommodationUpdate);
                });
    }

    public Long countBookingsByAccommodationId(Long id) {
        return accommodationRepository.countBookingsByAccommodationId(id);
    }

    public boolean deleteById(Long id) {
        if(accommodationRepository.existsById(id)){
            accommodationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
