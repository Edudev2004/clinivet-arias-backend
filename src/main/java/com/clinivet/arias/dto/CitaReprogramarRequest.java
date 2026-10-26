package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Objeto de transferencia de datos (DTO) para la reprogramación de una cita médica (RF-16).
 * Recibe la nueva fecha y hora deseadas para la agenda del veterinario.
 */
@Schema(description = "Solicitud para reprogramar una cita médica existente (RF-16)")
public class CitaReprogramarRequest {

    @NotNull(message = "La nueva fecha es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Nueva fecha solicitada para la cita médica (YYYY-MM-DD)", example = "2026-11-05")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Nueva hora solicitada para la cita médica (HH:mm)", example = "10:30")
    private LocalTime nuevaHora;

    public CitaReprogramarRequest() {
    }

    public CitaReprogramarRequest(LocalDate nuevaFecha, LocalTime nuevaHora) {
        this.nuevaFecha = nuevaFecha;
        this.nuevaHora = nuevaHora;
    }

    public LocalDate getNuevaFecha() {
        return nuevaFecha;
    }

    public void setNuevaFecha(LocalDate nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public LocalTime getNuevaHora() {
        return nuevaHora;
    }

    public void setNuevaHora(LocalTime nuevaHora) {
        this.nuevaHora = nuevaHora;
    }
}
