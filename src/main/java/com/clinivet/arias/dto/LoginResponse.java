package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO que encapsula la respuesta tras una autenticación exitosa.
 * Incluye el token JWT y datos de identidad y perfil del usuario.
 */
@Schema(description = "Respuesta de autenticación exitosa con JWT y perfil de usuario")
public class LoginResponse {

    @Schema(description = "Token JWT firmado con HMAC-SHA256")
    private String token;

    @Schema(description = "Tipo de autorización", example = "Bearer")
    private String tipo = "Bearer";

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long idUsuario;

    @Schema(description = "Correo electrónico del usuario", example = "admin@clinivet.pe")
    private String email;

    @Schema(description = "Nombres del usuario", example = "Rommel")
    private String nombres;

    @Schema(description = "Apellidos del usuario", example = "Navarro")
    private String apellidos;

    @Schema(description = "Rol asignado para control de acceso", example = "ADMIN")
    private String rol;

    @Schema(description = "Milisegundos de validez del token", example = "86400000")
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long idUsuario, String email, String nombres,
                         String apellidos, String rol, Long expiresIn) {
        this.token = token;
        this.tipo = "Bearer";
        this.idUsuario = idUsuario;
        this.email = email;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rol = rol;
        this.expiresIn = expiresIn;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
