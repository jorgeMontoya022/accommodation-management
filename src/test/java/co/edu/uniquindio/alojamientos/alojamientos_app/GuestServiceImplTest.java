package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.GuestServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.GuestMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Gestión de Huéspedes")
@Feature("Servicios de Huésped")
@ExtendWith(MockitoExtension.class)
class GuestServiceImplTest {

    @Mock
    private GuestDao guestDao;

    @Mock
    private GuestMapper guestMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GuestServiceImpl guestService;

    private GuestEntity guestEntity;
    private RequestGuestDto requestGuestDto;
    private ResponseGuestDto responseGuestDto;

    @BeforeEach
    void setUp() {
        guestEntity = new GuestEntity();
        guestEntity.setId(1L);
        guestEntity.setName("Juan Pérez");
        guestEntity.setEmail("juan@example.com");
        guestEntity.setPassword("encodedPass");

        requestGuestDto = new RequestGuestDto();
        requestGuestDto.setName("Juan Pérez");
        requestGuestDto.setEmail("juan@example.com");
        requestGuestDto.setPassword("12345678");

        responseGuestDto = new ResponseGuestDto();
        responseGuestDto.setId(1L);
        responseGuestDto.setName("Juan Pérez");
        responseGuestDto.setEmail("juan@example.com");
    }


    // createGuest() - Éxito
    @Test
    @Story("Crear huésped exitosamente")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se cree un huésped correctamente cuando el email no está registrado.")
    @DisplayName("Crear huésped exitosamente")
    void testCreateGuest_Success() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.empty());
        when(guestMapper.guestDtoToGuestEntity(requestGuestDto)).thenReturn(guestEntity);
        when(passwordEncoder.encode("12345678")).thenReturn("encodedPass");
        when(guestDao.saveEntity(any(GuestEntity.class))).thenReturn(responseGuestDto);
        when(guestMapper.guestEntityToGuestDto(guestEntity)).thenReturn(responseGuestDto);

        ResponseGuestDto result = guestService.createGuest(requestGuestDto);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        verify(guestDao, times(1)).saveEntity(any(GuestEntity.class));
    }

    // createGuest() - Email duplicado
    @Test
    @Story("Intentar crear huésped con email existente")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se lance una excepción cuando se intenta registrar un huésped con un email existente.")
    @DisplayName("Crear huésped con email duplicado")
    void testCreateGuest_EmailAlreadyExists() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.of(guestEntity));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> guestService.createGuest(requestGuestDto));

        assertTrue(exception.getMessage().contains("ya registrado"));
        verify(guestDao, never()).saveEntity(any());
    }

    // changePassword() - Contraseña actual incorrecta
    @Test
    @Story("Cambio de contraseña fallido: contraseña actual incorrecta")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se lance excepción cuando la contraseña actual no coincide.")
    @DisplayName("🔒 Contraseña actual incorrecta")
    void testChangePassword_IncorrectCurrentPassword() {
        ChangePasswordDto dto = new ChangePasswordDto("wrong", "Nueva1234", "Nueva1234");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("wrong", guestEntity.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> guestService.changePassword(1L, dto));

        assertTrue(exception.getMessage().contains("incorrecta"));
        verify(guestDao, never()).updateEntity(any());
    }

    // changePassword() - Nueva igual a la actual
    @Test
    @Story("Cambio de contraseña fallido: nueva igual a la actual")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que no se permita cambiar la contraseña si la nueva es igual a la anterior.")
    @DisplayName("🔁 Nueva igual a la actual")
    void testChangePassword_SameAsCurrentPassword() {
        ChangePasswordDto dto = new ChangePasswordDto("12345678", "12345678", "12345678");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("12345678", guestEntity.getPassword())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> guestService.changePassword(1L, dto));

        assertTrue(exception.getMessage().contains("igual a la actual"));
        verify(guestDao, never()).updateEntity(any());
    }

    // changePassword() - Confirmación no coincide
    @Test
    @Story("Cambio de contraseña fallido: confirmación no coincide")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se lance una excepción si la confirmación no coincide con la nueva contraseña.")
    @DisplayName("Confirmación de contraseña incorrecta")
    void testChangePassword_ConfirmationMismatch() {
        ChangePasswordDto dto = new ChangePasswordDto("12345678", "Nueva1234", "Diferente1234");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("12345678", guestEntity.getPassword())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> guestService.changePassword(1L, dto));

        assertTrue(exception.getMessage().contains("no coincide"));
        verify(guestDao, never()).updateEntity(any());
    }

    // changePassword() - Éxito
    @Test
    @Story("Cambio de contraseña exitoso")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifica que se cambie correctamente la contraseña y se guarde en la base de datos.")
    @DisplayName("✅ Cambio de contraseña exitoso")
    void testChangePassword_Success() {
        ChangePasswordDto dto = new ChangePasswordDto("oldPass", "NewPass123", "NewPass123");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("oldPass", guestEntity.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("encodedNewPass");

        guestService.changePassword(1L, dto);

        verify(passwordEncoder).encode("NewPass123");
        verify(guestDao).updateEntity(guestEntity);
        assertEquals("encodedNewPass", guestEntity.getPassword());
    }
}
