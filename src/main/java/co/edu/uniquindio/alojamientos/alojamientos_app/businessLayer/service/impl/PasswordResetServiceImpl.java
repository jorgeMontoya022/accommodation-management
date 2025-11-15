package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.VerifyPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.PasswordResetService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.PasswordResetToken;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.GuestRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.HostRepository;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {
    private final GuestRepository guestRepository;
    private final HostRepository hostRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void requestReset(String email) {
        String userType;

        if (guestRepository.existsByEmail(email)) {
            userType = "GUEST";
        } else if (hostRepository.existsByEmail(email)) {
            userType = "HOST";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ese email");
        }

        // Invalidar tokens anteriores (opcional)
        tokenRepository.findByEmailAndUserType(email, userType)
                .ifPresent(t -> {
                    t.setUsed(true);
                    tokenRepository.save(t);
                });

        String code = generateCode();

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(code);
        token.setEmail(email);
        token.setUserType(userType);
        // createdAt y expiresAt se setean en @PrePersist
        tokenRepository.save(token);

        // TODO: enviar email con el código 'code'
        // Mientras tanto, en desarrollo, puedes devolver el code en la respuesta del controller si quieres probar rápido.
    }

    @Override
    public void verifyReset(VerifyPasswordResetDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña y la confirmación no coinciden");
        }

        PasswordResetToken token = tokenRepository
                .findByTokenAndEmailAndUsedFalse(dto.getCode(), dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ha expirado");
        }

        if (token.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ya fue utilizado");
        }

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

        switch (token.getUserType().toUpperCase()) {
            case "GUEST" -> {
                GuestEntity guest = guestRepository.findByEmail(dto.getEmail())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Huésped no encontrado"));
                guest.setPassword(encodedPassword);
                guestRepository.save(guest);
            }
            case "HOST" -> {
                HostEntity host = hostRepository.findByEmail(dto.getEmail())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anfitrión no encontrado"));
                host.setPassword(encodedPassword);
                hostRepository.save(host);
            }
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tipo de usuario no soportado");
        }

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900_000) + 100_000; // 100000 - 999999
        return String.valueOf(num);
    }
}