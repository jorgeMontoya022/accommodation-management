package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CommentMapper {

    /**
     * Convierte una entidad ReviewEntity a su DTO correspondiente.
     */
    @Named("reviewEntityToReviewDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "tex", target = "text")
    @Mapping(source = "hostResponse", target = "hostResponse")
    @Mapping(source = "dateCreation", target = "dateCreation")
    @Mapping(source = "dateResponse", target = "dateResponse")
    @Mapping(source = "accommodationEntity.id", target = "idAccommodation")
    @Mapping(source = "authorGuest.id", target = "idGuest")
    @Mapping(source = "bookingEntity.id", target = "idBooking")
    ResponseCommentDto reviewEntityToReviewDto(CommentEntity commentEntity);


    /**
     * Convierte un DTO ReviewDto a una nueva entidad ReviewEntity.
     * IGNORA las fechas de auditoría y relaciones porque se manejan por separado.
     */
    @Named("reviewDtoToReviewEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "text", target = "tex")
    @Mapping(source = "hostResponse", target = "hostResponse")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateResponse", ignore = true)
    @Mapping(target = "accommodationEntity", ignore = true)
    @Mapping(target = "authorGuest", ignore = true)
    @Mapping(target = "bookingEntity", ignore = true)
    CommentEntity reviewDtoToReviewEntity(CommentDto CommentDto);


    /**
     * Actualiza una ReviewEntity existente con datos de ReviewDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, fechas, relaciones)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en ReviewDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {rating: 4}, solo se actualiza la calificación
     *
     * @param commentDto DTO con los datos a actualizar
     * @param commentEntity entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                    // ID nunca cambia
    @Mapping(target = "dateCreation", ignore = true)          // Fecha de creación es inmutable
    @Mapping(target = "hostResponse", ignore = true)          // Se maneja en el Service (solo el host puede responder)
    @Mapping(target = "dateResponse", ignore = true)          // Se maneja en el Service
    @Mapping(target = "accommodationEntity", ignore = true)   // Relación no se actualiza aquí
    @Mapping(target = "authorGuest", ignore = true)           // Relación no se actualiza aquí
    @Mapping(target = "bookingEntity", ignore = true)         // Relación no se actualiza aquí
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CommentDto commentDto, @MappingTarget CommentEntity commentEntity);


    /**
     * Convierte una lista de entidades ReviewEntity a una lista de DTOs.
     *
     * @param commentEntityList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "reviewEntityToReviewDto")
    List<ResponseCommentDto> getReviewsDto(List<CommentEntity> commentEntityList);


    /**
     * Convierte una lista de DTOs ReviewDto a una lista de entidades.
     *
     * @param commentDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "reviewDtoToReviewEntity")
    List<CommentEntity> getReviewsEntity(List<CommentDto> commentDtoList);


}