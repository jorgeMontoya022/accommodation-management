package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JWTUtils jwtUtils;

    public AuthController(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "user@example.com");
        // Aquí podrías validar credenciales reales si quieres
        String token = jwtUtils.generateToken(email, Map.of("roles", "ROLE_USER"));
        // IMPORTANTE: el token va en 'message'
        return ResponseEntity.ok(new ResponseDto<>(false, token));
    }
}

