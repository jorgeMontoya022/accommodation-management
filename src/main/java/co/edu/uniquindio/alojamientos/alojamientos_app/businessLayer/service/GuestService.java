package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;


import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ChangePasswordDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.UpdateGuestDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.GuestEntity;

public interface GuestService {

    /**
     * Crear un nuevo huésped
     *
     * VALIDACIONES DE NEGOCIO:
     * - Email único
     * - Formato de datos correcto
     * - Reglas específicas del negocio
     *
     * @param guestDto Datos del huésped a crear
     * @return DTO del huésped creado con ID generado
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws RuntimeException Si el email ya existe
     */
    ResponseGuestDto createGuest(RequestGuestDto guestDto);


    /**
     * Buscar huésped por ID
     *
     * @param id ID del huésped
     * @return DTO del huésped encontrado
     * @throws RuntimeException Si el huésped no existe
     */
    ResponseGuestDto getGuestById(Long id);


    /**
     * Buscar huésped por email
     *
     * @param email ID del huésped
     * @return DTO del huésped encontrado
     * @throws RuntimeException Si el huésped no existe
     */
    ResponseGuestDto getGuestByEmail(String email);


    /**
     * Actualizar huésped existente
     *
     * VALIDACIONES:
     * - Huésoed debe existir
     * - Datos de actualización válidos
     * - No violar reglas de negocio
     *
     * @param id ID del huésped a actualizar
     * @param guestDto Datos a actualizar
     * @return DTO del huésped actualizado
     * @throws RuntimeException Si el huésped no existe
     */
    ResponseGuestDto updateGuest(Long id, UpdateGuestDto guestDto);


    /**
     * Eliminar huésped
     *
     * REGLAS DE NEGOCIO:
     * - Verificar si tiene reservas asociados
     * - Solo permitir eliminación si no tiene reservas
     * - O implementar eliminación en cascada según reglas de negocio
     *
     * @param id ID del huésped a eliminar
     * @throws RuntimeException Si el huésped no existe o tiene reservas asociados
     */
    void deleteGuest(Long id);


    /**
     * Obtener estadísticas del huésped
     *
     * CASO DE USO: Dashboard del huésped
     *
     * @param guestId ID del huésped
     * @return Número de productos del huésped
     */
    Long getGuestBookingCount(Long guestId);


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
     * Verificar si un huésped Está activo
     *
     * @param id id del huésped a verificar
     * @return true si está activo, false si no está activo
     */
    boolean isActiveGuest(Long id);

    GuestEntity getGuestEntityById(Long id);

    void changePassword(Long id, ChangePasswordDto changePasswordDto);

}
