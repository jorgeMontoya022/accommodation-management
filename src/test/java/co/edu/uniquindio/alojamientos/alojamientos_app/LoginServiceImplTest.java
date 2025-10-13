package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.LoginServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceImplTest {

    @Mock
    private GuestDao guestDao;

    @Mock
    private HostDao hostDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginServiceImpl loginService;

    private LoginDto loginDto;
    private GuestEntity guestEntity;
    private HostEntity hostEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loginDto = new LoginDto("juan@example.com", "Password123!");

        guestEntity = new GuestEntity();
        guestEntity.setId(1L);
        guestEntity.setEmail("juan@example.com");
        guestEntity.setPassword("$2a$10$encodedpassword");
        guestEntity.setName("Juan Pérez");
        guestEntity.setPhotoProfile("foto.png");
        guestEntity.setActive(true);

        hostEntity = new HostEntity();
        hostEntity.setId(2L);
        hostEntity.setEmail("ana@example.com");
        hostEntity.setPassword("$2a$10$encodedpassword");
        hostEntity.setName("Ana Gómez");
        hostEntity.setPhotoProfile("hostfoto.png");
        hostEntity.setActive(true);
    }

    // TESTS DE HUÉSPED

    @Test
    @DisplayName("Login exitoso de huésped")
    void loginGuest_Success() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("Password123!", guestEntity.getPassword())).thenReturn(true);

        LoginResponseDto response = loginService.loginGuest(loginDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("juan@example.com", response.getEmail());
        assertEquals("Juan Pérez", response.getFullName());
        assertEquals("GUEST", response.getUserType());
    }

    @Test
    @DisplayName("Error: huésped no encontrado")
    void loginGuest_NotFound() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginGuest(loginDto));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    @Test
    @DisplayName("Error: huésped inactivo")
    void loginGuest_Inactive() {
        guestEntity.setActive(false);
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.of(guestEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginGuest(loginDto));
        assertEquals("La cuenta del huésped está inactiva", ex.getMessage());
    }

    @Test
    @DisplayName("Error: contraseña incorrecta huésped")
    void loginGuest_WrongPassword() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("Password123!", guestEntity.getPassword())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginGuest(loginDto));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    // TESTS DE ANFITRIÓN

    @Test
    @DisplayName("Login exitoso de anfitrión")
    void loginHost_Success() {
        loginDto.setEmail("ana@example.com");
        when(hostDao.findByEmailEntity("ana@example.com")).thenReturn(Optional.of(hostEntity));
        when(passwordEncoder.matches("Password123!", hostEntity.getPassword())).thenReturn(true);

        LoginResponseDto response = loginService.loginHost(loginDto);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("ana@example.com", response.getEmail());
        assertEquals("Ana Gómez", response.getFullName());
        assertEquals("HOST", response.getUserType());
    }

    @Test
    @DisplayName("Error: anfitrión no encontrado")
    void loginHost_NotFound() {
        when(hostDao.findByEmailEntity("ana@example.com")).thenReturn(Optional.empty());
        loginDto.setEmail("ana@example.com");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginHost(loginDto));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    @Test
    @DisplayName("Error: anfitrión inactivo")
    void loginHost_Inactive() {
        hostEntity.setActive(false);
        loginDto.setEmail("ana@example.com");
        when(hostDao.findByEmailEntity("ana@example.com")).thenReturn(Optional.of(hostEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginHost(loginDto));
        assertEquals("La cuenta del anfitrión está inactiva", ex.getMessage());
    }

    @Test
    @DisplayName("Error: contraseña incorrecta anfitrión")
    void loginHost_WrongPassword() {
        loginDto.setEmail("ana@example.com");
        when(hostDao.findByEmailEntity("ana@example.com")).thenReturn(Optional.of(hostEntity));
        when(passwordEncoder.matches("Password123!", hostEntity.getPassword())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.loginHost(loginDto));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }
}
