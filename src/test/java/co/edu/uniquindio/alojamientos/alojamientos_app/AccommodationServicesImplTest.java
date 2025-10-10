package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseAccommodationDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.AccommodationServicesImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.AccommodationDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.*;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.AccommodationMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.AccommodationRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationDao accommodationDao;

    @Mock
    private HostService hostService;

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private AccommodationServicesImpl accommodationService;

    private HostEntity hostEntity;
    private AccommodationEntity accommodationEntity;
    private RequestAccommodationDto requestDto;
    private ResponseAccommodationDto responseDto;

    @BeforeEach
    void setUp() {
        hostEntity = new HostEntity();
        hostEntity.setId(1L);
        hostEntity.setName("Juan Pérez");
        hostEntity.setEmail("juan@example.com");
        hostEntity.setActive(true);

        accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(10L);
        accommodationEntity.setQualification("Casa de campo");
        accommodationEntity.setDescription("Hermosa casa en el campo");
        accommodationEntity.setCity("Armenia");
        accommodationEntity.setLatitude("4.45");
        accommodationEntity.setLongitude("-75.66");
        accommodationEntity.setPriceNight(300000);
        accommodationEntity.setMaximumCapacity(4);
        accommodationEntity.setStatusAccommodation(StatusAccommodation.ACTIVE);
        accommodationEntity.setHostEntity(hostEntity);

        requestDto = new RequestAccommodationDto();
        requestDto.setQualification("Casa de campo");
        requestDto.setDescription("Hermosa casa en el campo");
        requestDto.setCity("Armenia");
        requestDto.setLatitude(4.4F);
        requestDto.setLongitude(-75.6F);
        requestDto.setPriceNight(300000);
        requestDto.setMaximumCapacity(4);

        responseDto = new ResponseAccommodationDto();
        responseDto.setId(10L);
        responseDto.setQualification("Casa de campo");
        responseDto.setDescription("Hermosa casa en el campo");
        responseDto.setCity("Armenia");
        responseDto.setPriceNight(300000);
        responseDto.setMaximumCapacity(4);
    }

    @Test
    @DisplayName("Debe crear un alojamiento exitosamente")
    void testCreateAccommodationSuccess() {
        when(hostService.getHostEntityById(1L)).thenReturn(hostEntity);
        when(accommodationMapper.accommodationDtoToAccommodationEntity(any())).thenReturn(accommodationEntity);
        when(accommodationDao.saveEntity(any())).thenReturn(accommodationEntity);
        when(accommodationMapper.accommodationEntityToAccommodationDto(any())).thenReturn(responseDto);

        ResponseAccommodationDto result = accommodationService.createAccommodation(requestDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getQualification()).isEqualTo("Casa de campo");
        verify(accommodationDao, times(1)).saveEntity(any(AccommodationEntity.class));
        verify(accommodationMapper, times(1)).accommodationEntityToAccommodationDto(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el host está inactivo")
    void testCreateAccommodationInactiveHost() {
        hostEntity.setActive(false);
        when(hostService.getHostEntityById(1L)).thenReturn(hostEntity);

        assertThatThrownBy(() -> accommodationService.createAccommodation(requestDto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El anfitrión no está activo");
    }

    @Test
    @DisplayName("Debe actualizar un alojamiento existente")
    void testUpdateAccommodation() {
        when(accommodationDao.findById(10L)).thenReturn(Optional.of(accommodationEntity));
        when(accommodationDao.updateEntity(any())).thenReturn(accommodationEntity);
        when(accommodationMapper.accommodationEntityToAccommodationDto(any())).thenReturn(responseDto);

        ResponseAccommodationDto result = accommodationService.updateAccommodation(10L, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getQualification()).isEqualTo("Casa de campo");
        verify(accommodationDao).updateEntity(any(AccommodationEntity.class));
    }

    @Test
    @DisplayName("Debe eliminar (soft delete) un alojamiento existente")
    void testDeleteAccommodationSuccess() {
        accommodationEntity.setDeleted(false);
        when(accommodationDao.findById(10L)).thenReturn(Optional.of(accommodationEntity));

        accommodationService.deleteAccommodation(10L);

        assertThat(accommodationEntity.isDeleted()).isTrue();
        verify(accommodationDao, times(1)).save(accommodationEntity);
    }

    @Test
    @DisplayName("Debe devolver un alojamiento por ID")
    void testGetAccommodationById() {
        when(accommodationDao.findById(10L)).thenReturn(Optional.of(accommodationEntity));

        AccommodationEntity result = accommodationService.getAccommodationById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Armenia");
        verify(accommodationDao).findById(10L);
    }

    @Test
    @DisplayName("Debe devolver lista de alojamientos por ciudad")
    void testGetAccommodationsByCity() {
        List<AccommodationEntity> list = List.of(accommodationEntity);
        when(accommodationDao.findByCity("Armenia")).thenReturn(list);
        when(accommodationMapper.getAccommodationsDto(list)).thenReturn(List.of(responseDto));

        List<ResponseAccommodationDto> result = accommodationService.getAccommodationsByCity("Armenia");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Armenia");
        verify(accommodationDao).findByCity("Armenia");
    }

    @Test
    @DisplayName("Debe obtener la URL de imagen principal")
    void testGetMainImageUrl() {
        ImageAccommodation img1 = new ImageAccommodation();
        img1.setUrl("main.jpg");
        img1.setPrincipal(true);
        img1.setDisplayOrder(1);

        accommodationEntity.setImages(List.of(img1));

        when(accommodationRepository.fetchWithImagesById(10L)).thenReturn(Optional.of(accommodationEntity));

        String url = accommodationService.getMainImageUrl(10L);

        assertThat(url).isEqualTo("main.jpg");
        verify(accommodationRepository).fetchWithImagesById(10L);
    }

    @Test
    @DisplayName("Debe listar alojamientos por host con paginación")
    void testListByHost() {
        Page<AccommodationEntity> pageEntities = new PageImpl<>(List.of(accommodationEntity));
        when(accommodationRepository.findAllByHostEntity_IdAndDeletedFalse(eq(1L), any(Pageable.class)))
                .thenReturn(pageEntities);
        when(accommodationMapper.accommodationEntityToAccommodationDto(any())).thenReturn(responseDto);

        Pageable pageable = PageRequest.of(0, 5);
        Page<ResponseAccommodationDto> result = accommodationService.listByHost(1L, pageable);

        assertThat(result).hasSize(1);
        verify(accommodationRepository).findAllByHostEntity_IdAndDeletedFalse(eq(1L), any(Pageable.class));
    }
}