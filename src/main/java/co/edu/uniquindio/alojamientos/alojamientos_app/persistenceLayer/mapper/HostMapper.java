package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface HostMapper {
}
