package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.GuestMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GuestDao {
    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    /**
     * Crear un nuevo huésped
     * @PrePersist se encarga automáticamente de dateRegister y active=true
     */
    public ResponseGuestDto save(RequestGuestDto guestDto) {
        GuestEntity guestEntity = guestMapper.guestDtoToGuestEntity(guestDto);
        GuestEntity savedGuestEntity = guestRepository.save(guestEntity);
        return guestMapper.guestEntityToGuestDto(savedGuestEntity);
    }

    /**
     * Obtener huésped por ID (retorna DTO)
     */
    public Optional<ResponseGuestDto> findById(Long id) {
        return guestRepository.findById(id)
                .map(guestMapper::guestEntityToGuestDto);
    }

    /**
     * Obtener huésped por ID (retorna Entity)
     */
    public Optional<GuestEntity> findByIdEntity(Long id) {
        return guestRepository.findById(id);
    }

    /**
     * Obtener huésped por email (retorna Entity)
     */
    public Optional<GuestEntity> findByEmailEntity(String email) {
        return guestRepository.findByEmail(email);
    }

    /**
     * Obtener huésped por email (retorna DTO)
     */
    public Optional<ResponseGuestDto> findByEmail(String email) {
        return guestRepository.findByEmail(email)
                .map(guestMapper::guestEntityToGuestDto);
    }

    /**
     * Verificar si existe email
     */
    public boolean existsByEmail(String email) {
        return guestRepository.exitsByEmail(email);
    }

    /**
     * Actualizar huésped
     * @PreUpdate se encarga automáticamente de asignar dateUpdate
     */
    public Optional<ResponseGuestDto> update(Long id, UpdateGuestDto updateDto) {
        return guestRepository.findById(id)
                .map(existingEntity -> {
                    guestMapper.updateEntityFromDto(updateDto, existingEntity);
                    GuestEntity guestUpdate = guestRepository.save(existingEntity);
                    return guestMapper.guestEntityToGuestDto(guestUpdate);
                });
    }

    /**
     * Guardar una entidad de guest (usado después de modificarla)
     * @PreUpdate se encarga automáticamente de asignar dateUpdate
     */
    public GuestEntity updateEntity(GuestEntity guestEntity) {
        return guestRepository.save(guestEntity);
    }

    /**
     * Contar TODAS las reservas de un huésped
     */
    public Long countBookingByGuestId(Long id) {
        return guestRepository.findById(id)
                .map(guest -> (long) guest.getBookingEntityList().size())
                .orElse(0L);
    }

    /**
     * Contar solo las reservas ACTIVAS de un huésped
     */
    public Long countActiveBookingByGuestId(Long id) {
        return guestRepository.findById(id)
                .map(guest -> guest.getBookingEntityList().stream()
                        .filter(booking -> booking.getStatusReservation().name().equals("CONFIRMED")
                                || booking.getStatusReservation().name().equals("PENDING"))
                        .count())
                .orElse(0L);
    }

    /**
     * Verificar si un huésped está activo
     */
    public boolean isActiveById(Long id) {
        return guestRepository.findById(id)
                .map(GuestEntity::isActive)
                .orElse(false);
    }
}