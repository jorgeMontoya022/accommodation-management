package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.GuestMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GuestDao {
    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    /**
     * Crear un nuevo huésped
     * <p>
     * FLUJO:
     * 1. CreateDTO → Entity (con Mapper)
     * 2. Guardar Entity en BD (con Repository)
     * 3. Entity guardada → DTO (con Mapper)
     * 4. Retornar DTO al Service
     */
    public ResponseGuestDto save(RequestGuestDto guestDto) {
        GuestEntity guestEntity = guestMapper.guestDtoToGuestEntity(guestDto);
        GuestEntity savedGuestEntity = guestRepository.save(guestEntity);
        return guestMapper.guestEntityToGuestDto(savedGuestEntity);
    }

    /**
     * Busca un huésped por su ID único.
     * Retorna un Optional con el GuestDto si existe.
     */
    public Optional<ResponseGuestDto> findById(Long id) {
        return guestRepository.findById(id)
                .map(guestMapper::guestEntityToGuestDto);
    }

    /**
     * Busca un huésped por su correo electrónico.
     * Retorna un Optional con el GuestDto si existe.
     */
    public Optional<ResponseGuestDto> findByEmail(String email) {
        return guestRepository.findByEmail(email)
                .map(guestMapper::guestEntityToGuestDto);
    }

    /**
     * Obtiene la lista de todos los huéspedes registrados.
     * Retorna una lista de GuestDto.
     */
    public List<ResponseGuestDto> findAll() {
        return guestMapper.getGuestsDto(guestRepository.findAll());
    }


    /**
     * Verifica si existe un huésped con el correo dado.
     * Retorna true si existe, false en caso contrario.
     */
    public boolean exitsByEmail(String email) {
        return guestRepository.exitsByEmail(email);
    }

    /**
     * Actualiza la información de un huésped existente por su ID.
     * Si se encuentra, aplica cambios desde el DTO y retorna el GuestDto actualizado.
     */
    public Optional<ResponseGuestDto> update(Long id, RequestGuestDto updateDto){
        return guestRepository.findById(id)
                .map(existingEntity -> {
                    guestMapper.updateEntityFromDto(updateDto, existingEntity);
                    GuestEntity guestUpdate = guestRepository.save(existingEntity);
                    return guestMapper.guestEntityToGuestDto(guestUpdate);
                });
    }

    /**
     * Eliminar huesped por ID
     *
     * RETORNA: boolean indicando si se eliminó
     * - true: Se eliminó exitosamente
     * - false: No existía el vendedor
     */
    public boolean deleteById(Long id) {
        if (guestRepository.existsById(id)) {
            guestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     *Buscar a los huespedes que tienen
     * reservas .
     */
    public List<ResponseGuestDto> findGuestsWithBookings() {
        List<GuestEntity> guestEntities = guestRepository.findGuestsWithBookings();
        return guestMapper.getGuestsDto(guestEntities);
    }

    public Long countBookingByGuestId(Long id) {
        return guestRepository.countBookingGuestById(id);
    }

    /**
     * Verifica si un huésped está activo
     * @param id ID del huésped
     * @return true si está activo, false si no existe o está inactivo
     */
    public boolean isActiveById(Long id) {
        return guestRepository.isActiveById(id).orElse(false);
    }
}
