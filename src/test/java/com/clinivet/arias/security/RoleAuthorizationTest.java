package com.clinivet.arias.security;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.config.SecurityConfig;
import com.clinivet.arias.controller.AccessControlTestController;
import com.clinivet.arias.controller.AuthController;
import com.clinivet.arias.service.IAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para validar el control de acceso basado en roles (RBAC)
 * según los criterios de aceptación de RF-03 e Issue #12.
 */
@WebMvcTest(controllers = {AccessControlTestController.class, AuthController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private ITokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("RF-03: Usuario sin autenticación es rechazado con HTTP 401 Unauthorized")
    void accesoSinAutenticacion_Retorna401() throws Exception {
        mockMvc.perform(get("/api/test/roles/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Se requiere autenticación para acceder a este recurso. Inicie sesión."));
    }

    @Test
    @WithMockUser(username = "admin@clinivet.pe", roles = {"ADMINISTRADOR"})
    @DisplayName("RF-03: Usuario con rol ADMINISTRADOR accede exitosamente a zona admin (HTTP 200)")
    void accesoAdmin_ConRolAdmin_Retorna200Ok() throws Exception {
        mockMvc.perform(get("/api/test/roles/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Acceso concedido a zona de ADMINISTRADOR"));
    }

    @Test
    @WithMockUser(username = "veterinario@clinivet.pe", roles = {"VETERINARIO"})
    @DisplayName("RF-03: Usuario con rol VETERINARIO accede exitosamente a zona clínica (HTTP 200)")
    void accesoVeterinario_ConRolVeterinario_Retorna200Ok() throws Exception {
        mockMvc.perform(get("/api/test/roles/veterinario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Acceso concedido a zona de VETERINARIO"));
    }

    @Test
    @WithMockUser(username = "veterinario@clinivet.pe", roles = {"VETERINARIO"})
    @DisplayName("RF-03: Usuario con rol VETERINARIO es bloqueado al intentar acceder a zona de inventario (HTTP 403)")
    void accesoInventario_ConRolVeterinario_Retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/test/roles/inventario"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos suficientes para acceder a este recurso"));
    }

    @Test
    @WithMockUser(username = "inventario@clinivet.pe", roles = {"ENCARGADO_INVENTARIO"})
    @DisplayName("RF-03: Usuario con rol ENCARGADO_INVENTARIO es bloqueado al intentar acceder a zona de veterinario (HTTP 403)")
    void accesoVeterinario_ConRolInventario_Retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/test/roles/veterinario"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos suficientes para acceder a este recurso"));
    }

    @Test
    @WithMockUser(username = "recepcion@clinivet.pe", roles = {"RECEPCIONISTA"})
    @DisplayName("RF-03: Usuario con rol RECEPCIONISTA es bloqueado al intentar acceder a zona admin (HTTP 403)")
    void accesoAdmin_ConRolRecepcionista_Retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/test/roles/admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos suficientes para acceder a este recurso"));
    }

    @Test
    @WithMockUser(username = "recepcion@clinivet.pe", roles = {"RECEPCIONISTA"})
    @DisplayName("RF-03: Usuario con rol RECEPCIONISTA accede exitosamente a su zona (HTTP 200)")
    void accesoRecepcion_ConRolRecepcionista_Retorna200Ok() throws Exception {
        mockMvc.perform(get("/api/test/roles/recepcion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Acceso concedido a zona de RECEPCIONISTA"));
    }

    @Test
    @WithMockUser(username = "admin@clinivet.pe", roles = {"ADMINISTRADOR"})
    @DisplayName("RF-03: Endpoint GET /api/auth/me retorna identidad y roles del usuario activo")
    void getPerfilActual_UsuarioAutenticado_RetornaRoles() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@clinivet.pe"))
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMINISTRADOR"));
    }

    @Test
    @DisplayName("RF-04: Petición con token en lista negra (tras logout) es rechazada con HTTP 401 y mensaje estándar")
    void accesoConTokenEnBlacklist_Retorna401() throws Exception {
        String token = "mock.invalidated.jwt";
        when(tokenBlacklistService.isTokenInvalidated(token)).thenReturn(true);

        mockMvc.perform(get("/api/test/roles/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("La sesión ha sido cerrada. Inicie sesión nuevamente."));
    }

    @Test
    @DisplayName("RF-04 / RNF-04: Petición con sesión inactiva es rechazada con HTTP 401 y mensaje de expiración")
    void accesoConSesionInactiva_Retorna401() throws Exception {
        String token = "mock.inactive.jwt";
        when(tokenBlacklistService.isTokenInvalidated(token)).thenReturn(false);
        when(tokenBlacklistService.isSessionInactive(token)).thenReturn(true);

        mockMvc.perform(get("/api/test/roles/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("La sesión ha expirado por inactividad. Inicie sesión nuevamente."));
    }
}
