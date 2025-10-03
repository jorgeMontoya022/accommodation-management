package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accommodations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qualification_accommodation",nullable = false, length = 500)
    private String qualification;

    @Column(name = "description_accommodation", nullable = false, length = 1000)
    private String description;

    @Column(name = "city_accommodation", nullable = false, length = 100)
    private String city;

    @Column(name = "latitude_accommodation", nullable = false, length = 100)
    private String latitude;

    @Column(name = "length_accommodation", nullable = false, length = 100)
    private String length;

    @Column(name = "price_night_accommodation", nullable = false)
    private double priceNight;

    @Column(name = "maximux_capacity_accommodation", nullable = false)
    private int maximumCapacity;

    @Column(name = "date_creation_accommodation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_update_accommodation")
    private LocalDateTime dateUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_accommodation", nullable = false, length = 20)
    private StatusAccommodation statusAccommodation;

    @Enumerated(EnumType.STRING)
    @Column(name = "services_accommodation", nullable = false, length = 20)
    private TypeServicesEnum typeServicesEnum;

    @OneToMany(mappedBy = "accommodationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingEntity> bookingEntityList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private HostEntity hostEntity;

    // Relación One-to-Many: Un alojamiento tiene muchas imágenes (máximo 6)
    @OneToMany(mappedBy = "accommodationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ImageAccommodation> images = new ArrayList<>();

    // Relación One-to-Many: Un alojamiento tiene muchos comentarios
    @OneToMany(mappedBy = "accommodationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentEntity> comments = new ArrayList<>();

}
