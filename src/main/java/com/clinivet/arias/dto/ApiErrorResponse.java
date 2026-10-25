package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO uniforme para la respuesta de errores de la API REST.
 * Oculta trazas internas y garantiza mensajes claros y seguros.
 */
@Schema(description = "Estructura estandarizada de errores de la API")
public class ApiErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora del incidente", example = "2026-10-25T14:45:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de estado HTTP", example = "401")
    private int status;

    @Schema(description = "Descripción del estado HTTP", example = "Unauthorized")
    private String error;

    @Schema(description = "Mensaje descriptivo del error para el usuario", example = "Usuario o contraseña incorrectos")
    private String mensaje;

    @Schema(description = "Ruta del recurso invocado", example = "/api/auth/login")
    private String path;

    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiErrorResponse(int status, String error, String mensaje, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }

    // Getters y Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
