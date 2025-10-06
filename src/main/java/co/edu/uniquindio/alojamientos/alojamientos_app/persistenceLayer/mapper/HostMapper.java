package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface HostMapper {

    /**
     * Convierte una entidad HostEntity a su DTO correspondiente.
     */
    @Named("hostEntityToHostDto")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "dateBirth", target = "dateBirth")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "active", target = "active")
    HostDto hostEntityToHostDto(HostEntity hostEntity);


    /**
     * Convierte un DTO HostDto a una nueva entidad HostEntity.
     * IGNORA las fechas de auditoría y relaciones porque se manejan por separado.
     */
    @Named("hostDtoToHostEntity")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "dateBirth", target = "dateBirth")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(source = "active", target = "active")
    @Mapping(target = "accommodationEntityList", ignore = true)
    HostEntity hostDtoToHostEntity(HostDto hostDto);


    /**
     * Actualiza una HostEntity existente con datos de HostDto.
     *
     * ¿POR QUÉ @MappingTarget?
     * - Actualiza la entidad existente en lugar de crear una nueva
     * - Preserva campos que no deben modificarse (id, email, fechas, relaciones)
     * - Permite actualización parcial (solo campos no-null del DTO)
     *
     * ESTRATEGIA NULL_VALUE_PROPERTY_MAPPING_STRATEGY.IGNORE:
     * - Si un campo en HostDto es null, NO actualiza ese campo en la entity
     * - Permite actualización parcial tipo PATCH
     * - Ejemplo: Si solo envías {name: "Nuevo Nombre"}, solo se actualiza el nombre
     *
     * @param hostDto DTO con los datos a actualizar
     * @param hostEntity entidad existente que será actualizada
     */
    @Mapping(target = "id", ignore = true)                       // ID nunca cambia
    @Mapping(target = "email", ignore = true)                    // Email no se puede modificar
    @Mapping(target = "dateRegister", ignore = true)             // Fecha de registro es inmutable
    @Mapping(target = "dateUpdate", ignore = true)               // Se maneja en el Service
    @Mapping(target = "active", ignore = true)                   // Se maneja en el Service (soft delete)
    @Mapping(target = "accommodationEntityList", ignore = true)  // Relaciones no se actualizan aquí
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(HostDto hostDto, @MappingTarget HostEntity hostEntity);


    /**
     * Convierte una lista de entidades HostEntity a una lista de DTOs.
     *
     * @param hostEntityList lista de entidades a convertir
     * @return lista de DTOs
     */
    @IterableMapping(qualifiedByName = "hostEntityToHostDto")
    List<HostDto> getHostsDto(List<HostEntity> hostEntityList);


    /**
     * Convierte una lista de DTOs HostDto a una lista de entidades.
     *
     * @param hostDtoList lista de DTOs a convertir
     * @return lista de entidades
     */
    @IterableMapping(qualifiedByName = "hostDtoToHostEntity")
    List<HostEntity> getHostsEntity(List<HostDto> hostDtoList);


}
