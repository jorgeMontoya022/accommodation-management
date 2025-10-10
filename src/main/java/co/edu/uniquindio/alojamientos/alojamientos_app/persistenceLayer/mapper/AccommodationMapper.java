package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccommodationMapper {

    @Named("accommodationEntityToAccommodationDto")
    @Mapping(source = "hostEntity.id", target = "idHost")
    ResponseAccommodationDto accommodationEntityToAccommodationDto(AccommodationEntity accommodationEntity);

    @Named("accommodationEntityToAccommodationRequestDto")
    RequestAccommodationDto accommodationEntityToAccommodationRequestDto(AccommodationEntity accommodation);

    @Named("accommodationDtoToAccommodationEntity")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "hostEntity", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "comments", ignore = true)
    AccommodationEntity accommodationDtoToAccommodationEntity(RequestAccommodationDto accommodationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "statusAccommodation", ignore = true)
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "hostEntity", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RequestAccommodationDto accommodationDto, @MappingTarget AccommodationEntity accommodationEntity);

    @IterableMapping(qualifiedByName = "accommodationEntityToAccommodationDto")
    List<ResponseAccommodationDto> getAccommodationsDto(List<AccommodationEntity> accommodationEntityList);

    @IterableMapping(qualifiedByName = "accommodationDtoToAccommodationEntity")
    List<AccommodationEntity> getAccommodationsEntity(List<RequestAccommodationDto> accommodationDtoList);
}