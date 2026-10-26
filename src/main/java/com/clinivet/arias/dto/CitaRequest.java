package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO para la solicitud de registro de nueva cita médica (RF-13).
 */
@Schema(description = "Payload para agendar una nueva cita médica veterinaria")
public class CitaRequest {

    @NotNull(message = "El identificador de la mascota es obligatorio")
    @Schema(description = "UUID de la mascota", example = "f0000000-0000-0000-0000-000000000001")
    private UUID mascotaId;

    @NotNull(message = "El identificador del propietario es obligatorio")
    @Schema(description = "UUID del propietario de la mascota", example = "e0000000-0000-0000-0000-000000000001")
    private UUID propietarioId;

    @NotNull(message = "El identificador del veterinario es obligatorio")
    @Schema(description = "UUID del médico veterinario asignado", example = "d0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la cita en formato YYYY-MM-DD", example = "2026-11-15")
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria")
    @JsonFormat(pattern = "HH:mm[:ss]")
    @Schema(description = "Hora de la cita en formato HH:mm", example = "10:30")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Size(min = 3, max = 500, message = "El motivo debe contener entre 3 y 500 caracteres")
    @Schema(description = "Motivo de la atención médica", example = "Control de vacunas y desparasitación periódica")
    private String motivo;

    public CitaRequest() {
    }

    public CitaRequest(UUID mascotaId, UUID propietarioId, UUID veterinarioId, LocalDate fecha, LocalTime hora, String motivo) {
        this.mascotaId = mascotaId;
        this.propietarioId = propietarioId;
        this.veterinarioId = veterinarioId;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
    }

    public UUID getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(UUID mascotaId) {
        this.mascotaId = mascotaId;
    }

    public UUID getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(UUID propietarioId) {
        this.propietarioId = propietarioId;
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

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
