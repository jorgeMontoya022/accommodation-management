package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hosts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostEntity extends UserEntity {

    // Relación One-to-Many: Un anfitrión tiene muchos alojamientos
    @OneToMany(mappedBy = "hostEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccommodationEntity> accommodationEntityList = new ArrayList<>();
}