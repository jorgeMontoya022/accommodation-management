package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.GuestServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.GuestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para GuestServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de Huésped de forma aislada
 * - No levanta contexto de Spring
 * - Usa Mockito para mockear dependencias
 * - Mantiene intactas las firmas/métodos del código productivo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GuestService - Unit Tests")
class GuestServiceImplTest {

    // ===== Dependencias mockeadas =====
    @Mock private GuestDao guestDao;
    @Mock private GuestMapper guestMapper;
    @Mock private PasswordEncoder passwordEncoder;

    // ===== SUT =====
    @InjectMocks
    private GuestServiceImpl guestService;

    // ===== Datos de prueba =====
    private RequestGuestDto requestGuest;
    private ResponseGuestDto responseGuest;
    private GuestEntity guestEntity;

    @BeforeEach
    void setUp() {
        requestGuest = new RequestGuestDto(
                "Juan Pérez",
                "juan@example.com",
                LocalDate.of(1990, 5, 15),
                "+57 3001234567",
                null,
                "Str0ng@Pass1!"
        );

        guestEntity = new GuestEntity();
        guestEntity.setId(1L);
        guestEntity.setName("Juan Pérez");
        guestEntity.setEmail("juan@example.com");
        guestEntity.setPassword("$bcrypt");
        guestEntity.setActive(true);

        responseGuest = new ResponseGuestDto(
                1L,
                "Juan Pérez",
                "juan@example.com",
                LocalDate.of(1990, 5, 15),
                "+57 3001234567",
                null, // sin foto para forzar default en get
                LocalDateTime.now(),
                true
        );
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("CREATE - Huésped válido retorna DTO con ID y encripta contraseña")
    void createGuest_Valid_ShouldReturnCreated() {
        when(guestDao.existsByEmail(requestGuest.getEmail())).thenReturn(false);
        when(guestMapper.guestDtoToGuestEntity(requestGuest)).thenReturn(guestEntity);
        when(passwordEncoder.encode(requestGuest.getPassword())).thenReturn("$encoded");
        when(guestDao.saveEntity(any(GuestEntity.class))).thenReturn(responseGuest);

        ResponseGuestDto result = guestService.createGuest(requestGuest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(guestDao).existsByEmail("juan@example.com");
        verify(passwordEncoder).encode("Str0ng@Pass1!");
        verify(guestDao).saveEntity(argThat(e ->
                "$encoded".equals(e.getPassword()) &&
                        "juan@example.com".equals(e.getEmail())));
    }

    @Test
    @DisplayName("CREATE - Email duplicado lanza IllegalArgumentException")
    void createGuest_DuplicatedEmail_ShouldThrow() {
        when(guestDao.existsByEmail(requestGuest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> guestService.createGuest(requestGuest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un huésped");

        verify(guestDao, never()).saveEntity(any());
    }

    // ==================== READ ====================

    @Test
    @DisplayName("READ - getGuestById retorna DTO y setea foto por defecto si es null")
    void getGuestById_ShouldSetDefaultPhoto_WhenNull() {
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(guestMapper.guestEntityToGuestDto(guestEntity)).thenReturn(responseGuest);

        ResponseGuestDto dto = guestService.getGuestById(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPhotoProfile()).isNotNull();
        assertThat(dto.getPhotoProfile()).startsWith("http");
    }

    @Test
    @DisplayName("READ - getGuestById inexistente lanza RuntimeException")
    void getGuestById_NonExisting_ShouldThrow() {
        when(guestDao.findByIdEntity(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.getGuestById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Huésped no encontrado");
    }

    @Test
    @DisplayName("READ - getGuestByEmail retorna DTO y setea foto por defecto si es null")
    void getGuestByEmail_ShouldSetDefaultPhoto_WhenNull() {
        when(guestDao.findByEmailEntity("juan@example.com")).thenReturn(Optional.of(guestEntity));
        when(guestMapper.guestEntityToGuestDto(guestEntity)).thenReturn(responseGuest);

        ResponseGuestDto dto = guestService.getGuestByEmail("juan@example.com");

        assertThat(dto).isNotNull();
        assertThat(dto.getEmail()).isEqualTo("juan@example.com");
        assertThat(dto.getPhotoProfile()).isNotNull();
    }

    @Test
    @DisplayName("READ - getGuestByEmail inexistente lanza RuntimeException")
    void getGuestByEmail_NonExisting_ShouldThrow() {
        when(guestDao.findByEmailEntity("nope@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.getGuestByEmail("nope@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Huésped no encontrado");
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("UPDATE - Actualiza campos permitidos y retorna DTO")
    void updateGuest_Valid_ShouldReturnUpdated() {
        UpdateGuestDto update = new UpdateGuestDto("Nuevo Nombre", "+57 3110000000", "https://img/pic.png");

        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        // Simulamos el mapper aplicando cambios permitidos
        doAnswer(inv -> {
            guestEntity.setName(update.getName());
            guestEntity.setPhone(update.getPhone());
            guestEntity.setPhotoProfile(update.getPhotoProfile());
            return null;
        }).when(guestMapper).updateEntityFromDto(eq(update), eq(guestEntity));
        when(guestDao.updateEntity(guestEntity)).thenReturn(guestEntity);

        ResponseGuestDto mapped = new ResponseGuestDto(
                1L, "Nuevo Nombre", "juan@example.com",
                LocalDate.of(1990, 5, 15),
                "+57 3110000000",
                "https://img/pic.png",
                LocalDateTime.now(),
                true
        );
        when(guestMapper.guestEntityToGuestDto(guestEntity)).thenReturn(mapped);

        ResponseGuestDto result = guestService.updateGuest(1L, update);

        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getEmail()).isEqualTo("juan@example.com"); // email no cambia
        verify(guestMapper).updateEntityFromDto(update, guestEntity);
        verify(guestDao).updateEntity(guestEntity);
    }

    @Test
    @DisplayName("UPDATE - Huésped inexistente lanza RuntimeException")
    void updateGuest_NonExisting_ShouldThrow() {
        when(guestDao.findByIdEntity(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.updateGuest(99L, new UpdateGuestDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Huésped no encontrado");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("DELETE - Con reservas activas lanza IllegalStateException")
    void deleteGuest_WithActiveBookings_ShouldThrow() {
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(guestDao.countActiveBookingByGuestId(1L)).thenReturn(2L);

        assertThatThrownBy(() -> guestService.deleteGuest(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reserva(s) activa(s)");

        verify(guestDao, never()).updateEntity(any());
    }

    @Test
    @DisplayName("DELETE - Sin reservas activas realiza soft delete")
    void deleteGuest_NoActiveBookings_ShouldSoftDelete() {
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(guestDao.countActiveBookingByGuestId(1L)).thenReturn(0L);
        when(guestDao.updateEntity(any(GuestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> guestService.deleteGuest(1L)).doesNotThrowAnyException();
        assertThat(guestEntity.isActive()).as("Debe marcarse inactivo").isFalse();
        verify(guestDao).updateEntity(guestEntity);
    }

    // ==================== QUERIES SIMPLES ====================

    @Test
    @DisplayName("COUNT - getGuestBookingCount delega en DAO tras validar existencia")
    void getGuestBookingCount_DelegatesToDao() {
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(guestDao.countBookingByGuestId(1L)).thenReturn(5L);

        Long count = guestService.getGuestBookingCount(1L);

        assertThat(count).isEqualTo(5L);
        verify(guestDao).findByIdEntity(1L);
        verify(guestDao).countBookingByGuestId(1L);
    }

    @Test
    @DisplayName("EMAIL TAKEN - isEmailTaken delega en DAO")
    void isEmailTaken_DelegatesToDao() {
        when(guestDao.existsByEmail("juan@example.com")).thenReturn(true);
        assertThat(guestService.isEmailTaken("juan@example.com")).isTrue();
        verify(guestDao).existsByEmail("juan@example.com");
    }

    @Test
    @DisplayName("ACTIVE - isActiveGuest delega en DAO")
    void isActiveGuest_DelegatesToDao() {
        when(guestDao.isActiveById(1L)).thenReturn(true);
        assertThat(guestService.isActiveGuest(1L)).isTrue();
        verify(guestDao).isActiveById(1L);
    }

    // ==================== CHANGE PASSWORD ====================

    @Test
    @DisplayName("CHANGE PASSWORD - Confirmación distinta lanza IllegalArgumentException")
    void changePassword_MismatchConfirmation_ShouldThrow() {
        ChangePasswordDto dto = new ChangePasswordDto("Curr#123", "New#12345", "Other#123");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));

        assertThatThrownBy(() -> guestService.changePassword(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coinciden");

        verify(guestDao, never()).updateEntity(any());
    }

    @Test
    @DisplayName("CHANGE PASSWORD - Contraseña actual incorrecta lanza IllegalArgumentException")
    void changePassword_WrongCurrent_ShouldThrow() {
        ChangePasswordDto dto = new ChangePasswordDto("Bad#123", "New#12345", "New#12345");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("Bad#123", guestEntity.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> guestService.changePassword(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actual es incorrecta");

        verify(guestDao, never()).updateEntity(any());
    }

    @Test
    @DisplayName("CHANGE PASSWORD - Nueva igual a la actual lanza IllegalArgumentException")
    void changePassword_SameAsCurrent_ShouldThrow() {
        ChangePasswordDto dto = new ChangePasswordDto("Curr#123", "Curr#123", "Curr#123");
        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("Curr#123", guestEntity.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> guestService.changePassword(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diferente a la actual");

        verify(guestDao, never()).updateEntity(any());
    }

    @Test
    @DisplayName("CHANGE PASSWORD - Flujo OK encripta y persiste")
    void changePassword_Ok_ShouldEncodeAndSave() {
        ChangePasswordDto dto = new ChangePasswordDto("Curr#123", "New#12345", "New#12345");

        when(guestDao.findByIdEntity(1L)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.matches("Curr#123", guestEntity.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("New#12345", guestEntity.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("New#12345")).thenReturn("$encodedNew");
        when(guestDao.updateEntity(any(GuestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> guestService.changePassword(1L, dto)).doesNotThrowAnyException();

        verify(passwordEncoder).encode("New#12345");
        verify(guestDao).updateEntity(argThat(e -> "$encodedNew".equals(e.getPassword())));
    }
}