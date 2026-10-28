package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para la respuesta del registro de vacuna aplicada a una mascota (RF-24).
 */
@Schema(description = "Datos detallados de la vacuna administrada a un paciente")
public class VacunaAplicadaResponse {

    @Schema(description = "Identificador único del registro de vacuna aplicada", example = "7a000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "ID de la mascota receptora", example = "b0000000-0000-0000-0000-000000000001")
    private UUID mascotaId;

    @Schema(description = "Nombre de la mascota receptora", example = "Firulais")
    private String mascotaNombre;

    @Schema(description = "ID de la vacuna en el catálogo maestro", example = "9a000000-0000-0000-0000-000000000001")
    private UUID vacunaId;

    @Schema(description = "Nombre de la vacuna", example = "Séxtuple Canina")
    private String nombreVacuna;

    @Schema(description = "Fecha en que se administró la dosis", example = "2026-10-28")
    private LocalDate fechaAplicacion;

    @Schema(description = "Fecha de próxima dosis o refuerzo", example = "2027-10-28")
    private LocalDate proximaFecha;

    @Schema(description = "ID del veterinario responsable", example = "a0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @Schema(description = "Nombre completo del médico veterinario", example = "Dra. Patricia Gómez")
    private String veterinarioNombre;

    @Schema(description = "Lote del medicamento aplicado", example = "LOT-2026-A12")
    private String lote;

    @Schema(description = "ID de la consulta asociada si aplica", example = "e0000000-0000-0000-0000-000000000001")
    private UUID consultaId;

    @Schema(description = "Fecha y hora de registro en auditoría", example = "2026-10-28T15:00:00")
    private LocalDateTime createdAt;

    public VacunaAplicadaResponse() {
    }

    public VacunaAplicadaResponse(UUID id, UUID mascotaId, String mascotaNombre, UUID vacunaId,
                                  String nombreVacuna, LocalDate fechaAplicacion, LocalDate proximaFecha,
                                  UUID veterinarioId, String veterinarioNombre, String lote,
                                  UUID consultaId, LocalDateTime createdAt) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.mascotaNombre = mascotaNombre;
        this.vacunaId = vacunaId;
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaFecha = proximaFecha;
        this.veterinarioId = veterinarioId;
        this.veterinarioNombre = veterinarioNombre;
        this.lote = lote;
        this.consultaId = consultaId;
        this.createdAt = createdAt;
    }

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

    public String getMascotaNombre() {
        return mascotaNombre;
    }

    public void setMascotaNombre(String mascotaNombre) {
        this.mascotaNombre = mascotaNombre;
    }

    public UUID getVacunaId() {
        return vacunaId;
    }

    public void setVacunaId(UUID vacunaId) {
        this.vacunaId = vacunaId;
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

    public String getVeterinarioNombre() {
        return veterinarioNombre;
    }

    public void setVeterinarioNombre(String veterinarioNombre) {
        this.veterinarioNombre = veterinarioNombre;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
