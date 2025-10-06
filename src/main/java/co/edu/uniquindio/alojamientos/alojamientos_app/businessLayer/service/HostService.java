package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostDto;

/**
 * Servicio para la gestión de anfitriones
 *
 * Responsabilidades:
 * - CRUD de anfitriones
 * - Validaciones de reglas de negocio
 * - Gestión de estado y estadísticas
 */
public interface HostService {

    /**
     * Crear un nuevo anfitrión
     *
     * VALIDACIONES DE NEGOCIO:
     * - Email único
     * - Formato de datos correcto
     * - Reglas específicas del negocio
     *
     * @param hostDto Datos del anfitrión a crear
     * @return DTO del anfitrión creado con ID generado
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws RuntimeException Si el email ya existe
     */
    HostDto createHost(HostDto hostDto);

    /**
     * Buscar anfitrión por ID
     *
     * @param id ID del anfitrión
     * @return DTO del anfitrión encontrado
     * @throws RuntimeException Si el anfitrión no existe
     */
    HostDto getHostById(Long id);

    /**
     * Buscar anfitrión por email
     *
     * @param email Email del anfitrión
     * @return DTO del anfitrión encontrado
     * @throws RuntimeException Si el anfitrión no existe
     */
    HostDto getHostByEmail(String email);

    /**
     * Actualizar anfitrión existente
     *
     * VALIDACIONES:
     * - Anfitrión debe existir
     * - Datos de actualización válidos
     * - No violar reglas de negocio
     *
     * @param id ID del anfitrión a actualizar
     * @param hostDto Datos a actualizar
     * @return DTO del anfitrión actualizado
     * @throws RuntimeException Si el anfitrión no existe
     * @throws IllegalArgumentException Si los datos no son válidos
     */
    HostDto updateHost(Long id, HostDto hostDto);

    /**
     * Eliminar anfitrión
     *
     * REGLAS DE NEGOCIO:
     * - Verificar si tiene alojamientos asociados
     * - Solo permitir eliminación si no tiene alojamientos
     * - O implementar eliminación en cascada según reglas de negocio
     *
     * @param id ID del anfitrión a eliminar
     * @throws RuntimeException Si el anfitrión no existe
     * @throws IllegalStateException Si el anfitrión tiene alojamientos asociados
     */
    void deleteHost(Long id);

    /**
     * Obtener estadísticas del anfitrión
     *
     * CASO DE USO: Dashboard del anfitrión
     *
     * @param hostId ID del anfitrión
     * @return Número de alojamientos del anfitrión
     * @throws RuntimeException Si el anfitrión no existe
     */
    Long getHostAccommodationCount(Long hostId);

    /**
     * Verificar si un email ya está en uso
     *
     * CASO DE USO: Validación antes de crear/actualizar
     *
     * @param email Email a verificar
     * @return true si ya existe, false si está disponible
     */
    boolean isEmailTaken(String email);

    /**
     * Verificar si un anfitrión está activo
     *
     * CASO DE USO: Validación de permisos y acceso
     *
     * @param id ID del anfitrión a verificar
     * @return true si está activo, false si no está activo o no existe
     */
    boolean isActiveHost(Long id);
}