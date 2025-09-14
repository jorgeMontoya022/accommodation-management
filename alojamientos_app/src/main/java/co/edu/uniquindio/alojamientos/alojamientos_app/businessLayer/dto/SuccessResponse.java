package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "Respuesta exitosa de la API")
public class SuccessResponse {
    @Schema(description = "Mensaje de éxito", example = "Usuario creado exitosamente")
    private String mensaje;

    @Schema(description = "ID del recurso creado", example = "abc123")
    private String id;

    public SuccessResponse() {
    }

    public SuccessResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public SuccessResponse(String mensaje, String id) {
        this.mensaje = mensaje;
        this.id = id;
    }

    // Respuesta de error
    @Schema(description = "Respuesta de error de la API")
    class ErrorResponse {

        @Schema(description = "Código de error", example = "VALIDATION_ERROR")
        private String error;

        @Schema(description = "Mensaje descriptivo del error", example = "Los datos proporcionados no son válidos")
        private String mensaje;

        public ErrorResponse() {
        }

        public ErrorResponse(String error, String mensaje) {
            this.error = error;
            this.mensaje = mensaje;
        }

        // Getters y Setters
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}
