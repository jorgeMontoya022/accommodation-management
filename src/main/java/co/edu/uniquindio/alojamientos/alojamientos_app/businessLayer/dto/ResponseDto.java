package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean error;
    private T message;
}
