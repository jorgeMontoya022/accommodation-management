package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.LoginResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.LoginServiceImpl; // ✅ impl
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth") //  alíneado con SecurityConfig
@Tag(name = "Autenticación", description = "Login de huéspedes y anfitriones")
@RequiredArgsConstructor
public class AuthController {

    private final LoginServiceImpl loginService; //  inyectar la implementación
    private final JWTUtils jwtUtils;

    @Operation(summary = "Login huésped")
    @PostMapping("/guest/login")
    public ResponseEntity<LoginResponseDto> loginGuest(@Valid @RequestBody LoginDto request) {
        // 1) valida credenciales contra DB
        LoginResponseDto info = loginService.loginGuest(request);

        // 2) genera JWT con subject=id y roles para el filtro
        String token = jwtUtils.generateToken(
                String.valueOf(info.getId()),
                Map.of(
                        "email", info.getEmail(),
                        "name", info.getFullName(),
                        "roles", "ROLE_GUEST" // JwtAuthFilter espera "roles"
                )
        );

        // 3) token en Authorization y datos en body
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(info);
    }

    @Operation(summary = "Login anfitrión")
    @PostMapping("/host/login")
    public ResponseEntity<LoginResponseDto> loginHost(@Valid @RequestBody LoginDto request) {
        LoginResponseDto info = loginService.loginHost(request);

        String token = jwtUtils.generateToken(
                String.valueOf(info.getId()),
                Map.of(
                        "email", info.getEmail(),
                        "name", info.getFullName(),
                        "roles", "ROLE_HOST"
                )
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(info);
    }
}
