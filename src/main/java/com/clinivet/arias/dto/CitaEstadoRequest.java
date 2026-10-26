package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Objeto de transferencia de datos (DTO) para la actualización del estado de una cita médica (RF-19).
 * Permite transiciones controladas de agenda (PROGRAMADA → ATENDIDA, PROGRAMADA → CANCELADA).
 */
@Schema(description = "Solicitud para actualizar el estado de una cita médica (RF-19)")
public class CitaEstadoRequest {

    @NotBlank(message = "El estado de la cita es obligatorio")
    @Schema(description = "Nuevo estado solicitado para la cita (ATENDIDA o CANCELADA)", example = "ATENDIDA")
    private String estado;

    public CitaEstadoRequest() {
    }

    public CitaEstadoRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
