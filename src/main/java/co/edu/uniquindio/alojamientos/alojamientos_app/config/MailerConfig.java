package co.edu.uniquindio.alojamientos.alojamientos_app.config;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailerConfig {

    @Bean
    public Mailer mailer() {
        return MailerBuilder
                .withSMTPServer("smtp.gmail.com", 587, "SMTP_USERNAME", "SMTP_PASSWORD")
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer();
    }
}

