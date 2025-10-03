package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@MappedSuperclass
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "date_birth")
    private LocalDate dateBirth;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "photo_profile", length = 500) //<--URL de la foto
    private String photoProfile;

    @Column(name = "date_register", nullable = false, updatable = false) //<-- No se puede actualizar
    private LocalDateTime dateRegister;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(nullable = false)
    private boolean active;


}
