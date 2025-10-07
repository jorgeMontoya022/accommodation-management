package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {
    private final GuestDao guestDao;

    @Override
    public ResponseGuestDto createGuest(RequestGuestDto guestDto) {
        log.info("Creando nuevo vendedor con email: {}", guestDto.getEmail());

        // Validación de negocio: Email único

        if (guestDao.exitsByEmail(guestDto.getEmail())) {
            log.warn("Intento de crear huésped con email duplicado: {}", guestDto.getEmail());
            throw new IllegalArgumentException("Ya existe un huésped con el email: " + guestDto.getEmail());
        }

        validateGuestCreateData(guestDto);

        ResponseGuestDto createdGuest = guestDao.save(guestDto);
        log.info("Huésped creado exitosament con ID: {}", createdGuest.getId());

        return createdGuest;

    }

    //TODO: ¿Preguntar si es necesario tener esas validaciones nuevamente?
    private void validateGuestCreateData(RequestGuestDto guestDto) {
        if (guestDto.getName() == null || guestDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del huésped es obligatorio");
        }

        if (guestDto.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
        if (guestDto.getEmail() == null || guestDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email no es valido");
        }
        if (!isValidEmail(guestDto.getEmail())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        if (guestDto.getDateBirth() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento del huésped es obligatoria");
        }
        if (guestDto.getPhone() == null) {
            throw new IllegalArgumentException("El número de télefono de huésped es obligatorio");
        }

        if (!isValidPhone(guestDto.getPhone())) {
            throw new IllegalArgumentException("El formato del télefono no es valido");
        }


    }

    @Override
    @Transactional(readOnly = true)
    public ResponseGuestDto getGuestById(Long id) {

        ResponseGuestDto responseGuestDto = guestDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con ID: {}", id);
                    return new RuntimeException("Huésped no encontrado con ID: " + id);
                });

        //Si no hay foro pone una por defecto
        if(responseGuestDto.getPhotoProfile() == null){
            responseGuestDto.setPhotoProfile("https://e7.pngegg.com/pngimages/867/694/png-clipart-user-profile-default-computer-icons-network-video-recorder-avatar-cartoon-maker-blue-text.png");
        }

        return responseGuestDto;

    }

    @Override
    public ResponseGuestDto getGuestByEmail(String email) {
        return guestDao.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con ID: {}", email);
                    return new RuntimeException("Huésped no encontrado con el email: " + email);
                });
    }

    @Override
    public ResponseGuestDto updateGuest(Long id, RequestGuestDto guestDto) {
        if (!guestDao.findById(id).isPresent()) {
            throw new RuntimeException("Vendedor no encontrado con ID: " + id);
        }
        validateGuestUpdateData(guestDto);
        return guestDao.update(id, guestDto).orElseThrow(() -> new RuntimeException("Error al actualizar al huésped"));
    }

    private void validateGuestUpdateData(RequestGuestDto guestDto) {
        if (guestDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (guestDto.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }

        if (guestDto.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }

        if (guestDto.getPhone().length() > 15) {
            throw new IllegalArgumentException("El teléfono no puede exceder 15 caracteres");
        }
    }

    @Override
    public void deleteGuest(Long id) {
        ResponseGuestDto guest = getGuestById(id);
        Long bookingCount = guestDao.countBookingByGuestId(id);
        if (bookingCount > 0) {
            log.warn("Intento de eliminar huésped con reservas. ID: {}, Reservas: {}", id, bookingCount);
            throw new IllegalStateException(
                    String.format("No se puede eliminar el huésped porque tiene %d reserva(s) asociada(s)", bookingCount)
            );
        }
        boolean deleted = guestDao.deleteById(id);
        if (!deleted) {
            throw new RuntimeException("Error al eliminar al huésped con ID: " + id);
        }
        log.info("Huésped eliminado exitosamente ID: {}", id);

    }

    @Override
    @Transactional(readOnly = true)
    public Long getGuestBookingCount(Long guestId) {
        getGuestById(guestId);
        return guestDao.countBookingByGuestId(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return guestDao.exitsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveGuest(Long id) {
        log.info("Verificando si el huésped con ID {} está activo", id);
        boolean isActive = guestDao.isActiveById(id);
        log.info("Huésped con ID {}: {}", id, isActive ? "ACTIVO" : "INACTIVO");
        return isActive;
    }


    // Métodos auxiliares de validación
    private boolean isValidPhone(String phone) {
        Pattern patron = Pattern.compile("^\\+?57?[0-9\\s\\-\\(\\)]{7,15}$");
        Matcher matcher = patron.matcher(phone);
        return matcher.matches();
    }


    private boolean isValidEmail(String email) {
        Pattern patron = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = patron.matcher(email);
        return matcher.find();
    }
}
