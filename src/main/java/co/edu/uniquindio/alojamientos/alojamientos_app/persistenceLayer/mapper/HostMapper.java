package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HostMapper {

    @Named("hostEntityToHostDto")
    ResponseHostDto hostEntityToHostDto(HostEntity hostEntity);

    @Named("hostDtoToHostEntity")
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "accommodationEntityList", ignore = true)
    HostEntity hostDtoToHostEntity(RequestHostDto hostDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "dateRegister", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "accommodationEntityList", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateHostDto hostDto, @MappingTarget HostEntity hostEntity);

    @IterableMapping(qualifiedByName = "hostEntityToHostDto")
    List<ResponseHostDto> getHostsDto(List<HostEntity> hostEntityList);

    @IterableMapping(qualifiedByName = "hostDtoToHostEntity")
    List<HostEntity> getHostsEntity(List<RequestHostDto> hostDtoList);
}