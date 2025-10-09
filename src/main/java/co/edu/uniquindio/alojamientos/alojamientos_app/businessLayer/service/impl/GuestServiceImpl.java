package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.GuestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {
    private final GuestDao guestDao;
    private final GuestMapper guestMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseGuestDto createGuest(RequestGuestDto guestDto) {
        log.info("Creando nuevo huésped con email: {}", guestDto.getEmail());

        if (guestDao.existsByEmail(guestDto.getEmail())) {
            log.warn("Intento de crear huésped con email duplicado: {}", guestDto.getEmail());
            throw new IllegalArgumentException("Ya existe un huésped con el email: " + guestDto.getEmail());
        }

        // 1. Mapear DTO a Entity
        GuestEntity guestEntity = guestMapper.guestDtoToGuestEntity(guestDto);

        // 2. Encriptar la contraseña
        String encryptedPassword = passwordEncoder.encode(guestDto.getPassword());
        guestEntity.setPassword(encryptedPassword);

        // 3. Guardar
        ResponseGuestDto createdGuest = guestDao.saveEntity(guestEntity);
        log.info("Huésped creado exitosamente con ID: {}", createdGuest.getId());

        return createdGuest;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseGuestDto getGuestById(Long id) {
        GuestEntity guest = findGuestEntityById(id);
        ResponseGuestDto response = guestMapper.guestEntityToGuestDto(guest);

        // Si no hay foto pone una por defecto
        if (response.getPhotoProfile() == null) {
            response.setPhotoProfile("https://e7.pngegg.com/pngimages/867/694/png-clipart-user-profile-default-computer-icons-network-video-recorder-avatar-cartoon-maker-blue-text.png");
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GuestEntity getGuestEntityById(Long id) {
        return findGuestEntityById(id);
    }

    private GuestEntity findGuestEntityById(Long id) {
        return guestDao.findByIdEntity(id)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con ID: {}", id);
                    return new RuntimeException("Huésped no encontrado con ID: " + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseGuestDto getGuestByEmail(String email) {
        GuestEntity guest = guestDao.findByEmailEntity(email)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con email: {}", email);
                    return new RuntimeException("Huésped no encontrado con el email: " + email);
                });

        ResponseGuestDto response = guestMapper.guestEntityToGuestDto(guest);

        if (response.getPhotoProfile() == null) {
            response.setPhotoProfile("https://e7.pngegg.com/pngimages/867/694/png-clipart-user-profile-default-computer-icons-network-video-recorder-avatar-cartoon-maker-blue-text.png");
        }

        return response;
    }

    @Override
    public ResponseGuestDto updateGuest(Long id, UpdateGuestDto guestDto) {
        // 1. Obtener la entidad
        GuestEntity guest = findGuestEntityById(id);

        // 2. Aplicar cambios (el mapper actualiza los campos)
        guestMapper.updateEntityFromDto(guestDto, guest);

        // 3. Guardar (@PreUpdate automáticamente asigna dateUpdate)
        GuestEntity updated = guestDao.updateEntity(guest);

        log.info("Huésped actualizado exitosamente con ID: {}", id);

        ResponseGuestDto response = guestMapper.guestEntityToGuestDto(updated);

        if (response.getPhotoProfile() == null) {
            response.setPhotoProfile("https://e7.pngegg.com/pngimages/867/694/png-clipart-user-profile-default-computer-icons-network-video-recorder-avatar-cartoon-maker-blue-text.png");
        }

        return response;
    }

    @Override
    public void deleteGuest(Long id) {
        // 1. Obtener la entidad
        GuestEntity guest = findGuestEntityById(id);

        // 2. Verificar que no tiene reservas ACTIVAS
        Long activeBookingCount = guestDao.countActiveBookingByGuestId(id);

        if (activeBookingCount > 0) {
            log.warn("Intento de eliminar huésped con reservas activas. ID: {}, Reservas: {}", id, activeBookingCount);
            throw new IllegalStateException(
                    String.format("No se puede eliminar el huésped porque tiene %d reserva(s) activa(s)", activeBookingCount)
            );
        }

        // 3. Hacer soft delete: cambiar estado a inactivo
        guest.setActive(false);

        // 4. Guardar (@PreUpdate automáticamente asigna dateUpdate)
        guestDao.updateEntity(guest);

        log.info("Huésped desactivado exitosamente (soft delete) ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getGuestBookingCount(Long guestId) {
        findGuestEntityById(guestId); // Validar que existe
        return guestDao.countBookingByGuestId(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return guestDao.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveGuest(Long id) {
        log.info("Verificando si el huésped con ID {} está activo", id);
        boolean isActive = guestDao.isActiveById(id);
        log.info("Huésped con ID {}: {}", id, isActive ? "ACTIVO" : "INACTIVO");
        return isActive;
    }

    @Override
    public void changePassword(Long id, ChangePasswordDto changePasswordDto) {
        log.info("Cambiando contraseña para huésped ID: {}", id);

        GuestEntity guest = findGuestEntityById(id);

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
        }

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), guest.getPassword())) {
            log.warn("Intento de cambiar contraseña con contraseña actual incorrecta. ID: {}", id);
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), guest.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        String encryptedPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
        guest.setPassword(encryptedPassword);

        guestDao.updateEntity(guest);

        log.info("Contraseña cambiada exitosamente para huésped ID: {}", id);

    }
}