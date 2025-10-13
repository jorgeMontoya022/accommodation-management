package co.edu.uniquindio.alojamientos.alojamientos_app.presentationLayer.controller;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.ResponseDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.securityLayer.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthTestController {

    @RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final JWTUtils jwtUtils;

        // GET /api/auth/dev-token -> devuelve un token simple
        @GetMapping("/dev-token")
        public ResponseEntity<ResponseDto<String>> devToken() {
            String token = jwtUtils.generateToken(
                    "dev-user",
                    Map.of("roles", "ROLE_USER", "email", "dev@local")
            );
            return ResponseEntity.ok(new ResponseDto<>(false, token));
        }

        // POST /api/auth/login  -> ejemplo simple (sin base de datos)
        @PostMapping("/login")
        public ResponseEntity<ResponseDto<String>> login(@RequestBody Map<String, String> body) {
            // Para dev: acepta cualquier user/pass y genera token
            String email = body.getOrDefault("email", "user@local");
            String token = jwtUtils.generateToken(
                    email,
                    Map.of("roles", "ROLE_USER", "email", email)
            );
            return ResponseEntity.ok(new ResponseDto<>(false, token));
        }
    }}
