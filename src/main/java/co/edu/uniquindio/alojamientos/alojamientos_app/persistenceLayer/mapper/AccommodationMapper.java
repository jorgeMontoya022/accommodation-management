package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.AccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AccommodationMapper {

    /**
     * Convierte una entidad AccommodationEntity a su DTO correspondiente.
     */
    @Named("accommodationEntityToAccommodationDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "qualification", target = "qualification")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "priceNight", target = "priceNight")
    @Mapping(source = "maximumCapacity", target = "maximumCapacity")
    @Mapping(source = "dateCreation", target = "dateCreation")
    @Mapping(source = "dateUpdate", target = "dateUpdate")
    @Mapping(source = "statusAccommodation", target = "statusAccommodation")
    @Mapping(source = "typeServicesEnum", target = "typeServicesEnum")
    @Mapping(source = "hostEntity.id", target = "idHost")
    AccommodationDto accommodationEntityToAccommodationDto(AccommodationEntity accommodationEntity);


    /**
     * Convierte un DTO AccommodationDto a una nueva entidad AccommodationEntity.
     * IGNORA las fechas de auditoría y relaciones porque se manejan por separado.
     */
    @Named("accommodationDtoToAccommodationEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "qualification", target = "qualification")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "priceNight", target = "priceNight")
    @Mapping(source = "maximumCapacity", target = "maximumCapacity")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "statusAccommodation", target = "statusAccommodation")
    @Mapping(source = "typeServicesEnum", target = "typeServicesEnum")
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "hostEntity", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "comments", ignore = true)
    AccommodationEntity accommodationDtoToAccommodationEntity(AccommodationDto accommodationDto);


    /**
     * Actualiza una AccommodationEntity existente con datos de AccommodationDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, fechas, relaciones)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en AccommodationDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {priceNight: 300000}, solo se actualiza el precio
     *
     * @param accommodationDto DTO con los datos a actualizar
     * @param accommodationEntity entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                    // ID nunca cambia
    @Mapping(target = "dateCreation", ignore = true)          // Fecha de creación es inmutable
    @Mapping(target = "dateUpdate", ignore = true)            // Se maneja en el Service
    @Mapping(target = "statusAccommodation", ignore = true)   // Se maneja en el Service
    @Mapping(target = "bookingEntityList", ignore = true)     // Relaciones no se actualizan aquí
    @Mapping(target = "hostEntity", ignore = true)            // Relación con host no se actualiza aquí
    @Mapping(target = "images", ignore = true)                // Relaciones no se actualizan aquí
    @Mapping(target = "comments", ignore = true)              // Relaciones no se actualizan aquí
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AccommodationDto accommodationDto, @MappingTarget AccommodationEntity accommodationEntity);


    /**
     * Convierte una lista de entidades AccommodationEntity a una lista de DTOs.
     *
     * @param accommodationEntityList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "accommodationEntityToAccommodationDto")
    List<AccommodationDto> getAccommodationsDto(List<AccommodationEntity> accommodationEntityList);


    /**
     * Convierte una lista de DTOs AccommodationDto a una lista de entidades.
     *
     * @param accommodationDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "accommodationDtoToAccommodationEntity")
    List<AccommodationEntity> getAccommodationsEntity(List<AccommodationDto> accommodationDtoList);


}