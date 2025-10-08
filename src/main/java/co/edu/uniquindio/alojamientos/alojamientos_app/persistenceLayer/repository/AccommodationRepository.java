package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.AccommodationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<AccommodationEntity, Long> {

    @Query("SELECT COUNT(r) FROM BookingEntity r WHERE r.accommodationAssociated.id = :id")
    Long countBookingsByAccommodationId(@Param("id") Long id);

}
