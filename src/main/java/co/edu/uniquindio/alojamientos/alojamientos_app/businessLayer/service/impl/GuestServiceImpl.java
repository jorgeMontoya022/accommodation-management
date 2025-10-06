package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.GuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.GuestService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.GuestDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
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
    public GuestDto createGuest(GuestDto guestDto) {
        log.info("Creando nuevo vendedor con email: {}", guestDto.getEmail());

        // Validación de negocio: Email único

        if (guestDao.exitsByEmail(guestDto.getEmail())) {
            log.warn("Intento de crear huésped con email duplicado: {}", guestDto.getEmail());
            throw new IllegalArgumentException("Ya existe un huésped con el email: " + guestDto.getEmail());
        }

        validateGuestCreateData(guestDto);

        GuestDto createdGuest = guestDao.save(guestDto);
        log.info("Huésped creado exitosament con ID: {}", createdGuest.getId());
        return createdGuest;

    }

    //TODO: ¿Preguntar si es necesario tener esas validaciones nuevamente?
    private void validateGuestCreateData(GuestDto guestDto) {
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
    public GuestDto getGuestById(Long id) {
        return guestDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con ID: {}", id);
                    return new RuntimeException("Huésped no encontrado con ID: " + id);
                });
    }

    @Override
    public GuestDto getGuestByEmail(String email) {
        return guestDao.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Huésped no encontrado con ID: {}", email);
                    return new RuntimeException("Huésped no encontrado con el email: " + email);
                });
    }

    @Override
    public GuestDto updateGuest(Long id, GuestDto guestDto) {
        if (!guestDao.findById(id).isPresent()) {
            throw new RuntimeException("Vendedor no encontrado con ID: " + id);
        }
        validateGuestUpdateData(guestDto);
        return guestDao.update(id, guestDto).orElseThrow(() -> new RuntimeException("Error al actualizar al huésped"));
    }

    private void validateGuestUpdateData(GuestDto guestDto) {
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
        GuestDto guest = getGuestById(id);
        Long bookingCount = guestDao.countBookingByGuestId(id);
        if (bookingCount > 0) {
            log.warn("Intento de eliminar vendedor con productos. ID: {}, Reservas: {}", id, bookingCount);
            throw new IllegalStateException(
                    String.format("No se puede eliminar el huésped porque tiene %d producto(s) asociado(s)", bookingCount)
            );
        }

    }

    @Override
    public Long getGuestBookingCount(Long guestId) {
        return null;
    }

    @Override
    public boolean isEmailTaken(String email) {
        return false;
    }

    @Override
    public boolean isActiveGuest(Long id) {
        return false;
    }


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
