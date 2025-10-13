package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.LoginService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final GuestDao guestDao;
    private final HostDao hostDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto loginGuest(LoginDto loginDto) {
        log.info("Intento de login para huésped con email: {}", loginDto.getEmail());

        GuestEntity guest = guestDao.findByEmailEntity(loginDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con email: {}", loginDto.getEmail());
                    return new RuntimeException("Email o contraseña incorrectos");
                });

        // Verificar que el huésped esté activo
        if (!guest.isActive()) {
            log.warn("Intento de login con cuenta inactiva. Email: {}", loginDto.getEmail());
            throw new RuntimeException("La cuenta del huésped está inactiva");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(loginDto.getPassword(), guest.getPassword())) {
            log.warn("Contraseña incorrecta para huésped: {}", loginDto.getEmail());
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        log.info("Login exitoso para huésped: {}", loginDto.getEmail());

        return LoginResponseDto.builder()
                .id(guest.getId())
                .email(guest.getEmail())
                .fullName(guest.getName())
                .userType("GUEST")
                .photoProfile(guest.getPhotoProfile())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto loginHost(LoginDto loginDto) {
        log.info("Intento de login para anfitrión con email: {}", loginDto.getEmail());

        HostEntity host = hostDao.findByEmailEntity(loginDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Anfitrión no encontrado con email: {}", loginDto.getEmail());
                    return new RuntimeException("Email o contraseña incorrectos");
                });

        // Verificar que el anfitrión esté activo
        if (!host.isActive()) {
            log.warn("Intento de login con cuenta inactiva. Email: {}", loginDto.getEmail());
            throw new RuntimeException("La cuenta del anfitrión está inactiva");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(loginDto.getPassword(), host.getPassword())) {
            log.warn("Contraseña incorrecta para anfitrión: {}", loginDto.getEmail());
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        log.info("Login exitoso para anfitrión: {}", loginDto.getEmail());

        return LoginResponseDto.builder()
                .id(host.getId())
                .email(host.getEmail())
                .fullName(host.getName())
                .userType("HOST")
                .photoProfile(host.getPhotoProfile())
                .build();
    }
}