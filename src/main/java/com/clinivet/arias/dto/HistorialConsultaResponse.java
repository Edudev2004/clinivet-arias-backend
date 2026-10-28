package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta del historial clínico completo de una mascota (RF-25 / RF-26).
 * Incluye consulta, diagnóstico, tratamientos prescritos y vacunas administradas.
 */
@Schema(description = "Resumen clínico detallado de una atención médica en el expediente del paciente")
public class HistorialConsultaResponse {

    @Schema(description = "Identificador único de la consulta médica", example = "e0000000-0000-0000-0000-000000000001")
    private UUID consultaId;

    @Schema(description = "Identificador único de la cita asociada", example = "c0000000-0000-0000-0000-000000000001")
    private UUID citaId;

    @Schema(description = "Fecha y hora en que se realizó la atención", example = "2026-10-28T10:30:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Identificador único del veterinario tratante", example = "a0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @Schema(description = "Nombre completo del médico veterinario tratante", example = "Dra. Patricia Gómez")
    private String veterinarioNombre;

    @Schema(description = "Motivo de la consulta reportado", example = "Revisión anual y cojera leve")
    private String motivo;

    @Schema(description = "Diagnóstico clínico emitido (RF-21)", example = "Gastroenteritis aguda leve")
    private String diagnostico;

    @Schema(description = "Peso corporal registrado en kg (RF-12)", example = "28.50")
    private BigDecimal pesoKg;

    @Schema(description = "Temperatura corporal registrada en °C", example = "38.5")
    private BigDecimal temperaturaC;

    @Schema(description = "Observaciones o recomendaciones", example = "Reposo relativo por 48 horas")
    private String observaciones;

    @Schema(description = "Lista de tratamientos y recetas médicas prescritas en la consulta (RF-22 / RF-23)")
    private List<TratamientoResponse> tratamientos;

    @Schema(description = "Lista de vacunas administradas durante la consulta (RF-24)")
    private List<VacunaAplicadaResponse> vacunas;

    @Schema(description = "Fecha y hora de registro en auditoría", example = "2026-10-28T10:45:00")
    private LocalDateTime createdAt;

    public HistorialConsultaResponse() {
    }

    public HistorialConsultaResponse(UUID consultaId, UUID citaId, LocalDateTime fechaHora,
                                     UUID veterinarioId, String veterinarioNombre, String motivo,
                                     String diagnostico, BigDecimal pesoKg, BigDecimal temperaturaC,
                                     String observaciones, List<TratamientoResponse> tratamientos,
                                     List<VacunaAplicadaResponse> vacunas, LocalDateTime createdAt) {
        this.consultaId = consultaId;
        this.citaId = citaId;
        this.fechaHora = fechaHora;
        this.veterinarioId = veterinarioId;
        this.veterinarioNombre = veterinarioNombre;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.pesoKg = pesoKg;
        this.temperaturaC = temperaturaC;
        this.observaciones = observaciones;
        this.tratamientos = tratamientos;
        this.vacunas = vacunas;
        this.createdAt = createdAt;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(UUID consultaId) {
        this.consultaId = consultaId;
    }

    public UUID getCitaId() {
        return citaId;
    }

    public void setCitaId(UUID citaId) {
        this.citaId = citaId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public UUID getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(UUID veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getVeterinarioNombre() {
        return veterinarioNombre;
    }

    public void setVeterinarioNombre(String veterinarioNombre) {
        this.veterinarioNombre = veterinarioNombre;
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

    public List<TratamientoResponse> getTratamientos() {
        return tratamientos;
    }

    public void setTratamientos(List<TratamientoResponse> tratamientos) {
        this.tratamientos = tratamientos;
    }

    public List<VacunaAplicadaResponse> getVacunas() {
        return vacunas;
    }

    public void setVacunas(List<VacunaAplicadaResponse> vacunas) {
        this.vacunas = vacunas;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
