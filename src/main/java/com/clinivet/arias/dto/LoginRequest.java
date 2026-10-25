package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

/**
 * DTO que encapsula las credenciales enviadas para iniciar sesión.
 * Permite autenticarse mediante 'email' o 'username' indistintamente.
 */
@Schema(description = "Solicitud de inicio de sesión con credenciales")
public class LoginRequest {

    @Schema(description = "Correo electrónico o nombre de usuario", example = "admin@clinivet.pe")
    private String email;

    @Schema(description = "Nombre de usuario alternativo", example = "admin")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña en texto plano para validación con BCrypt", example = "Admin123!")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Retorna el identificador principal (email o username) sanitizado con Apache Commons.
     */
    public String getIdentifier() {
        if (StringUtils.isNotBlank(this.email)) {
            return StringUtils.trim(this.email);
        }
        return StringUtils.trim(this.username);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
