package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 estrellas

    @Column(name = "text", nullable = false, length = 1000)
    private String tex;

    @Column(name = "host_response", length = 1000)
    private String hostResponse;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_response")
    private LocalDateTime dateResponse;

    // Relación Many-to-One: Muchos comentarios pertenecen a un alojamiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private AccommodationEntity accommodationEntity;

    // Relación Many-to-One: Muchos comentarios son escritos por un huésped (autor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private GuestEntity authorGuest;

    // Relación Many-to-One: El comentario está asociado a una reserva completada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity bookingEntity;
}
