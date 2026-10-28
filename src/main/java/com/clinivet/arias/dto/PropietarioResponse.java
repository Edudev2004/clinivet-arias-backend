package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que representa a un propietario registrado en el sistema.
 */
@Schema(description = "Datos de respuesta de un propietario de mascota")
public class PropietarioResponse {

    @Schema(description = "Identificador único UUID del propietario", example = "e0000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "Nombre(s) del propietario", example = "Carlos")
    private String nombre;

    @Schema(description = "Apellido(s) del propietario", example = "Mendoza Ramos")
    private String apellido;

    @Schema(description = "Nombre completo concatenado", example = "Carlos Mendoza Ramos")
    private String nombreCompleto;

    @Schema(description = "Documento Nacional de Identidad", example = "45871236")
    private String dni;

    @Schema(description = "Teléfono de contacto", example = "987654321")
    private String telefono;

    @Schema(description = "Correo electrónico", example = "carlos.mendoza@gmail.com")
    private String email;

    @Schema(description = "Dirección domiciliaria", example = "Av. Universitaria 1240, Carabayllo")
    private String direccion;

    @Schema(description = "Estado de actividad del propietario", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha y hora de registro", example = "2026-10-27T10:00:00")
    private LocalDateTime createdAt;

    public PropietarioResponse() {
    }

    public PropietarioResponse(UUID id, String nombre, String apellido, String dni, String telefono, String email, String direccion, Boolean activo, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreCompleto = (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : "");
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = activo;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
