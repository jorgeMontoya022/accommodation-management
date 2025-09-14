package co.edu.uniquindio.alojamientos.alojamientos_app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Plataforma Alojamiento",
                version = "1.0.0",
                description = "API para gestión de usuarios, alojamientos, reservas, comentarios y notificaciones."
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor local"),
                @Server(url = "https://api.plataforma.com", description = "Servidor de producción")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el token JWT obtenido del login"
)

public class OpenApiConfig {
}
