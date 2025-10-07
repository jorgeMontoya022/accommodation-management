package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestHostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseHostDto;
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
     * <p>
     * FLUJO:
     * 1. CreateDTO → Entity (con Mapper)
     * 2. Guardar Entity en BD (con Repository)
     * 3. Entity guardada → DTO (con Mapper)
     * 4. Retornar DTO al Service
     */
    public ResponseHostDto save(RequestHostDto hostDto) {
        HostEntity hostEntity = hostMapper.hostDtoToHostEntity(hostDto);
        HostEntity savedHostEntity = hostRepository.save(hostEntity);
        return hostMapper.hostEntityToHostDto(savedHostEntity);
    }

    /**
     * Busca un anfitrión por su ID único.
     * Retorna un Optional con el HostDto si existe.
     */
    public Optional<ResponseHostDto> findById(Long id) {
        return hostRepository.findById(id)
                .map(hostMapper::hostEntityToHostDto);
    }

    /**
     * Busca un anfitrión por su correo electrónico.
     * Retorna un Optional con el HostDto si existe.
     */
    public Optional<ResponseHostDto> findByEmail(String email) {
        return hostRepository.findByEmail(email)
                .map(hostMapper::hostEntityToHostDto);
    }

    /**
     * Obtiene la lista de todos los anfitriones registrados.
     * Retorna una lista de HostDto.
     */
    public List<ResponseHostDto> findAll() {
        return hostMapper.getHostsDto(hostRepository.findAll());
    }

    /**
     * Verifica si existe un anfitrión con el correo dado.
     * Retorna true si existe, false en caso contrario.
     */
    public boolean existsByEmail(String email) {
        return hostRepository.existsByEmail(email);
    }

    /**
     * Actualiza la información de un anfitrión existente por su ID.
     * Si se encuentra, aplica cambios desde el DTO y retorna el HostDto actualizado.
     */
    public Optional<ResponseHostDto> update(Long id, RequestHostDto updateDto) {
        return hostRepository.findById(id)
                .map(existingEntity -> {
                    hostMapper.updateEntityFromDto(updateDto, existingEntity);
                    HostEntity hostUpdate = hostRepository.save(existingEntity);
                    return hostMapper.hostEntityToHostDto(hostUpdate);
                });
    }

    /**
     * Eliminar anfitrión por ID
     *
     * RETORNA: boolean indicando si se eliminó
     * - true: Se eliminó exitosamente
     * - false: No existía el anfitrión
     */
    public boolean deleteById(Long id) {
        if (hostRepository.existsById(id)) {
            hostRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Buscar a los anfitriones que tienen alojamientos.
     */
    public List<ResponseHostDto> findHostsWithAccommodations() {
        List<HostEntity> hostEntities = hostRepository.findHostsWithAccommodations();
        return hostMapper.getHostsDto(hostEntities);
    }

    /**
     * Cuenta la cantidad de alojamientos que tiene un anfitrión
     * @param id ID del anfitrión
     * @return Cantidad de alojamientos
     */
    public Long countAccommodationsByHostId(Long id) {
        return hostRepository.countAccommodationsByHostId(id);
    }

    /**
     * Verifica si un anfitrión está activo
     * @param id ID del anfitrión
     * @return true si está activo, false si no existe o está inactivo
     */
    public boolean isActiveById(Long id) {
        return hostRepository.isActiveById(id).orElse(false);
    }
}