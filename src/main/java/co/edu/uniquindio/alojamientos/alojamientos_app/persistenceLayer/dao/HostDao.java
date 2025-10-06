package co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao;


import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.mapper.HostMapper;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.repository.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HostDao {
    private final HostRepository hostRepository;
    private final HostMapper hostMapper;
}
