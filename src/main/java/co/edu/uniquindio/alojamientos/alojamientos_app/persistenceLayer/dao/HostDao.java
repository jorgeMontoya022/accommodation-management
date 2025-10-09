package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.HostMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HostDao {
    private final HostRepository hostRepository;
    private final HostMapper hostMapper;

    /**
     * Crear un nuevo anfitrión
     * @PrePersist se encarga automáticamente de:
     * - dateRegister = now()
     * - active = true
     */
    public ResponseHostDto save(RequestHostDto hostDto) {
        HostEntity hostEntity = hostMapper.hostDtoToHostEntity(hostDto);
        HostEntity savedHostEntity = hostRepository.save(hostEntity);
        return hostMapper.hostEntityToHostDto(savedHostEntity);
    }

    /**
     * Guardar una nueva entidad de host
     * @PrePersist se encarga automáticamente de dateRegister y active=true
     */
    public ResponseHostDto saveEntity(HostEntity hostEntity) {
        HostEntity saved = hostRepository.save(hostEntity);
        return hostMapper.hostEntityToHostDto(saved);
    }

    /**
     * Obtener anfitrión por ID (retorna Entity)
     */
    public Optional<HostEntity> findById(Long id) {
        return hostRepository.findById(id);
    }

    /**
     * Obtener anfitrión por email (retorna Entity)
     */
    public Optional<HostEntity> findByEmailEntity(String email) {
        return hostRepository.findByEmail(email);
    }

    /**
     * Obtener anfitrión por email (retorna DTO)
     */
    public Optional<ResponseHostDto> findByEmail(String email) {
        return hostRepository.findByEmail(email)
                .map(hostMapper::hostEntityToHostDto);
    }

    /**
     * Listar todos los anfitriones activos
     */
    public List<ResponseHostDto> findAll() {
        return hostMapper.getHostsDto(hostRepository.findAll());
    }

    /**
     * Verificar si existe email
     */
    public boolean existsByEmail(String email) {
        return hostRepository.existsByEmail(email);
    }

    /**
     * Actualizar anfitrión
     * @PreUpdate se encarga automáticamente de asignar dateUpdate
     */
    public Optional<ResponseHostDto> update(Long id, UpdateHostDto updateDto) {
        return hostRepository.findById(id)
                .map(existingEntity -> {
                    hostMapper.updateEntityFromDto(updateDto, existingEntity);
                    HostEntity hostUpdate = hostRepository.save(existingEntity);
                    return hostMapper.hostEntityToHostDto(hostUpdate);
                });
    }

    /**
     * Guardar una entidad de host (usado después de modificarla)
     * @PreUpdate se encarga automáticamente de asignar dateUpdate
     */
    public HostEntity updateEntity(HostEntity hostEntity) {
        return hostRepository.save(hostEntity);
    }

    /**
     * Eliminar anfitrión (hard delete) - No recomendado
     * En su lugar, usar soft delete en el Service
     */
    public boolean deleteById(Long id) {
        if (hostRepository.existsById(id)) {
            hostRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Buscar anfitriones con alojamientos activos
     */
    public List<ResponseHostDto> findHostsWithAccommodations() {
        List<HostEntity> hostEntities = hostRepository.findHostsWithAccommodations();
        return hostMapper.getHostsDto(hostEntities);
    }

    /**
     * Contar TODOS los alojamientos de un host (activos e inactivos)
     */
    public Long countAccommodationsByHostId(Long id) {
        return hostRepository.countAccommodationsByHostId(id);
    }

    /**
     * Contar solo los alojamientos ACTIVOS de un host
     */
    public Long countActiveAccommodationsByHostId(Long id) {
        return hostRepository.findById(id)
                .map(host -> host.getAccommodationEntityList().stream()
                        .filter(acc -> acc.getStatusAccommodation().name().equals("ACTIVE"))
                        .count())
                .orElse(0L);
    }

    /**
     * Verificar si un anfitrión está activo
     */
    public boolean isActiveById(Long id) {
        return hostRepository.isActiveById(id).orElse(false);
    }
}