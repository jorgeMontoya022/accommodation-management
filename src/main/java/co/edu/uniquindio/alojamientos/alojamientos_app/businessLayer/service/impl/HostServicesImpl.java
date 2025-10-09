package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.HostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HostServicesImpl implements HostService {
    private final HostDao hostDao;
    private final HostMapper hostMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ResponseHostDto createHost(RequestHostDto hostDto) {
        log.info("Creando nuevo anfitrión con email: {}", hostDto.getEmail());

        // Validación de negocio: Email único
        if (hostDao.existsByEmail(hostDto.getEmail())) {
            log.warn("Intento de crear anfitrión con email duplicado: {}", hostDto.getEmail());
            throw new IllegalArgumentException("Ya existe un anfitrión con el email: " + hostDto.getEmail());
        }

        // 1. Mapear DTO a Entity
        HostEntity hostEntity = hostMapper.hostDtoToHostEntity(hostDto);

        // 2. Encriptar la contraseña antes de guardar
        String encryptedPassword = passwordEncoder.encode(hostDto.getPassword());
        hostEntity.setPassword(encryptedPassword);

        // 3. Guardar usando el DAO
        ResponseHostDto createdHost = hostDao.saveEntity(hostEntity);

        log.info("Anfitrión creado exitosamente con ID: {}", createdHost.getId());

        try {
            SendEmailDto welcomeEmail = SendEmailDto.builder()
                    .recipient(createdHost.getEmail())
                    .subject("¡Bienvenido a Alojamientos Úniquindío!")
                    .body(buildWelcomeEmailBody(createdHost.getName()))
                    .build();
            emailService.sendMail(welcomeEmail);
            log.info("Email de bienvenida enviado a: {}", createdHost.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida para anfitrión ID: {}", createdHost.getId(), e);
        }
        return createdHost;
    }

    private String buildWelcomeEmailBody(String name) {
        return "¡Hola " + name + "!\n\n" +
                "Bienvenido a Alojamientos Úniquindío.\n" +
                "Tu cuenta ha sido creada exitosamente.\n\n" +
                "Ahora puedes comenzar a crear tus alojamientos y recibir huéspedes.\n\n" +
                "Si tienes alguna pregunta, no dudes en contactarnos.\n\n" +
                "Saludos,\n" +
                "El equipo de Alojamientos Úniquindío";
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseHostDto getHostById(Long id) {
        HostEntity host = findHostEntityById(id);
        return hostMapper.hostEntityToHostDto(host);
    }

    @Override
    @Transactional(readOnly = true)
    public HostEntity getHostEntityById(Long id) {
        return findHostEntityById(id);
    }

    private HostEntity findHostEntityById(Long id) {
        return hostDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Anfitrión no encontrado con ID: {}", id);
                    return new RuntimeException("Anfitrión no encontrado con ID: " + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseHostDto getHostByEmail(String email) {
        HostEntity host = hostDao.findByEmailEntity(email)
                .orElseThrow(() -> {
                    log.warn("Anfitrión no encontrado con email: {}", email);
                    return new RuntimeException("Anfitrión no encontrado con el email: " + email);
                });
        return hostMapper.hostEntityToHostDto(host);
    }

    @Override
    public ResponseHostDto updateHost(Long id, UpdateHostDto hostDto) {
        // 1. Obtener la entidad
        HostEntity host = findHostEntityById(id);

        // 2. Aplicar cambios (el mapper actualiza los campos)
        hostMapper.updateEntityFromDto(hostDto, host);

        // 3. Guardar (automáticamente @PreUpdate asigna dateUpdate)
        HostEntity updated = hostDao.updateEntity(host);

        log.info("Anfitrión actualizado exitosamente con ID: {}", id);

        return hostMapper.hostEntityToHostDto(updated);
    }

    @Override
    public void deleteHost(Long id) {
        // 1. Obtener la entidad
        HostEntity host = findHostEntityById(id);

        // 2. Verificar que no tiene alojamientos ACTIVOS
        Long activeAccommodationCount = hostDao.countActiveAccommodationsByHostId(id);

        if (activeAccommodationCount > 0) {
            log.warn("Intento de eliminar anfitrión con alojamientos activos. ID: {}, Alojamientos: {}", id, activeAccommodationCount);
            throw new IllegalStateException(
                    String.format("No se puede eliminar el anfitrión porque tiene %d alojamiento(s) activo(s)", activeAccommodationCount)
            );
        }

        // 3. Hacer soft delete: cambiar estado a inactivo
        host.setActive(false);

        // 4. Guardar (automáticamente @PreUpdate asigna dateUpdate)
        hostDao.updateEntity(host);

        log.info("Anfitrión desactivado exitosamente (soft delete) ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getHostAccommodationCount(Long hostId) {
        findHostEntityById(hostId); // Validar que existe
        return hostDao.countAccommodationsByHostId(hostId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return hostDao.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveHost(Long id) {
        log.info("Verificando si el anfitrión con ID {} está activo", id);
        boolean isActive = hostDao.isActiveById(id);
        log.info("Anfitrión con ID {}: {}", id, isActive ? "ACTIVO" : "INACTIVO");
        return isActive;
    }

    @Override
    public void changePassword(Long id, ChangePasswordDto changePasswordDto) {

        log.info("Cambiando contraseña para anfitrión ID: {}", id);

        HostEntity host = findHostEntityById(id);

        if(!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
        }

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), host.getPassword())) {
            log.warn("Intento de cambiar contraseña con contraseña actual incorrecta. ID: {}", id);
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), host.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        String encryptedPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        host.setPassword(encryptedPassword);

        hostDao.updateEntity(host);

        log.info("Contraseña cambiada exitosamente para anfitrión ID: {}", id);

    }
}