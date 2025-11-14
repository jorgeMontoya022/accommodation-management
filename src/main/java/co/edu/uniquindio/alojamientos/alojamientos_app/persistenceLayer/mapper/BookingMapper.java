package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseBookingDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.BookingEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Named("bookingEntityToBookingDto")
    @Mapping(source = "accommodationAssociated.id", target = "idAccommodation")
    @Mapping(source = "guestEntity.id", target = "idGuest")
    @Mapping(target = "guestName", source = "guestEntity.name")
    @Mapping(target = "guestEmail", source = "guestEntity.email")
    @Mapping(target = "guestPhone", source = "guestEntity.phone")
    ResponseBookingDto bookingEntityToBookingDto(BookingEntity bookingEntity);

    @Named("bookingDtoToBookingEntity")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateCancellation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "accommodationAssociated", ignore = true)
    @Mapping(target = "guestEntity", ignore = true)
    @Mapping(target = "comments", ignore = true)
    BookingEntity bookingDtoToBookingEntity(RequestBookingDto bookingDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateCancellation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "statusReservation", ignore = true)
    @Mapping(target = "accommodationAssociated", ignore = true)
    @Mapping(target = "guestEntity", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reasonCancellation", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RequestBookingDto bookingDto, @MappingTarget BookingEntity bookingEntity);

    @IterableMapping(qualifiedByName = "bookingEntityToBookingDto")
    List<ResponseBookingDto> getBookingsDto(List<BookingEntity> bookingEntityList);

    @IterableMapping(qualifiedByName = "bookingDtoToBookingEntity")
    List<BookingEntity> getBookingsEntity(List<RequestBookingDto> bookingDtoList);
}