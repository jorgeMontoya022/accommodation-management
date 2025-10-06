package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.GuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GuestMapper {

    /**
     * Convierte una entidad GuestEntity a su DTO correspondiente.
     */
    @Named("guestEntityToGuestDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "dateBirth", target = "dateBirth")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "active", target = "active")
    GuestDto guestEntityToGuestDto(GuestEntity guestEntity);


    /**
     * Convierte un DTO GuestDto a una nueva entidad GuestEntity.
     * IGNORA las fechas de auditoría y relaciones porque se manejan por separado.
     */
    @Named("guestDtoToGuestEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "dateBirth", target = "dateBirth")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "active", target = "active")
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "commentsWritten", ignore = true)
    GuestEntity guestDtoToGuestEntity(GuestDto guestDto);


    /**
     * Actualiza una GuestEntity existente con datos de GuestDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, email, fechas, relaciones)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en GuestDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {name: "Nuevo Nombre"}, solo se actualiza el nombre
     *
     * @param guestDto DTO con los datos a actualizar
     * @param guestEntity entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                    // ID nunca cambia
    @Mapping(target = "email", ignore = true)                 // Email no se puede modificar
    @Mapping(target = "dateRegister", ignore = true)          // Fecha de registro es inmutable
    @Mapping(target = "dateUpdate", ignore = true)            // Se maneja en el Service
    @Mapping(target = "active", ignore = true)                // Se maneja en el Service (soft delete)
    @Mapping(target = "bookingEntityList", ignore = true)     // Relaciones no se actualizan aquí
    @Mapping(target = "commentsWritten", ignore = true)       // Relaciones no se actualizan aquí
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(GuestDto guestDto, @MappingTarget GuestEntity guestEntity);


    /**
     * Convierte una lista de entidades GuestEntity a una lista de DTOs.
     *
     * @param guestEntityList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "guestEntityToGuestDto")
    List<GuestDto> getGuestsDto(List<GuestEntity> guestEntityList);


    /**
     * Convierte una lista de DTOs GuestDto a una lista de entidades.
     *
     * @param guestDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "guestDtoToGuestEntity")
    List<GuestEntity> getGuestsEntity(List<GuestDto> guestDtoList);


}
