package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.LoginRequest;
import com.clinivet.arias.dto.LoginResponse;
import com.clinivet.arias.service.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link AuthController} con MockMvc.
 * Valida los códigos de respuesta HTTP y la serialización JSON.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login retorna 200 OK y token cuando las credenciales son válidas")
    void login_CredencialesValidas_Retorna200Ok() throws Exception {
        LoginRequest request = new LoginRequest("admin@clinivet.pe", "Admin123!");
        LoginResponse response = new LoginResponse(
                "token.jwt.valido",
                1L,
                "admin@clinivet.pe",
                "Rommel",
                "Navarro",
                "ADMIN",
                86400000L
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt.valido"))
                .andExpect(jsonPath("$.email").value("admin@clinivet.pe"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login retorna 401 Unauthorized cuando las credenciales son incorrectas")
    void login_CredencialesInvalidas_Retorna401() throws Exception {
        LoginRequest request = new LoginRequest("admin@clinivet.pe", "ClaveErronea");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Usuario o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /api/auth/login retorna 403 Forbidden cuando el usuario está desactivado")
    void login_UsuarioDesactivado_Retorna403() throws Exception {
        LoginRequest request = new LoginRequest("desactivado@clinivet.pe", "Admin123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new DisabledException("Tu cuenta está desactivada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("Tu cuenta está desactivada"));
    }

    @Test
    @DisplayName("POST /api/auth/logout retorna 200 OK con mensaje de éxito")
    void logout_Retorna200Ok() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente"));
    }
}
