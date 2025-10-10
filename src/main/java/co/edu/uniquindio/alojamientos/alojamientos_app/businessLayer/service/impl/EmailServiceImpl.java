package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.EmailService;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final Mailer mailer;

    // Constructor para inyección (mock o real)
    public EmailServiceImpl(Mailer mailer) {
        this.mailer = mailer;
    }

    @Override
    public void sendMail(SendEmailDto sendEmailDto) throws Exception {

        Email email = EmailBuilder.startingBlank()
                .from("test@domain.com") // correo válido de prueba
                //.from("SMTP_USERNAME")
                .to(sendEmailDto.getRecipient())
                .withSubject(sendEmailDto.getSubject())
                .withPlainText(sendEmailDto.getBody())
                .buildEmail();

        /*try (Mailer mailer = MailerBuilder
                .withSMTPServer("smtp.gmail.com", 587, "SMTP_USERNAME", "SMTP_PASSWORD")
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {*/

            mailer.sendMail(email);
        }

    }
