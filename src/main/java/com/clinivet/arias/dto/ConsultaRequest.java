package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para registrar una consulta médica con los datos clínicos de la atención (RF-12).
 */
@Schema(description = "Payload para registrar una consulta médica vinculada a una cita")
public class ConsultaRequest {

    @NotNull(message = "El identificador de la cita es obligatorio")
    @Schema(description = "UUID de la cita atendida", example = "c0000000-0000-0000-0000-000000000001")
    private UUID citaId;

    @NotNull(message = "El identificador del veterinario es obligatorio")
    @Schema(description = "UUID del veterinario que realiza la atención", example = "d0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @NotNull(message = "La fecha de la consulta es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la consulta en formato YYYY-MM-DD", example = "2026-11-15")
    private LocalDate fecha;

    @NotBlank(message = "El motivo de la consulta es obligatorio")
    @Size(min = 3, max = 500, message = "El motivo debe contener entre 3 y 500 caracteres")
    @Schema(description = "Motivo de la consulta", example = "Control post-operatorio")
    private String motivo;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(min = 3, max = 2000, message = "El diagnóstico debe contener entre 3 y 2000 caracteres")
    @Schema(description = "Diagnóstico clínico (columna NOT NULL en la tabla consultas)", example = "Herida en cicatrización adecuada")
    private String diagnostico;

    @NotNull(message = "El peso de la mascota es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0 kg")
    @DecimalMax(value = "999.99", message = "El peso no puede superar 999.99 kg")
    @Digits(integer = 3, fraction = 2, message = "El peso admite hasta 3 enteros y 2 decimales")
    @Schema(description = "Peso de la mascota en kg", example = "28.50")
    private BigDecimal peso;

    @NotNull(message = "La temperatura de la mascota es obligatoria")
    @DecimalMin(value = "0.1", message = "La temperatura debe ser mayor a 0 °C")
    @DecimalMax(value = "99.9", message = "La temperatura no puede superar 99.9 °C")
    @Digits(integer = 2, fraction = 1, message = "La temperatura admite hasta 2 enteros y 1 decimal")
    @Schema(description = "Temperatura corporal en °C", example = "38.5")
    private BigDecimal temperatura;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres")
    @Schema(description = "Observaciones adicionales (opcional)", example = "Sin complicaciones")
    private String observaciones;

    public ConsultaRequest() {
    }

    public ConsultaRequest(UUID citaId, UUID veterinarioId, LocalDate fecha, String motivo,
                           String diagnostico, BigDecimal peso, BigDecimal temperatura, String observaciones) {
        this.citaId = citaId;
        this.veterinarioId = veterinarioId;
        this.fecha = fecha;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.peso = peso;
        this.temperatura = temperatura;
        this.observaciones = observaciones;
    }

    public UUID getCitaId() {
        return citaId;
    }

    public void setCitaId(UUID citaId) {
        this.citaId = citaId;
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

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(BigDecimal temperatura) {
        this.temperatura = temperatura;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
