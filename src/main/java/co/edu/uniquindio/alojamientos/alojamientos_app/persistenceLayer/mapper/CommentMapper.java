package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.CommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseCommentDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.CommentEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Named("reviewEntityToReviewDto")
    @Mapping(source = "tex", target = "text")
    @Mapping(source = "authorGuest.name", target = "authorName")
    @Mapping(source = "accommodationEntity.qualification", target = "accommodationName")
    ResponseCommentDto reviewEntityToReviewDto(CommentEntity commentEntity);

    @Named("reviewDtoToReviewEntity")
    @Mapping(source = "text", target = "tex")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateResponse", ignore = true)
    @Mapping(target = "accommodationEntity", ignore = true)
    @Mapping(target = "authorGuest", ignore = true)
    @Mapping(target = "bookingEntity", ignore = true)
    CommentEntity reviewDtoToReviewEntity(CommentDto CommentDto);

    @Mapping(source = "text", target = "tex")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "hostResponse", ignore = true)
    @Mapping(target = "dateResponse", ignore = true)
    @Mapping(target = "accommodationEntity", ignore = true)
    @Mapping(target = "authorGuest", ignore = true)
    @Mapping(target = "bookingEntity", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CommentDto commentDto, @MappingTarget CommentEntity commentEntity);

    @IterableMapping(qualifiedByName = "reviewEntityToReviewDto")
    List<ResponseCommentDto> getReviewsDto(List<CommentEntity> commentEntityList);

    @IterableMapping(qualifiedByName = "reviewDtoToReviewEntity")
    List<CommentEntity> getReviewsEntity(List<CommentDto> commentDtoList);
}