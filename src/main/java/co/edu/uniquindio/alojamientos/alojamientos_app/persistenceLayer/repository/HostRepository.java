package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository;

import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.entity.HostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HostRepository extends JpaRepository<HostEntity, Long> {

    /**
     * Busca un anfitrión por su email
     */
    Optional<HostEntity> findByEmail(String email);

    /**
     * Verifica si existe un anfitrión con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Busca anfitriones que tienen al menos un alojamiento
     */
    @Query("SELECT h FROM HostEntity h WHERE SIZE(h.accommodationEntityList) > 0")
    List<HostEntity> findHostsWithAccommodations();

    /**
     * Cuenta la cantidad de alojamientos que tiene un anfitrión
     */
    @Query("SELECT COUNT(a) FROM AccommodationEntity a WHERE a.hostEntity.id = :id")
    Long countAccommodationsByHostId(@Param("id") Long id);

    /**
     * Verifica si un anfitrión está activo
     */
    @Query("SELECT h.active FROM HostEntity h WHERE h.id = :id")
    Optional<Boolean> isActiveById(@Param("id") Long id);
}