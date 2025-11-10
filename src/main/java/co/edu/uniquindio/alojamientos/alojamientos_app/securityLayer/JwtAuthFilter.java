package co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.TokenDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JWTUtils jwtUtils;

    public JwtAuthFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (log.isDebugEnabled()) {
            log.debug("Authorization header: {}", header);
        }


        // Si no hay Bearer, seguimos la cadena sin tocar el contexto
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si ya hay un Authentication válido, no lo volvemos a setear
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            // 1) Parsear JWT
            Jws<Claims> jws = jwtUtils.parseJwt(token);
            Claims claims = jws.getPayload();

            // 2) Principal = subject (tu id en el token)
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("JWT sin 'sub' (subject)");
            }

            // 3) Authorities desde claim "roles" (acepta String "A,B" o List ["A","B"])
            Collection<? extends GrantedAuthority> authorities = toAuthorities(claims.get("roles"));

            // 4) Crear Authentication y ponerlo en el contexto
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (IllegalArgumentException e) {
            log.warn("Error leyendo JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.warn("Fallo inesperado validando JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> toAuthorities(@Nullable Object rolesClaim) {
        if (rolesClaim == null) return List.of();

        if (rolesClaim instanceof String s) {
            if (s.isBlank()) return List.of();
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        if (rolesClaim instanceof Collection<?> col) {
            return col.stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // Cualquier otro tipo: lo convertimos a un único rol
        return List.of(new SimpleGrantedAuthority(String.valueOf(rolesClaim).trim()));
    }
}
