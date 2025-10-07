package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.BookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BookingMapper {

    /**
     * Convierte una entidad BookingEntity a su DTO correspondiente.
     */
    @Named("bookingEntityToBookingDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "dateCheckin", target = "dateCheckin")
    @Mapping(source = "dateCheckout", target = "dateCheckout")
    @Mapping(source = "dateCreation", target = "dateCreation")
    @Mapping(source = "dateCancellation", target = "dateCancellation")
    @Mapping(source = "dateUpdate", target = "dateUpdate")
    @Mapping(source = "statusReservation", target = "statusReservation")
    @Mapping(source = "accommodationAssociated.id", target = "idAccommodation")
    @Mapping(source = "guestEntity.id", target = "idGuest")
    @Mapping(source = "quantityPeople", target = "quantityPeople")
    @Mapping(source = "totalValue", target = "totalValue")
    @Mapping(source = "reasonCancellation", target = "reasonCancellation")
    BookingDto bookingEntityToBookingDto(BookingEntity bookingEntity);


    /**
     * Convierte un DTO BookingDto a una nueva entidad BookingEntity.
     * IGNORA las fechas de auditoría y relaciones porque se manejan por separado.
     */
    @Named("bookingDtoToBookingEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "dateCheckin", target = "dateCheckin")
    @Mapping(source = "dateCheckout", target = "dateCheckout")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateCancellation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "statusReservation", target = "statusReservation")
    @Mapping(target = "accommodationAssociated", ignore = true)
    @Mapping(target = "guestEntity", ignore = true)
    @Mapping(source = "quantityPeople", target = "quantityPeople")
    @Mapping(source = "totalValue", target = "totalValue")
    @Mapping(source = "reasonCancellation", target = "reasonCancellation")
    @Mapping(target = "comments", ignore = true)
    BookingEntity bookingDtoToBookingEntity(BookingDto bookingDto);


    /**
     * Actualiza una BookingEntity existente con datos de BookingDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, fechas, relaciones)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en BookingDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {quantityPeople: 4}, solo se actualiza la cantidad de personas
     *
     * @param bookingDto DTO con los datos a actualizar
     * @param bookingEntity entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                         // ID nunca cambia
    @Mapping(target = "dateCreation", ignore = true)               // Fecha de creación es inmutable
    @Mapping(target = "dateCancellation", ignore = true)           // Se maneja en el Service
    @Mapping(target = "dateUpdate", ignore = true)                 // Se maneja en el Service
    @Mapping(target = "statusReservation", ignore = true)          // Se maneja en el Service
    @Mapping(target = "accommodationAssociated", ignore = true)    // Relación no se actualiza aquí
    @Mapping(target = "guestEntity", ignore = true)                // Relación no se actualiza aquí
    @Mapping(target = "comments", ignore = true)                   // Relaciones no se actualizan aquí
    @Mapping(target = "reasonCancellation", ignore = true)         // Se maneja solo en cancelación
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BookingDto bookingDto, @MappingTarget BookingEntity bookingEntity);


    /**
     * Convierte una lista de entidades BookingEntity a una lista de DTOs.
     *
     * @param bookingEntityList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "bookingEntityToBookingDto")
    List<BookingDto> getBookingsDto(List<BookingEntity> bookingEntityList);


    /**
     * Convierte una lista de DTOs BookingDto a una lista de entidades.
     *
     * @param bookingDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "bookingDtoToBookingEntity")
    List<BookingEntity> getBookingsEntity(List<BookingDto> bookingDtoList);


}