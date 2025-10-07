package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ImageAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ImageAccommodationMapper {

    /**
     * Convierte una entidad ImageAccommodation a su DTO correspondiente.
     */
    @Named("imageAccommodationEntityToImageAccommodationDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "isPrincipal", target = "isPrincipal")
    @Mapping(source = "displayOrder", target = "displayOrder")
    @Mapping(source = "accommodationEntity.id", target = "idAccommodation")
    ImageAccommodationDto imageAccommodationEntityToImageAccommodationDto(ImageAccommodation imageAccommodation);


    /**
     * Convierte un DTO ImageAccommodationDto a una nueva entidad ImageAccommodation.
     * IGNORA la relación con el alojamiento porque se maneja por separado.
     */
    @Named("imageAccommodationDtoToImageAccommodationEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "isPrincipal", target = "isPrincipal")
    @Mapping(source = "displayOrder", target = "displayOrder")
    @Mapping(target = "accommodationEntity", ignore = true)
    ImageAccommodation imageAccommodationDtoToImageAccommodationEntity(ImageAccommodationDto imageAccommodationDto);


    /**
     * Actualiza una ImageAccommodation existente con datos de ImageAccommodationDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, relación con alojamiento)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en ImageAccommodationDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {displayOrder: 2}, solo se actualiza el orden
     *
     * @param imageAccommodationDto DTO con los datos a actualizar
     * @param imageAccommodation entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                        // ID nunca cambia
    @Mapping(target = "accommodationEntity", ignore = true)       // Relación no se actualiza aquí
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ImageAccommodationDto imageAccommodationDto, @MappingTarget ImageAccommodation imageAccommodation);


    /**
     * Convierte una lista de entidades ImageAccommodation a una lista de DTOs.
     *
     * @param imageAccommodationList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "imageAccommodationEntityToImageAccommodationDto")
    List<ImageAccommodationDto> getImageAccommodationsDto(List<ImageAccommodation> imageAccommodationList);


    /**
     * Convierte una lista de DTOs ImageAccommodationDto a una lista de entidades.
     *
     * @param imageAccommodationDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "imageAccommodationDtoToImageAccommodationEntity")
    List<ImageAccommodation> getImageAccommodationsEntity(List<ImageAccommodationDto> imageAccommodationDtoList);


}