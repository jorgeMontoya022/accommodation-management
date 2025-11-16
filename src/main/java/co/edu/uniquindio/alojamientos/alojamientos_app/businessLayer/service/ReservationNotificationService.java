package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.ReservationNotificationDataDto;

/**
 * Servicio de alto nivel para manejar todas las notificaciones
 * por correo relacionadas con reservas.
 */
public interface ReservationNotificationService {

    /**
     * Enviar notificaciones cuando se crea / confirma una reserva.
     * Regla de negocio:
     * - Huésped recibe correo con detalles de la reserva.
     * - Anfitrión recibe notificación de nueva reserva.
     */
    void sendReservationCreatedNotifications(ReservationNotificationDataDto data);

    /**
     * Enviar notificaciones cuando se cancela una reserva.
     * Regla de negocio:
     * - Anfitrión recibe notificación de cancelación.
     */
    void sendReservationCancelledNotifications(ReservationNotificationDataDto data);
}
