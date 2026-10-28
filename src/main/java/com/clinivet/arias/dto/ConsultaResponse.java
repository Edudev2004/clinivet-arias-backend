package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta con la consulta médica registrada y su vínculo a la mascota vía cita (RF-12).
 */
@Schema(description = "Consulta médica registrada")
public class ConsultaResponse {

    @Schema(description = "ID de la consulta", example = "e0000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "ID de la cita atendida")
    private UUID citaId;

    @Schema(description = "ID de la mascota atendida (obtenida a través de la cita)")
    private UUID mascotaId;

    @Schema(description = "Nombre de la mascota atendida")
    private String mascotaNombre;

    @Schema(description = "ID del veterinario que atendió")
    private UUID veterinarioId;

    @Schema(description = "Fecha de la consulta", example = "2026-11-15")
    private LocalDate fecha;

    @Schema(description = "Motivo de la consulta")
    private String motivo;

    @Schema(description = "Diagnóstico clínico")
    private String diagnostico;

    @Schema(description = "Peso registrado en kg", example = "28.50")
    private BigDecimal pesoKg;

    @Schema(description = "Temperatura registrada en °C", example = "38.5")
    private BigDecimal temperaturaC;

    @Schema(description = "Observaciones")
    private String observaciones;

    @Schema(description = "Fecha y hora de registro")
    private LocalDateTime createdAt;

    public ConsultaResponse() {
    }

    public ConsultaResponse(UUID id, UUID citaId, UUID mascotaId, String mascotaNombre, UUID veterinarioId,
                            LocalDate fecha, String motivo, String diagnostico, BigDecimal pesoKg,
                            BigDecimal temperaturaC, String observaciones, LocalDateTime createdAt) {
        this.id = id;
        this.citaId = citaId;
        this.mascotaId = mascotaId;
        this.mascotaNombre = mascotaNombre;
        this.veterinarioId = veterinarioId;
        this.fecha = fecha;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.pesoKg = pesoKg;
        this.temperaturaC = temperaturaC;
        this.observaciones = observaciones;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCitaId() {
        return citaId;
    }

    public void setCitaId(UUID citaId) {
        this.citaId = citaId;
    }

    public UUID getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(UUID mascotaId) {
        this.mascotaId = mascotaId;
    }

    public String getMascotaNombre() {
        return mascotaNombre;
    }

    public void setMascotaNombre(String mascotaNombre) {
        this.mascotaNombre = mascotaNombre;
    }

    public UUID getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(UUID veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(BigDecimal temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
