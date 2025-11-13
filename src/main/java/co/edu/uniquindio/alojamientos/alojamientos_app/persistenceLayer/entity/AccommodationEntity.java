package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accommodations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE accommodations SET deleted = true, date_update_accommodation = NOW() WHERE id = ?")
@Where(clause = "deleted = false") // <- filtra registros marcados como eliminados
public class AccommodationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bloqueo optimista para evitar escrituras perdidas
    @Version
    private Long version;

    @Column(name = "qualification_accommodation", nullable = false, length = 500)
    private String qualification;

    @Column(name = "description_accommodation", nullable = false, length = 1000)
    private String description;

    @Column(name = "city_accommodation", nullable = false, length = 100)
    private String city;

    // Nota: mantengo String para no romper el esquema actual
    @Column(name = "latitude_accommodation", nullable = false, length = 100)
    private String latitude;

    // OJO: el nombre de columna 'length_accommodation' parece un typo de 'longitude'
    @Column(name = "length_accommodation", nullable = false, length = 100)
    private String longitude;

    @Column(name = "price_night_accommodation", nullable = false)
    private double priceNight;

    @Column(name = "maximux_capacity_accommodation", nullable = false)
    private int maximumCapacity;

    @Column(name = "date_creation_accommodation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_update_accommodation")
    private LocalDateTime dateUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_accommodation", nullable = false, length = 20)
    private StatusAccommodation statusAccommodation;

    // Comentario: Guardamos varios servicios en tabla secundaria (evita enum() vacío y permite múltiples valores)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "accommodation_services",
            joinColumns = @JoinColumn(name = "accommodation_id")
    )
    @Column(name = "service", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Set<TypeServicesEnum> services = new HashSet<>();

    // Relación correcta con BookingEntity: mappedBy debe apuntar al campo ManyToOne en BookingEntity
    @OneToMany(mappedBy = "accommodationAssociated", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingEntity> bookingEntityList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private HostEntity hostEntity;

    // Galería (máximo 6) — regla de negocio validada en el servicio correspondiente
    @OneToMany(mappedBy = "accommodationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ImageAccommodation> images = new ArrayList<>();

    @OneToMany(mappedBy = "accommodationEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentEntity> comments = new ArrayList<>();

    // Soft delete
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;


    @PrePersist
    public void onCreate() {
        // Fecha de creación y estado inicial
        this.dateCreation = LocalDateTime.now();
        this.statusAccommodation = StatusAccommodation.ACTIVE;
        this.deleted = false;
    }

    @PreUpdate
    public void onUpdate() {
        // Fecha de actualización
        this.dateUpdate = LocalDateTime.now();
    }
}
