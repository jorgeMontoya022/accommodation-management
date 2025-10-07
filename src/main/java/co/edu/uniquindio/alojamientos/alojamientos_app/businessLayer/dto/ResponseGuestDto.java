package co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del huésped")
public class ResponseGuestDto {


    private Long id;

    private String name;


    private String email;


    private LocalDate dateBirth;

    private String phone;


    private String photoProfile;


    private LocalDateTime dateRegister;


    private boolean active;

}
