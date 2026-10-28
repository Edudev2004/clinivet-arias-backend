package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para la solicitud de registro de una vacuna aplicada a una mascota (RF-24).
 */
@Schema(description = "Solicitud para registrar la aplicación de una vacuna a un paciente")
public class VacunaAplicadaRequest {

    @NotBlank(message = "El nombre de la vacuna es obligatorio")
    @Size(max = 100, message = "El nombre de la vacuna no puede exceder 100 caracteres")
    @Schema(description = "Nombre comercial o genérico de la vacuna", example = "Séxtuple Canina")
    private String nombreVacuna;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @Schema(description = "Fecha en que se administró la vacuna", example = "2026-10-28")
    private LocalDate fechaAplicacion;

    @Schema(description = "Fecha estimada para la próxima dosis de refuerzo (opcional)", example = "2027-10-28")
    private LocalDate proximaFecha;

    @NotNull(message = "El ID del veterinario responsable es obligatorio")
    @Schema(description = "Identificador único del veterinario que administró la dosis", example = "a0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @Schema(description = "Código de lote del biológico aplicado (opcional)", example = "LOT-2026-A12")
    private String lote;

    @Schema(description = "Identificador de la consulta asociada si fue administrada durante una atención (opcional)", example = "e0000000-0000-0000-0000-000000000001")
    private UUID consultaId;

    public VacunaAplicadaRequest() {
    }

    public VacunaAplicadaRequest(String nombreVacuna, LocalDate fechaAplicacion, LocalDate proximaFecha, UUID veterinarioId, String lote) {
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaFecha = proximaFecha;
        this.veterinarioId = veterinarioId;
        this.lote = lote;
    }

    public VacunaAplicadaRequest(String nombreVacuna, LocalDate fechaAplicacion, LocalDate proximaFecha, UUID veterinarioId, String lote, UUID consultaId) {
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaFecha = proximaFecha;
        this.veterinarioId = veterinarioId;
        this.lote = lote;
        this.consultaId = consultaId;
    }

    public String getNombreVacuna() {
        return nombreVacuna;
    }

    public void setNombreVacuna(String nombreVacuna) {
        this.nombreVacuna = nombreVacuna;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDate getProximaFecha() {
        return proximaFecha;
    }

    public void setProximaFecha(LocalDate proximaFecha) {
        this.proximaFecha = proximaFecha;
    }

    public UUID getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(UUID veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(UUID consultaId) {
        this.consultaId = consultaId;
    }
}
