package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RequestPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.VerifyPasswordResetDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/v1/password-reset")
@RequiredArgsConstructor
@Tag(name = "Restablecimiento de contraseña")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "Solicitar código de restablecimiento de contraseña")
    @PostMapping("/request")
    public ResponseEntity<?> request(@Valid @RequestBody RequestPasswordResetDto dto) {
        passwordResetService.requestReset(dto.getEmail());
        return ResponseEntity.ok(Map.of("mensaje", "Si el email existe, se ha enviado un código de restablecimiento"));
    }

    @Operation(summary = "Verificar código y establecer nueva contraseña")
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyPasswordResetDto dto) {
        passwordResetService.verifyReset(dto);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida correctamente"));
    }

}
