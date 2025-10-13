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
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_chekin", nullable = false)
    private LocalDateTime dateCheckin;

    @Column(name = "date_checkout", nullable = false)
    private LocalDateTime dateCheckout;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_cancellation")
    private LocalDateTime dateCancellation;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_reservation", nullable = false, length = 20)
    private StatusReservation statusReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private AccommodationEntity accommodationAssociated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private GuestEntity guestEntity;

    @Column(name = "quantity_people", nullable = false)
    private int quantityPeople;

    @Column(name = "total_value", nullable = false)
    private double totalValue;

    @Column(name = "reason_cancellation", length = 500)
    private String reasonCancellation;

    // Relación One-to-Many: Una reserva puede tener un comentario (después de completarse)
    @OneToMany(mappedBy = "bookingEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentEntity> comments = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        // Fecha de creación y estado inicial
        this.dateCreation = LocalDateTime.now();
    }




}
