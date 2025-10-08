package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<AccommodationEntity, Long> {

    /**
     * Buscar alojamientos por ciudad y estado ACTIVE
     */
    @Query("SELECT a FROM AccommodationEntity a WHERE a.city = :city AND a.statusAccommodation = 'ACTIVE'")
    List<AccommodationEntity> findByCity(@Param("city") String city);

    @Query("SELECT COUNT(r) FROM BookingEntity r WHERE r.accommodationAssociated.id = :id")
    Long countBookingsByAccommodationId(@Param("id") Long id);

}
