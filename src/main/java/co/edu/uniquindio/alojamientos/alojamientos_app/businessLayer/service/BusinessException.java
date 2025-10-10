package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service;

/** Excepción para reglas de negocio */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
