package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.RequestPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.VerifyPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.PasswordResetService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.PasswordResetToken;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final HostDao hostDao;
    private final GuestDao guestDao;

    @Override
    public void requestPasswordReset(RequestPasswordResetDto request, String userType) {
        log.info("Solicitando reset de contraseña para email: {} tipo: {}", request.getEmail(), userType);

        // 1. Validar que el usuario existe
        validateUserExists(request.getEmail(), userType);

        // 2. Generar código de 6 dígitos
        String code = generateResetCode();

        // 3. Invalidar tokens anteriores no usados
        tokenRepository.findByEmailAndUserType(request.getEmail(), userType)
                .ifPresent(oldToken -> {
                    if (!oldToken.isUsed()) {
                        oldToken.setUsed(true);
                        tokenRepository.save(oldToken);
                    }
                });
        // 4. Crear nuevo token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(code);
        resetToken.setEmail(request.getEmail());
        resetToken.setUserType(userType);

        PasswordResetToken savedToken = tokenRepository.save(resetToken);

        log.info("Token de reset creado para: {}", request.getEmail());

        // 5. Enviar email con código
        try {
            SendEmailDto resetEmail = SendEmailDto.builder()
                    .recipient(request.getEmail())
                    .subject("Código para recuperar tu contraseña")
                    .body(buildPasswordResetEmailBody(code))
                    .build();

            emailService.sendMail(resetEmail);
            log.info("Email de reset enviado a: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de reset", e);
            throw new RuntimeException("Error al enviar el email de recuperación");
        }
    }

    @Override
    public void verifyAndResetPassword(VerifyPasswordResetDto request, String userType) {
        log.info("Verificando código de reset para email: {}", request.getEmail());

        // 1. Validar que las contraseñas coinciden
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // 2. Buscar token válido
        PasswordResetToken resetToken = tokenRepository
                .findByTokenAndEmailAndUsedFalse(request.getCode(), request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Código inválido o expirado para: {}", request.getEmail());
                    return new IllegalArgumentException("Código inválido o expirado");
                });

        // 3. Validar que no ha expirado (15 minutos)
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            throw new IllegalArgumentException("El código ha expirado");
        }

        // 4. Actualizar contraseña del usuario
        String encryptedPassword = passwordEncoder.encode(request.getNewPassword());

        if ("HOST".equals(userType)) {
            HostEntity host = hostDao.findByEmailEntity(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Anfitrión no encontrado"));
            host.setPassword(encryptedPassword);
            hostDao.updateEntity(host);
            log.info("Contraseña de anfitrión actualizada: {}", request.getEmail());
        } else if ("GUEST".equals(userType)) {
            GuestEntity guest = guestDao.findByEmailEntity(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Huésped no encontrado"));
            guest.setPassword(encryptedPassword);
            guestDao.updateEntity(guest);
            log.info("Contraseña de huésped actualizada: {}", request.getEmail());
        }

        // 5. Marcar token como usado
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // 6. Enviar email de confirmación
        try {
            SendEmailDto confirmationEmail = SendEmailDto.builder()
                    .recipient(request.getEmail())
                    .subject("Tu contraseña ha sido actualizada")
                    .body("Tu contraseña ha sido actualizada exitosamente.\n\n" +
                            "Si no realizaste este cambio, contacta con nosotros inmediatamente.\n\n" +
                            "Saludos,\n" +
                            "El equipo de Alojamientos Úniquindío")
                    .build();

            emailService.sendMail(confirmationEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación", e);
        }
    }

    private void validateUserExists(String email, String userType) {
        if ("HOST".equals(userType)) {
            hostDao.findByEmailEntity(email)
                    .orElseThrow(() -> new RuntimeException("Anfitrión no encontrado con este email"));
        } else if ("GUEST".equals(userType)) {
            guestDao.findByEmailEntity(email)
                    .orElseThrow(() -> new RuntimeException("Huésped no encontrado con este email"));
        }
    }

    private String generateResetCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        return String.valueOf(code);
    }

    private String buildPasswordResetEmailBody(String code) {
        return "Tu código para recuperar la contraseña es:\n\n" +
                "     " + code + "\n\n" +
                "Este código es válido por 15 minutos.\n\n" +
                "Si no solicitaste recuperar tu contraseña, ignora este email.\n\n" +
                "Saludos,\n" +
                "El equipo de Alojamientos Úniquindío";
    }
}
