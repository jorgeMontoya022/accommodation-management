package co.edu.uniquindio.alojamientos.alojamientos_app.controller;


import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.RegisterRequest;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Autenticación", description = "Endpoints para registro y autenticación de usuarios")
public class AlojamientoController {

    @Operation(
            summary = "Registro de usuario",
            description = "Crear una nueva cuenta de usuario en el sistema. " +
                    "El email debe ser único y la contraseña debe tener al menos 6 caracteres.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del nuevo usuario a registrar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Usuario huésped",
                                            summary = "Registro como huésped",
                                            description = "Ejemplo de registro de un usuario huésped",
                                            value = """
                        {
                            "nombre": "Juan Pérez",
                            "email": "juan@ejemplo.com",
                            "telefono": "+57 300 1234567",
                            "fechaNacimiento": "1990-05-15",
                            "rol": "huésped",
                            "password": "miPassword123"
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Usuario anfitrión",
                                            summary = "Registro como anfitrión",
                                            description = "Ejemplo de registro de un usuario anfitrión",
                                            value = """
                        {
                            "nombre": "María García",
                            "email": "maria@ejemplo.com",
                            "telefono": "+57 301 7654321",
                            "fechaNacimiento": "1985-03-20",
                            "rol": "anfitrión",
                            "password": "password456"
                        }
                        """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "mensaje": "Usuario creado exitosamente",
                        "id": "user_12345"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "error": "VALIDATION_ERROR",
                        "mensaje": "El email es obligatorio"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email ya está registrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "error": "CONFLICT",
                        "mensaje": "Ya existe un usuario con este email"
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Por ahora solo devolvemos una respuesta simulada
        // Aquí iría tu lógica de negocio real

        System.out.println("Registrando usuario: " + request.getNombre());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Rol: " + request.getRol());

        // Simular creación exitosa
        SuccessResponse response = new SuccessResponse(
                "Usuario creado exitosamente",
                "user_" + System.currentTimeMillis()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autenticar un usuario existente y obtener un token JWT para acceder a los endpoints protegidos."
    )
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de acceso",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                    {
                        "email": "juan@ejemplo.com",
                        "password": "miPassword123"
                    }
                    """
                            )
                    )
            )
            @RequestBody String loginRequest
    ) {
        // Implementación simulada
        return ResponseEntity.ok("Login endpoint funcionando");
    }

    @Operation(
            summary = "Recuperar contraseña",
            description = "Enviar un email con instrucciones para resetear la contraseña"
    )
    @PostMapping("/auth/recover-password")
    public ResponseEntity<SuccessResponse> recoverPassword(
            @RequestBody String recoverRequest
    ) {
        return ResponseEntity.ok(new SuccessResponse("Correo de recuperación enviado"));
    }


}
