package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.TokenBlacklist;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenDao {

    private final TokenBlacklistRepository repository;

    public void save(TokenBlacklist token) {
        repository.save(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return repository.existsByToken(token);
    }
}
