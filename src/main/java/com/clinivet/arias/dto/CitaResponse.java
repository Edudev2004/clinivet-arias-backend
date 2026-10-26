package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO que retorna todos los datos relacionados de la cita creada o consultada (RF-13).
 */
@Schema(description = "Detalle completo de una cita médica registrada")
public class CitaResponse {

    @Schema(description = "ID único de la cita (UUID)", example = "70000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "ID de la mascota", example = "f0000000-0000-0000-0000-000000000001")
    private UUID mascotaId;

    @Schema(description = "Nombre de la mascota", example = "Bobby")
    private String nombreMascota;

    @Schema(description = "Especie de la mascota", example = "Canino")
    private String especieMascota;

    @Schema(description = "ID del propietario", example = "e0000000-0000-0000-0000-000000000001")
    private UUID propietarioId;

    @Schema(description = "Nombre completo del propietario", example = "Carlos Mendoza Ramos")
    private String nombrePropietario;

    @Schema(description = "DNI del propietario", example = "45892314")
    private String dniPropietario;

    @Schema(description = "Teléfono de contacto del propietario", example = "987123456")
    private String telefonoPropietario;

    @Schema(description = "ID del veterinario asignado", example = "d0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @Schema(description = "Nombre completo del médico veterinario", example = "Beckhan Elias")
    private String nombreVeterinario;

    @Schema(description = "Especialidad médica del veterinario", example = "Medicina General y Cirugía Menor")
    private String especialidadVeterinario;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la cita", example = "2026-11-15")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Hora de la cita", example = "10:30")
    private LocalTime hora;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora completa combinada", example = "2026-11-15T10:30:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Estado actual de la cita", example = "PROGRAMADA")
    private String estado;

    @Schema(description = "Motivo de la consulta", example = "Control de vacunas y desparasitación periódica")
    private String motivo;

    @Schema(description = "ID del usuario que registró la cita", example = "b0000000-0000-0000-0000-000000000001")
    private UUID creadoPorId;

    @Schema(description = "Email del usuario que registró la cita", example = "admin@clinivet.pe")
    private String creadoPorEmail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de registro en el sistema")
    private LocalDateTime createdAt;

    public CitaResponse() {
    }

    public CitaResponse(UUID id, UUID mascotaId, String nombreMascota, String especieMascota,
                        UUID propietarioId, String nombrePropietario, String dniPropietario, String telefonoPropietario,
                        UUID veterinarioId, String nombreVeterinario, String especialidadVeterinario,
                        LocalDate fecha, LocalTime hora, LocalDateTime fechaHora,
                        String estado, String motivo, UUID creadoPorId, String creadoPorEmail, LocalDateTime createdAt) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.nombreMascota = nombreMascota;
        this.especieMascota = especieMascota;
        this.propietarioId = propietarioId;
        this.nombrePropietario = nombrePropietario;
        this.dniPropietario = dniPropietario;
        this.telefonoPropietario = telefonoPropietario;
        this.veterinarioId = veterinarioId;
        this.nombreVeterinario = nombreVeterinario;
        this.especialidadVeterinario = especialidadVeterinario;
        this.fecha = fecha;
        this.hora = hora;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivo = motivo;
        this.creadoPorId = creadoPorId;
        this.creadoPorEmail = creadoPorEmail;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(UUID mascotaId) {
        this.mascotaId = mascotaId;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public String getEspecieMascota() {
        return especieMascota;
    }

    public void setEspecieMascota(String especieMascota) {
        this.especieMascota = especieMascota;
    }

    public UUID getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(UUID propietarioId) {
        this.propietarioId = propietarioId;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public String getDniPropietario() {
        return dniPropietario;
    }

    public void setDniPropietario(String dniPropietario) {
        this.dniPropietario = dniPropietario;
    }

    public String getTelefonoPropietario() {
        return telefonoPropietario;
    }

    public void setTelefonoPropietario(String telefonoPropietario) {
        this.telefonoPropietario = telefonoPropietario;
    }

    public UUID getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(UUID veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getNombreVeterinario() {
        return nombreVeterinario;
    }

    public void setNombreVeterinario(String nombreVeterinario) {
        this.nombreVeterinario = nombreVeterinario;
    }

    public String getEspecialidadVeterinario() {
        return especialidadVeterinario;
    }

    public void setEspecialidadVeterinario(String especialidadVeterinario) {
        this.especialidadVeterinario = especialidadVeterinario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public UUID getCreadoPorId() {
        return creadoPorId;
    }

    public void setCreadoPorId(UUID creadoPorId) {
        this.creadoPorId = creadoPorId;
    }

    public String getCreadoPorEmail() {
        return creadoPorEmail;
    }

    public void setCreadoPorEmail(String creadoPorEmail) {
        this.creadoPorEmail = creadoPorEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
