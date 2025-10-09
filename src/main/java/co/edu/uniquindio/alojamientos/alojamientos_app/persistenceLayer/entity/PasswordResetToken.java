package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token; // Código de 6 dígitos

    @Column(nullable = false)
    private String email; // Email del usuario

    @Column(name = "user_type", nullable = false)
    private String userType; // "HOST" o "GUEST"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 15 minutos desde creación

    @Column(name = "used", nullable = false)
    private boolean used = false; // Si ya fue utilizado

    @Column(name = "used_at")
    private LocalDateTime usedAt; // Cuándo se usó

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(15); // Válido por 15 minutos
    }
}
