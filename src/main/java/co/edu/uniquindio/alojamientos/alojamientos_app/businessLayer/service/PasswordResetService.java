package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.VerifyPasswordResetDto;

public interface PasswordResetService {

    void requestReset(String email);

    void verifyReset(VerifyPasswordResetDto dto);
}
