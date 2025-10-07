package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;

public interface EmailService {
    void sendMail(SendEmailDto sendEmailDto) throws Exception;
}
