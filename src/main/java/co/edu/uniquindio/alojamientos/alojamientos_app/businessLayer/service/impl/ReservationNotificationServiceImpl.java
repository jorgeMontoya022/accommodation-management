package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.ReservationNotificationDataDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.ReservationNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de notificaciones por correo para reservas.
 *
 * Importante:
 * - Se apoya en EmailService (que ya encapsula Simple Java Mail).
 * - Los errores al enviar correo se registran pero NO rompen el flujo de negocio
 *   (para no afectar la creación/cancelación de la reserva).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationServiceImpl implements ReservationNotificationService {

    private final EmailService emailService;

    @Override
    public void sendReservationCreatedNotifications(ReservationNotificationDataDto data) {
        // Correo al huésped: confirmación de reserva
        SendEmailDto guestMail = buildGuestConfirmationEmail(data);
        sendSafe(guestMail);

        // Correo al anfitrión: nueva reserva
        SendEmailDto hostMail = buildHostNewReservationEmail(data);
        sendSafe(hostMail);
    }

    @Override
    public void sendReservationCancelledNotifications(ReservationNotificationDataDto data) {
        // Correo al anfitrión: cancelación de reserva
        SendEmailDto hostMail = buildHostCancellationEmail(data);
        sendSafe(hostMail);
    }

    /**
     * Construye el correo de confirmación para el huésped.
     */
    private SendEmailDto buildGuestConfirmationEmail(ReservationNotificationDataDto data) {
        String subject = "Confirmación de reserva #" + safeId(data.getReservationId());

        String body = """
                Hola %s,

                Tu reserva en "%s" ha sido CONFIRMADA.

                Detalles de la reserva:
                - Alojamiento: %s
                - Dirección: %s, %s
                - Check-in: %s
                - Check-out: %s
                - Estado: %s

                Gracias por reservar con nosotros.

                Equipo Alojamientos
                """.formatted(
                nullSafe(data.getGuestName()),
                nullSafe(data.getAccommodationTitle()),
                nullSafe(data.getAccommodationTitle()),
                nullSafe(data.getAccommodationAddress()),
                nullSafe(data.getAccommodationCity()),
                nullSafeDate(data.getCheckInDate()),
                nullSafeDate(data.getCheckOutDate()),
                nullSafe(data.getStatus())
        );

        return SendEmailDto.builder()
                .recipient(data.getGuestEmail())
                .subject(subject)
                .body(body)
                .build();
    }

    /**
     * Construye el correo de nueva reserva para el anfitrión.
     */
    private SendEmailDto buildHostNewReservationEmail(ReservationNotificationDataDto data) {
        String subject = "Nueva reserva #" + safeId(data.getReservationId())
                + " en tu alojamiento \"" + nullSafe(data.getAccommodationTitle()) + "\"";

        String body = """
                Hola %s,

                Tienes una NUEVA RESERVA en tu alojamiento "%s".

                Detalles de la reserva:
                - Huésped: %s (%s)
                - Alojamiento: %s
                - Dirección: %s, %s
                - Check-in: %s
                - Check-out: %s
                - Estado: %s

                Ingresa a la plataforma para ver más detalles.

                Equipo Alojamientos
                """.formatted(
                nullSafe(data.getHostName()),
                nullSafe(data.getAccommodationTitle()),
                nullSafe(data.getGuestName()),
                nullSafe(data.getGuestEmail()),
                nullSafe(data.getAccommodationTitle()),
                nullSafe(data.getAccommodationAddress()),
                nullSafe(data.getAccommodationCity()),
                nullSafeDate(data.getCheckInDate()),
                nullSafeDate(data.getCheckOutDate()),
                nullSafe(data.getStatus())
        );

        return SendEmailDto.builder()
                .recipient(data.getHostEmail())
                .subject(subject)
                .body(body)
                .build();
    }

    /**
     * Construye el correo de cancelación para el anfitrión.
     */
    private SendEmailDto buildHostCancellationEmail(ReservationNotificationDataDto data) {
        String subject = "Cancelación de reserva #" + safeId(data.getReservationId());

        String body = """
                Hola %s,

                El huésped %s ha CANCELADO una reserva en tu alojamiento "%s".

                Detalles de la reserva cancelada:
                - Huésped: %s (%s)
                - Alojamiento: %s
                - Check-in: %s
                - Check-out: %s

                Te recomendamos revisar tu calendario en la plataforma.

                Equipo Alojamientos
                """.formatted(
                nullSafe(data.getHostName()),
                nullSafe(data.getGuestName()),
                nullSafe(data.getAccommodationTitle()),
                nullSafe(data.getGuestName()),
                nullSafe(data.getGuestEmail()),
                nullSafe(data.getAccommodationTitle()),
                nullSafeDate(data.getCheckInDate()),
                nullSafeDate(data.getCheckOutDate())
        );

        return SendEmailDto.builder()
                .recipient(data.getHostEmail())
                .subject(subject)
                .body(body)
                .build();
    }

    /**
     * Envía el correo y registra el error sin lanzar excepción,
     * para no romper el flujo de la reserva.
     */
    private void sendSafe(SendEmailDto dto) {
        if (dto == null || dto.getRecipient() == null || dto.getRecipient().isBlank()) {
            return;
        }
        try {
            emailService.sendMail(dto);
            log.info("Correo enviado a {}", dto.getRecipient());
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {}", dto.getRecipient(), e.getMessage(), e);
        }
    }

    // Helpers para evitar NPEs y mejorar legibilidad
    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String nullSafeDate(java.time.LocalDate date) {
        return (date != null) ? date.toString() : "-";
    }

    private String safeId(Long id) {
        return (id != null) ? String.valueOf(id) : "-";
    }
}
