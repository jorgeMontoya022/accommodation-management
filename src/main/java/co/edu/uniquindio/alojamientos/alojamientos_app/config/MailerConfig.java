package co.edu.uniquindio.alojamientos.alojamientos_app.config;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailerConfig {

    @Value("${mail.host:smtp.gmail.com}") private String host;
    @Value("${mail.port:587}") private int port;
    @Value("${mail.username}") private String username;
    @Value("${mail.password}") private String password;
    @Value("${mail.tls:true}") private boolean tlsEnabled;

    @Bean
    public Mailer mailer() {
        System.out.println("======== MAIL CONFIG ========");
        System.out.println("MAIL HOST = " + host);
        System.out.println("MAIL PORT = " + port);
        System.out.println("MAIL USERNAME = '" + username + "'");
        System.out.println("MAIL PASSWORD IS NULL? " + (password == null));
        System.out.println("MAIL PASSWORD LENGTH = " + (password != null ? password.length() : 0));
        System.out.println("MAIL TLS = " + tlsEnabled);
        System.out.println("=============================");

        return MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(tlsEnabled ? TransportStrategy.SMTP_TLS : TransportStrategy.SMTP)
                .withSessionTimeout(10000)
                .buildMailer();
    }

}
