package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.*;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.HostServicesImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.HostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class HostServiceImplTest {

    @Mock
    private HostDao hostDao;
    @Mock
    private HostMapper hostMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private HostServicesImpl hostService;

    private RequestHostDto requestHostDto;
    private ResponseHostDto responseHostDto;
    private UpdateHostDto updateHostDto;
    private HostEntity hostEntity;

    @BeforeEach
    void setUp() {
        requestHostDto = new RequestHostDto();
        requestHostDto.setName("Juan Pérez");
        requestHostDto.setEmail("juan@example.com");
        requestHostDto.setPassword("123456");

        responseHostDto = ResponseHostDto.builder()
                .id(1L)
                .name("Juan Pérez")
                .email("juan@example.com")
                .active(true)
                .build();

        updateHostDto = new UpdateHostDto();
        updateHostDto.setName("Juan Actualizado");
        updateHostDto.setPhone("+573001234567");
        updateHostDto.setPhotoProfile("https://example.com/photo.jpg");

        hostEntity = new HostEntity();
        hostEntity.setId(1L);
        hostEntity.setEmail("juan@example.com");
        hostEntity.setPassword("encryptedPassword");
        hostEntity.setActive(true);
    }

    // -------------------------------------------------------------
    // CREATE HOST
    // -------------------------------------------------------------
    @Test
    @DisplayName("Debería crear un nuevo anfitrión exitosamente")
    void createHost_Success() {
        when(hostDao.existsByEmail(requestHostDto.getEmail())).thenReturn(false);
        when(hostMapper.hostDtoToHostEntity(requestHostDto)).thenReturn(hostEntity);
        when(passwordEncoder.encode(requestHostDto.getPassword())).thenReturn("encryptedPassword");
        when(hostDao.saveEntity(any(HostEntity.class))).thenReturn(responseHostDto);

        ResponseHostDto result = hostService.createHost(requestHostDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(requestHostDto.getEmail());
        verify(hostDao).existsByEmail(requestHostDto.getEmail());
        verify(hostDao).saveEntity(argThat(entity -> entity.getPassword().equals("encryptedPassword")));
        try {
            verify(emailService).sendMail(any(SendEmailDto.class));
        } catch (Exception e) {
            fail("Error inesperado al verificar envío de correo: " + e.getMessage());
        }

    }


    @Test
    @DisplayName("Debería lanzar excepción al intentar crear un anfitrión con email duplicado")
    void createHost_EmailAlreadyExists() {
        when(hostDao.existsByEmail(requestHostDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> hostService.createHost(requestHostDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un anfitrión");

        verify(hostDao, never()).saveEntity(any());
    }

    // GET BY ID
    @Test
    @DisplayName("Debería obtener un anfitrión por ID exitosamente")
    void getHostById_Success() {
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(hostMapper.hostEntityToHostDto(hostEntity)).thenReturn(responseHostDto);

        ResponseHostDto result = hostService.getHostById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Juan Pérez");
        verify(hostDao).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el anfitrión no existe por ID")
    void getHostById_NotFound() {
        when(hostDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hostService.getHostById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Anfitrión no encontrado");
    }

    // UPDATE HOST
    @Test
    @DisplayName("Debería actualizar un anfitrión exitosamente")
    void updateHost_Success() {
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(hostDao.updateEntity(hostEntity)).thenReturn(hostEntity);
        when(hostMapper.hostEntityToHostDto(hostEntity)).thenReturn(responseHostDto);

        ResponseHostDto result = hostService.updateHost(1L, updateHostDto);

        assertThat(result).isNotNull();
        verify(hostMapper).updateEntityFromDto(updateHostDto, hostEntity);
        verify(hostDao).updateEntity(hostEntity);
    }

    // DELETE HOST
    @Test
    @DisplayName("Debería desactivar un anfitrión sin alojamientos activos")
    void deleteHost_Success() {
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(hostDao.countActiveAccommodationsByHostId(1L)).thenReturn(0L);

        hostService.deleteHost(1L);

        assertThat(hostEntity.isActive()).isFalse();
        verify(hostDao).updateEntity(hostEntity);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar un anfitrión con alojamientos activos")
    void deleteHost_WithActiveAccommodations() {
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(hostDao.countActiveAccommodationsByHostId(1L)).thenReturn(2L);

        assertThatThrownBy(() -> hostService.deleteHost(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("alojamiento(s) activo(s)");

        verify(hostDao, never()).updateEntity(any());
    }


    // PASSWORD CHANGE

    @Test
    @DisplayName("Debería cambiar la contraseña exitosamente")
    void changePassword_Success() {
        ChangePasswordDto dto = new ChangePasswordDto("oldPass", "newPass", "newPass");
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(passwordEncoder.matches("oldPass", "encryptedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPass", "encryptedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        hostService.changePassword(1L, dto);

        verify(passwordEncoder).encode("newPass");
        verify(hostDao).updateEntity(hostEntity);
        assertThat(hostEntity.getPassword()).isEqualTo("encodedNewPass");
    }

    @Test
    @DisplayName("Debería lanzar excepción si la contraseña actual es incorrecta")
    void changePassword_InvalidCurrentPassword() {
        ChangePasswordDto dto = new ChangePasswordDto("wrongPass", "newPass", "newPass");
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));
        when(passwordEncoder.matches("wrongPass", "encryptedPassword")).thenReturn(false);

        assertThatThrownBy(() -> hostService.changePassword(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actual es incorrecta");

        verify(hostDao, never()).updateEntity(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si las nuevas contraseñas no coinciden")
    void changePassword_NewPasswordsDontMatch() {
        ChangePasswordDto dto = new ChangePasswordDto("oldPass", "newPass", "differentPass");
        when(hostDao.findById(1L)).thenReturn(Optional.of(hostEntity));

        assertThatThrownBy(() -> hostService.changePassword(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coinciden");
    }


    // VALIDACIONES EXTRA
    @Test
    @DisplayName("Debería retornar true si el email está en uso")
    void isEmailTaken_True() {
        when(hostDao.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = hostService.isEmailTaken("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Debería verificar si un anfitrión está activo")
    void isActiveHost_Success() {
        when(hostDao.isActiveById(1L)).thenReturn(true);

        boolean result = hostService.isActiveHost(1L);

        assertThat(result).isTrue();
        verify(hostDao).isActiveById(1L);
    }
}
