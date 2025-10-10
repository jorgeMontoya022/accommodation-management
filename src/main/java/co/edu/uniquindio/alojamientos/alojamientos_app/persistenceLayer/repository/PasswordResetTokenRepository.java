package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Buscar token activo no expirado
     */
    Optional<PasswordResetToken> findByTokenAndEmailAndUsedFalse(String token, String email);

    /**
     * Buscar tokens por email
     */
    Optional<PasswordResetToken> findByEmailAndUserType(String email, String userType);
}
