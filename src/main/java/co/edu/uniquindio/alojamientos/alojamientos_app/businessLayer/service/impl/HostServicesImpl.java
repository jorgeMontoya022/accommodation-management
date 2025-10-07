package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
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
public class HostServicesImpl implements HostService {
    private final HostDao hostDao;

    @Override
    public RequestHostDto createHost(RequestHostDto hostDto) {
        log.info("Creando nuevo anfitrión con email: {}", hostDto.getEmail());

        // Validación de negocio: Email único
        if (hostDao.existsByEmail(hostDto.getEmail())) {
            log.warn("Intento de crear anfitrión con email duplicado: {}", hostDto.getEmail());
            throw new IllegalArgumentException("Ya existe un anfitrión con el email: " + hostDto.getEmail());
        }

        validateHostCreateData(hostDto);

        RequestHostDto createdHost = hostDao.save(hostDto);
        log.info("Anfitrión creado exitosamente con ID: {}", createdHost.getId());
        return createdHost;
    }

    private void validateHostCreateData(RequestHostDto hostDto) {
        if (hostDto.getName() == null || hostDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del anfitrión es obligatorio");
        }

        if (hostDto.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }

        if (hostDto.getEmail() == null || hostDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email no es válido");
        }

        if (!isValidEmail(hostDto.getEmail())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        if (hostDto.getDateBirth() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento del anfitrión es obligatoria");
        }

        if (hostDto.getPhone() == null) {
            throw new IllegalArgumentException("El número de teléfono del anfitrión es obligatorio");
        }

        if (!isValidPhone(hostDto.getPhone())) {
            throw new IllegalArgumentException("El formato del teléfono no es válido");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RequestHostDto getHostById(Long id) {
        return hostDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Anfitrión no encontrado con ID: {}", id);
                    return new RuntimeException("Anfitrión no encontrado con ID: " + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public RequestHostDto getHostByEmail(String email) {
        return hostDao.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Anfitrión no encontrado con email: {}", email);
                    return new RuntimeException("Anfitrión no encontrado con el email: " + email);
                });
    }

    @Override
    public RequestHostDto updateHost(Long id, RequestHostDto hostDto) {
        if (!hostDao.findById(id).isPresent()) {
            throw new RuntimeException("Anfitrión no encontrado con ID: " + id);
        }
        validateHostUpdateData(hostDto);
        return hostDao.update(id, hostDto)
                .orElseThrow(() -> new RuntimeException("Error al actualizar al anfitrión"));
    }

    private void validateHostUpdateData(RequestHostDto hostDto) {
        if (hostDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        if (hostDto.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }

        if (hostDto.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }

        if (hostDto.getPhone().length() > 15) {
            throw new IllegalArgumentException("El teléfono no puede exceder 15 caracteres");
        }
    }

    @Override
    public void deleteHost(Long id) {
        RequestHostDto host = getHostById(id);
        Long accommodationCount = hostDao.countAccommodationsByHostId(id);

        if (accommodationCount > 0) {
            log.warn("Intento de eliminar anfitrión con alojamientos. ID: {}, Alojamientos: {}", id, accommodationCount);
            throw new IllegalStateException(
                    String.format("No se puede eliminar el anfitrión porque tiene %d alojamiento(s) asociado(s)", accommodationCount)
            );
        }

        boolean deleted = hostDao.deleteById(id);
        if (!deleted) {
            throw new RuntimeException("Error al eliminar al anfitrión con ID: " + id);
        }
        log.info("Anfitrión eliminado exitosamente ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getHostAccommodationCount(Long hostId) {
        getHostById(hostId);
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