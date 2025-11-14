package co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint entryPoint, JwtAuthFilter jwtAuthFilter) {
        this.entryPoint = entryPoint;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Autenticación / Swagger / registro legacy
                        .requestMatchers(
                                "/api/v1/auth/**","/api/v1/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/api/v1/register"
                        ).permitAll()

                        // ---- Públicos por controlador ----

                        // Guests (público)
                        .requestMatchers(HttpMethod.POST, "/api/v1/guests").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/guests/email-availability").permitAll()

                        // Hosts (público)
                        .requestMatchers(HttpMethod.POST, "/api/v1/hosts").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/hosts/email-availability").permitAll()

                        // Accommodation (consultas públicas)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation").permitAll()                 // listado paginado
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation/*").permitAll()              // detalle por id
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation/city/**").permitAll()        // por ciudad
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation/*/main-image").permitAll()   // imagen principal
                        .requestMatchers(HttpMethod.POST, "/api/v1/accommodation/*/images").permitAll()
                        // Comments (lectura pública)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/accommodation/*/ratings/average").permitAll()

                        // Reservations (solo detalle por id público; el resto autenticado)
                        .requestMatchers(HttpMethod.GET,  "/api/v1/reservations/*").permitAll()

                        // Todo lo demás requiere JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
