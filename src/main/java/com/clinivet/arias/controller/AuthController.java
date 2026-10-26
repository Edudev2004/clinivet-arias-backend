package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.LoginRequest;
import com.clinivet.arias.dto.LoginResponse;
import com.clinivet.arias.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de autenticación y sesión.
 * Cumple con SRP (solo orquesta HTTP) y DIP (depende de IAuthService).
 */
@RestController
@RequestMapping({"/api/auth", "/auth"})
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión, perfil y gestión de credenciales JWT")
public class AuthController {

    private final IAuthService authService;

    // Inyección de dependencias por constructor (DIP)
    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales con BCrypt y retorna un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o campos incompletos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Cuenta desactivada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar perfil y rol actual", description = "Retorna los datos y roles del usuario autenticado (RF-03)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario autenticado con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token expirado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> getPerfilActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "email", authentication.getName(),
                "roles", roles,
                "autenticado", true
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión actual del cliente agregando el token a lista negra (RF-04)")
    @ApiResponse(responseCode = "200", description = "Cierre de sesión exitoso")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }
}
