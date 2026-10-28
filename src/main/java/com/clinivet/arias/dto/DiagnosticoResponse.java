package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para la respuesta del registro de diagnóstico clínico por consulta médica (RF-21).
 */
@Schema(description = "Respuesta detallada con el diagnóstico vinculado a la consulta médica")
public class DiagnosticoResponse {

    @Schema(description = "Identificador único de la consulta médica (UUID)", example = "e0000000-0000-0000-0000-000000000001")
    private UUID consultaId;

    @Schema(description = "Descripción clínica del diagnóstico", example = "Gastroenteritis aguda por ingesta de cuerpo extraño")
    private String descripcion;

    @Schema(description = "Código CIE registrado (opcional)", example = "K52.9", nullable = true)
    private String codigoCIE;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora del registro", example = "2026-10-28T14:30:00")
    private LocalDateTime fechaRegistro;

    public DiagnosticoResponse() {
    }

    public DiagnosticoResponse(UUID consultaId, String descripcion, String codigoCIE, LocalDateTime fechaRegistro) {
        this.consultaId = consultaId;
        this.descripcion = descripcion;
        this.codigoCIE = codigoCIE;
        this.fechaRegistro = fechaRegistro;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(UUID consultaId) {
        this.consultaId = consultaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoCIE() {
        return codigoCIE;
    }

    public void setCodigoCIE(String codigoCIE) {
        this.codigoCIE = codigoCIE;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
