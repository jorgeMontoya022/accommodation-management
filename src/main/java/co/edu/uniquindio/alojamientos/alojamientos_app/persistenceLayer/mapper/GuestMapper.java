package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.GuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GuestMapper {

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
     * Convierte un DTO GuestDto a su entidad correspondiente.
     * IGNORA las fechas de auditoría porque se manejan automáticamente.
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
