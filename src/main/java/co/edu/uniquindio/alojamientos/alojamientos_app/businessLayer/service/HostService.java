package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.GuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostDto;

public interface HostService {

    /**
     * Crear un nuevo anfitrión
     *
     * VALIDACIONES DE NEGOCIO:
     * - Email único
     * - Formato de datos correcto
     * - Reglas específicas del negocio
     *
     * @param hostDto Datos del huésped a crear
     * @return DTO del huésped creado con ID generado
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
     * @param email ID del anfitrión
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
     * @throws RuntimeException Si el anfitrión no existe o tiene alojamientos asociados
     */
    void deleteHost(Long id);


    /**
     * Verificar si un email ya está en uso
     *
     * CASO DE USO: Validación antes de crear/actualizar
     *
     * @param email Email a verificar
     * @return true si ya existe, false si está disponible
     */
    boolean isEmailTaken(String email);


}
