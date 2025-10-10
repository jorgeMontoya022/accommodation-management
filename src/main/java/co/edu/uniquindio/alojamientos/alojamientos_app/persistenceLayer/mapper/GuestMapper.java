package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuestMapper {

    @Named("guestEntityToGuestDto")
    ResponseGuestDto guestEntityToGuestDto(GuestEntity guestEntity);

    @Named("guestDtoToGuestEntity")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "commentsWritten", ignore = true)
    GuestEntity guestDtoToGuestEntity(RequestGuestDto guestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bookingEntityList", ignore = true)
    @Mapping(target = "commentsWritten", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateGuestDto guestDto, @MappingTarget GuestEntity guestEntity);

    @IterableMapping(qualifiedByName = "guestEntityToGuestDto")
    List<ResponseGuestDto> getGuestsDto(List<GuestEntity> guestEntityList);

    @IterableMapping(qualifiedByName = "guestDtoToGuestEntity")
    List<GuestEntity> getGuestsEntity(List<RequestGuestDto> guestDtoList);
}