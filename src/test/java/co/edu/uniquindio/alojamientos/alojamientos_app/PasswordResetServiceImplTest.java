package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.RequestPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.VerifyPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.PasswordResetServiceImpl;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.PasswordResetToken;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.PasswordResetTokenRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HostDao hostDao;

    @Mock
    private GuestDao guestDao;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private final String hostEmail = "host@example.com";
    private final String guestEmail = "guest@example.com";

    private HostEntity hostEntity;
    private GuestEntity guestEntity;

    @BeforeEach
    void setup() {
        hostEntity = new HostEntity();
        hostEntity.setEmail(hostEmail);

        guestEntity = new GuestEntity();
        guestEntity.setEmail(guestEmail);
    }

    // =======================
    // Tests para requestPasswordReset
    // =======================
    @Test
    void requestPasswordReset_HostUser_Success() throws Exception {
        RequestPasswordResetDto request = new RequestPasswordResetDto(hostEmail);

        when(hostDao.findByEmailEntity(hostEmail)).thenReturn(Optional.of(hostEntity));
        when(tokenRepository.findByEmailAndUserType(hostEmail, "HOST")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(request, "HOST");

        // Verificar que se haya llamado a save y sendMail
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendMail(any());
    }

    @Test
    void requestPasswordReset_GuestUser_Success() throws Exception{
        RequestPasswordResetDto request = new RequestPasswordResetDto(guestEmail);

        when(guestDao.findByEmailEntity(guestEmail)).thenReturn(Optional.of(guestEntity));
        when(tokenRepository.findByEmailAndUserType(guestEmail, "GUEST")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(request, "GUEST");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendMail(any());
    }

    @Test
    void requestPasswordReset_UserNotFound_ThrowsException() {
        RequestPasswordResetDto request = new RequestPasswordResetDto("noexist@example.com");

        when(hostDao.findByEmailEntity("noexist@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                passwordResetService.requestPasswordReset(request, "HOST")
        );

        assertEquals("Anfitrión no encontrado con este email", exception.getMessage());
    }

    // =======================
    // Tests para verifyAndResetPassword
    // =======================
    @Test
    void verifyAndResetPassword_PasswordsDoNotMatch_ThrowsException() {
        VerifyPasswordResetDto request = new VerifyPasswordResetDto(
                hostEmail, "123456", "pass1", "pass2"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.verifyAndResetPassword(request, "HOST")
        );

        assertEquals("Las contraseñas no coinciden", exception.getMessage());
    }

    @Test
    void verifyAndResetPassword_TokenInvalid_ThrowsException() {
        VerifyPasswordResetDto request = new VerifyPasswordResetDto(
                hostEmail, "123456", "password123", "password123"
        );

        when(tokenRepository.findByTokenAndEmailAndUsedFalse("123456", hostEmail))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.verifyAndResetPassword(request, "HOST")
        );

        assertEquals("Código inválido o expirado", exception.getMessage());
    }

    @Test
    void verifyAndResetPassword_TokenExpired_ThrowsException() {
        VerifyPasswordResetDto request = new VerifyPasswordResetDto(
                hostEmail, "123456", "password123", "password123"
        );

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(hostEmail);
        token.setToken("123456");
        token.setUserType("HOST");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // ya expirado
        token.setUsed(false);

        when(tokenRepository.findByTokenAndEmailAndUsedFalse("123456", hostEmail))
                .thenReturn(Optional.of(token));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.verifyAndResetPassword(request, "HOST")
        );

        assertEquals("El código ha expirado", exception.getMessage());
        assertTrue(token.isUsed()); // token marcado como usado
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyAndResetPassword_Host_Success() throws Exception{
        VerifyPasswordResetDto request = new VerifyPasswordResetDto(
                hostEmail, "123456", "newPass123", "newPass123"
        );

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(hostEmail);
        token.setToken("123456");
        token.setUserType("HOST");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        when(tokenRepository.findByTokenAndEmailAndUsedFalse("123456", hostEmail))
                .thenReturn(Optional.of(token));
        when(hostDao.findByEmailEntity(hostEmail)).thenReturn(Optional.of(hostEntity));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedPass");

        passwordResetService.verifyAndResetPassword(request, "HOST");

        assertTrue(token.isUsed());
        verify(hostDao).updateEntity(hostEntity);
        verify(emailService).sendMail(any());
    }

    @Test
    void verifyAndResetPassword_Guest_Success() throws Exception{
        VerifyPasswordResetDto request = new VerifyPasswordResetDto(
                guestEmail, "654321", "guestPass123", "guestPass123"
        );

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(guestEmail);
        token.setToken("654321");
        token.setUserType("GUEST");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        when(tokenRepository.findByTokenAndEmailAndUsedFalse("654321", guestEmail))
                .thenReturn(Optional.of(token));
        when(guestDao.findByEmailEntity(guestEmail)).thenReturn(Optional.of(guestEntity));
        when(passwordEncoder.encode("guestPass123")).thenReturn("encodedGuestPass");

        passwordResetService.verifyAndResetPassword(request, "GUEST");

        assertTrue(token.isUsed());
        verify(guestDao).updateEntity(guestEntity);
        verify(emailService).sendMail(any());
    }
}
