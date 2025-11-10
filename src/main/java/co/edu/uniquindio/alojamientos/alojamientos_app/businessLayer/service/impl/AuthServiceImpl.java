package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.AuthService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.TokenDao;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.TokenBlacklist;
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TokenDao tokenDao;
    private final JWTUtils jwtUtils;

    @Override
    public void logout(String token) {
        TokenBlacklist blacklisted = new TokenBlacklist();
        blacklisted.setToken(token);

        // convertir la expiración (Date -> LocalDateTime)
        try {
            // extraer expiración del token
            Instant expInstant = jwtUtils.parseJwt(token)
                    .getPayload()
                    .getExpiration()
                    .toInstant();
            blacklisted.setFechaExpiracion(LocalDateTime.ofInstant(expInstant, ZoneOffset.UTC));
        } catch (Exception e) {
            // si el token está vencido, se registra la hora actual
            blacklisted.setFechaExpiracion(LocalDateTime.now());
        }

        tokenDao.save(blacklisted);
    }
}
