package com.clinivet.arias.service;

import com.clinivet.arias.dto.LoginRequest;
import com.clinivet.arias.dto.LoginResponse;
import com.clinivet.arias.model.Rol;
import com.clinivet.arias.model.Usuario;
import com.clinivet.arias.repository.UsuarioRepository;
import com.clinivet.arias.security.ITokenBlacklistService;
import com.clinivet.arias.security.JwtUtil;
import com.clinivet.arias.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link AuthServiceImpl} utilizando JUnit 5 y Mockito.
 * Valida los criterios de aceptación de RF-01, RF-02 y RF-04 con identificadores UUID (APF2).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ITokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioPrueba;
    private final UUID userId = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private final UUID rolId = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        Rol rolAdmin = new Rol(rolId, "ADMINISTRADOR", "Acceso total");
        usuarioPrueba = new Usuario(
                userId,
                "Rommel",
                "Navarro",
                "admin@clinivet.pe",
                "$2a$10$encryptedHashPassword",
                rolAdmin,
                true
        );
    }

    @Test
    @DisplayName("RF-01: Login correcto retorna token JWT y datos de rol")
    void login_CredencialesCorrectas_RetornaTokenYRol() {
        // Arrange
        LoginRequest request = new LoginRequest("admin@clinivet.pe", "Admin123!");
        when(usuarioRepository.findByEmail("admin@clinivet.pe")).thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches("Admin123!", usuarioPrueba.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(usuarioPrueba)).thenReturn("mocked.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertEquals("admin@clinivet.pe", response.getEmail());
        assertEquals("Rommel", response.getNombres());
        assertEquals(userId, response.getIdUsuario());

        verify(usuarioRepository, times(1)).findByEmail("admin@clinivet.pe");
        verify(passwordEncoder, times(1)).matches("Admin123!", usuarioPrueba.getPasswordHash());
        verify(tokenBlacklistService, times(1)).recordActivity("mocked.jwt.token");
    }

    @Test
    @DisplayName("RF-02: Login con contraseña incorrecta lanza BadCredentialsException")
    void login_PasswordIncorrecta_LanzaBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("admin@clinivet.pe", "PasswordEquivocada");
        when(usuarioRepository.findByEmail("admin@clinivet.pe")).thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches("PasswordEquivocada", usuarioPrueba.getPasswordHash())).thenReturn(false);

        // Act & Assert
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Usuario o contraseña incorrectos", ex.getMessage());

        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("RF-02: Login con usuario inexistente lanza BadCredentialsException con mensaje genérico")
    void login_UsuarioInexistente_LanzaBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("noexiste@clinivet.pe", "Admin123!");
        when(usuarioRepository.findByEmail("noexiste@clinivet.pe")).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Usuario o contraseña incorrectos", ex.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("RF-02: Login con usuario desactivado lanza DisabledException (HTTP 403)")
    void login_UsuarioDesactivado_LanzaDisabledException() {
        // Arrange
        usuarioPrueba.setActivo(false);
        LoginRequest request = new LoginRequest("admin@clinivet.pe", "Admin123!");
        when(usuarioRepository.findByEmail("admin@clinivet.pe")).thenReturn(Optional.of(usuarioPrueba));

        // Act & Assert
        DisabledException ex = assertThrows(DisabledException.class, () -> authService.login(request));
        assertEquals("Tu cuenta está desactivada", ex.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("RF-04: Logout con token válido extrae username e invalida el token en blacklist")
    void logout_ConTokenValido_InvalidaTokenEnBlacklist() {
        // Arrange
        String authHeader = "Bearer sample.jwt.token";
        when(jwtUtil.extractUsername("sample.jwt.token")).thenReturn("admin@clinivet.pe");

        // Act
        authService.logout(authHeader);

        // Assert
        verify(jwtUtil, times(1)).extractUsername("sample.jwt.token");
        verify(tokenBlacklistService, times(1)).invalidateToken("sample.jwt.token");
    }

    @Test
    @DisplayName("RF-04: Logout con header nulo o en blanco no invoca invalidación ni lanza excepción")
    void logout_ConHeaderNuloOEnBlanco_NoInvalidaNiFalla() {
        // Act
        authService.logout(null);
        authService.logout("");
        authService.logout("   ");

        // Assert
        verify(tokenBlacklistService, never()).invalidateToken(anyString());
        verify(jwtUtil, never()).extractUsername(anyString());
    }
}
