package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.RequestPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.VerifyPasswordResetDto;

public interface PasswordResetService {

    /**
     * Solicita un código de recuperación de contraseña
     * @param request DTO con el email
     * @param userType "HOST" o "GUEST"
     */
    void requestPasswordReset(RequestPasswordResetDto request, String userType);

    /**
     * Verifica el código y cambia la contraseña
     * @param request DTO con email, código y nueva contraseña
     * @param userType "HOST" o "GUEST"
     */
    void verifyAndResetPassword(VerifyPasswordResetDto request, String userType);
}
