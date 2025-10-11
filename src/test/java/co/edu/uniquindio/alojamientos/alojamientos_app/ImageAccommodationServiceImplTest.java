package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ImageAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.BusinessException;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.ImageAccommodationServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.ImageAccommodation;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.ImageAccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.ImageAccommodationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageAccommodationServiceImplTest {

    @Mock
    private ImageAccommodationRepository imageRepo;

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private ImageAccommodationMapper mapper;

    @InjectMocks
    private ImageAccommodationServiceImpl service;

    private final Long ACC_ID = 10L;

    @BeforeEach
    void setUp() {
    }

    // listGallery
    @Test
    @DisplayName("listGallery - should return DTO list ordered")
    void listGallery_ReturnsDtos() {
        ImageAccommodation e = new ImageAccommodation();
        e.setId(1L);
        List<ImageAccommodation> entities = List.of(e);

        ImageAccommodationDto dto = new ImageAccommodationDto();
        dto.setId(1L);

        when(imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(ACC_ID)).thenReturn(entities);
        when(mapper.getImageAccommodationsDto(entities)).thenReturn(List.of(dto));

        var res = service.listGallery(ACC_ID);

        assertThat(res).hasSize(1).contains(dto);
        verify(imageRepo).findByAccommodationEntity_IdOrderByDisplayOrderAsc(ACC_ID);
        verify(mapper).getImageAccommodationsDto(entities);
    }

    // addImage
    @Test
    @DisplayName("addImage - success: adds image and returns dto")
    void addImage_Success() {
        ImageAccommodationDto request = new ImageAccommodationDto();
        request.setIdAccommodation(ACC_ID);
        request.setUrl("https://example.com/img.jpg");
        request.setIsPrincipal(false);

        AccommodationEntity acc = new AccommodationEntity();
        acc.setId(ACC_ID);

        ImageAccommodation entityFromDto = new ImageAccommodation();
        ImageAccommodation savedEntity = new ImageAccommodation();
        savedEntity.setId(5L);
        savedEntity.setUrl(request.getUrl());
        savedEntity.setPrincipal(false);

        ImageAccommodationDto resultDto = new ImageAccommodationDto();
        resultDto.setId(5L);

        when(accommodationRepo.findById(ACC_ID)).thenReturn(Optional.of(acc));
        when(imageRepo.countByAccommodationEntity_Id(ACC_ID)).thenReturn(0L);
        when(imageRepo.findMaxDisplayOrderByAccommodationId(ACC_ID)).thenReturn(0);
        when(mapper.imageAccommodationDtoToImageAccommodationEntity(request)).thenReturn(entityFromDto);
        when(imageRepo.save(entityFromDto)).thenReturn(savedEntity);
        when(mapper.imageAccommodationEntityToImageAccommodationDto(savedEntity)).thenReturn(resultDto);

        ImageAccommodationDto res = service.addImage(request);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(5L);
        verify(imageRepo).save(entityFromDto);
    }

    @Test
    @DisplayName("addImage - should throw when accommodation not provided")
    void addImage_NoAccommodation_Throws() {
        ImageAccommodationDto request = new ImageAccommodationDto();
        request.setUrl("https://example.com/img.jpg");
        request.setIsPrincipal(false);

        assertThatThrownBy(() -> service.addImage(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Debe indicar el id del alojamiento");
    }

    @Test
    @DisplayName("addImage - should throw when max images exceeded")
    void addImage_MaxImages_Throws() {
        ImageAccommodationDto request = new ImageAccommodationDto();
        request.setIdAccommodation(ACC_ID);
        request.setUrl("https://example.com/img.jpg");
        request.setIsPrincipal(false);

        AccommodationEntity acc = new AccommodationEntity();
        acc.setId(ACC_ID);

        when(accommodationRepo.findById(ACC_ID)).thenReturn(Optional.of(acc));
        when(imageRepo.countByAccommodationEntity_Id(ACC_ID)).thenReturn(6L);

        assertThatThrownBy(() -> service.addImage(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    @DisplayName("addImage - should throw when principal already exists")
    void addImage_PrincipalExists_Throws() {
        ImageAccommodationDto request = new ImageAccommodationDto();
        request.setIdAccommodation(ACC_ID);
        request.setUrl("https://example.com/img.jpg");
        request.setIsPrincipal(true);

        AccommodationEntity acc = new AccommodationEntity();
        acc.setId(ACC_ID);

        when(accommodationRepo.findById(ACC_ID)).thenReturn(Optional.of(acc));
        when(imageRepo.countByAccommodationEntity_Id(ACC_ID)).thenReturn(0L);
        when(imageRepo.existsByAccommodationEntity_IdAndIsPrincipalTrue(anyLong())).thenReturn(true);

        assertThatThrownBy(() -> service.addImage(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("imagen principal");
    }

    // setAsPrincipal
    @Test
    @DisplayName("setAsPrincipal - should set image as principal and unset others")
    void setAsPrincipal_Success() {
        ImageAccommodation img = new ImageAccommodation();
        img.setId(2L);
        img.setPrincipal(false);

        when(imageRepo.findByIdAndAccommodationEntity_Id(2L, ACC_ID)).thenReturn(Optional.of(img));
        when(imageRepo.save(any(ImageAccommodation.class))).thenReturn(img);

        service.setAsPrincipal(ACC_ID, 2L);

        verify(imageRepo).unsetPrincipalForAccommodation(ACC_ID, img.getId());
        assertThat(img.isPrincipal()).isTrue();
        verify(imageRepo).save(img);
    }

    @Test
    @DisplayName("setAsPrincipal - should throw when image not belong or missing")
    void setAsPrincipal_NotFound_Throws() {
        when(imageRepo.findByIdAndAccommodationEntity_Id(2L, ACC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setAsPrincipal(ACC_ID, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece");
    }

    // reorder
    @Test
    @DisplayName("reorder - should reorder images successfully")
    void reorder_Success() {
        ImageAccommodation i1 = new ImageAccommodation(); i1.setId(1L); i1.setDisplayOrder(1);
        ImageAccommodation i2 = new ImageAccommodation(); i2.setId(2L); i2.setDisplayOrder(2);
        List<ImageAccommodation> current = List.of(i1, i2);

        when(imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(ACC_ID)).thenReturn(current);

        service.reorder(ACC_ID, List.of(2L, 1L));

        ArgumentCaptor<ImageAccommodation> captor = ArgumentCaptor.forClass(ImageAccommodation.class);
        verify(imageRepo, times(2)).save(captor.capture());

        List<ImageAccommodation> saved = captor.getAllValues();

        assertThat(saved).extracting("id", "displayOrder")
                .containsExactlyInAnyOrder(
                        tuple(2L, 1),
                        tuple(1L, 2)
                );
    }

    @Test
    @DisplayName("reorder - should throw when provided list does not match current gallery")
    void reorder_InvalidList_Throws() {
        ImageAccommodation i1 = new ImageAccommodation(); i1.setId(1L);
        List<ImageAccommodation> current = List.of(i1);

        when(imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(ACC_ID)).thenReturn(current);

        assertThatThrownBy(() -> service.reorder(ACC_ID, List.of(1L, 2L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    @DisplayName("reorder - should throw when empty input list")
    void reorder_EmptyInput_Throws() {
        assertThatThrownBy(() -> service.reorder(ACC_ID, Collections.emptyList()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Debe enviar al menos");
    }

    // removeImage
    @Test
    @DisplayName("removeImage - principal removed promotes first image")
    void removeImage_PrincipalPromote() {
        ImageAccommodation principal = new ImageAccommodation();
        principal.setId(11L);
        principal.setPrincipal(true);

        ImageAccommodation first = new ImageAccommodation();
        first.setId(12L);
        first.setPrincipal(false);

        when(imageRepo.findByIdAndAccommodationEntity_Id(11L, ACC_ID)).thenReturn(Optional.of(principal));
        when(imageRepo.findByAccommodationEntity_IdOrderByDisplayOrderAsc(ACC_ID)).thenReturn(List.of(first));
        when(imageRepo.save(first)).thenReturn(first);

        service.removeImage(ACC_ID, 11L);

        verify(imageRepo).deleteById(11L);
        verify(imageRepo).unsetPrincipalForAccommodation(ACC_ID, first.getId());
        verify(imageRepo).save(first);
    }

    @Test
    @DisplayName("removeImage - throws when image doesn't belong")
    void removeImage_NotFound_Throws() {
        when(imageRepo.findByIdAndAccommodationEntity_Id(99L, ACC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeImage(ACC_ID, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece");
    }

    // replaceGallery
    @Test
    @DisplayName("replaceGallery - should throw when more than max images supplied")
    void replaceGallery_TooMany_Throws() {
        List<ImageAccommodationDto> many = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ImageAccommodationDto dto = new ImageAccommodationDto();
            dto.setIdAccommodation(ACC_ID);
            dto.setUrl("https://example.com/img" + i + ".jpg");
            many.add(dto);
        }

        AccommodationEntity acc = new AccommodationEntity();
        acc.setId(ACC_ID);
        when(accommodationRepo.findById(ACC_ID)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> service.replaceGallery(ACC_ID, many))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No puede subir más de");
    }

    // getMainImageUrl
    @Test
    @DisplayName("getMainImageUrl - returns url when principal present")
    void getMainImageUrl_ReturnsUrl() {
        ImageAccommodation principal = new ImageAccommodation();
        principal.setId(21L);
        principal.setUrl("https://example.com/main.jpg");
        principal.setPrincipal(true);

        when(imageRepo.findFirstByAccommodationEntity_IdAndIsPrincipalTrue(ACC_ID))
                .thenReturn(Optional.of(principal));

        String url = service.getMainImageUrl(ACC_ID);

        assertThat(url).isEqualTo("https://example.com/main.jpg");
    }

    @Test
    @DisplayName("getMainImageUrl - returns null when none principal")
    void getMainImageUrl_NullWhenMissing() {
        when(imageRepo.findFirstByAccommodationEntity_IdAndIsPrincipalTrue(ACC_ID))
                .thenReturn(Optional.empty());

        assertThat(service.getMainImageUrl(ACC_ID)).isNull();
    }
}
