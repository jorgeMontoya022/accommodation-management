package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;


import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.HostDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.HostService;
import co.edu.uniquindio.alojamientos.alojamientos_app.persistenceLayer.dao.HostDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HostServicesImpl implements HostService {
    private final HostDao hostDao;

    @Override
    public HostDto createHost(HostDto hostDto) {
        return null;
    }

    @Override
    public HostDto getHostById(Long id) {
        return null;
    }

    @Override
    public HostDto getHostByEmail(String email) {
        return null;
    }

    @Override
    public HostDto updateHost(Long id, HostDto hostDto) {
        return null;
    }

    @Override
    public void deleteHost(Long id) {

    }

    @Override
    public boolean isEmailTaken(String email) {
        return false;
    }
}
