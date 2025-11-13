package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "images_accommodation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageAccommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "is_principal", nullable = false)
    private boolean isPrincipal; //Indica si es la imagen principal

    @Column(name = "display_order")
    private Integer displayOrder; //orden en que irían las imagenes (1-6)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private AccommodationEntity accommodationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_accommodation", nullable = false)
    private AccommodationEntity accommodation;
}
