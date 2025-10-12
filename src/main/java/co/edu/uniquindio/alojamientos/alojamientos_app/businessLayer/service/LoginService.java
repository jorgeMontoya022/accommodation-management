package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginResponseDto;

public interface LoginService {

    /**
     * Autenticar un huésped
     */
    LoginResponseDto loginGuest(LoginDto loginDto);

    /**
     * Autenticar un anfitrión
     */
    LoginResponseDto loginHost(LoginDto loginDto);
}
