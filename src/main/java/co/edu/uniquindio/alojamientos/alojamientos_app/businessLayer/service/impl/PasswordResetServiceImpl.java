package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.RequestPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.VerifyPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    @Override
    public void requestPasswordReset(RequestPasswordResetDto request, String userType) {

    }

    @Override
    public void verifyAndResetPassword(VerifyPasswordResetDto request, String userType) {

    }
}
