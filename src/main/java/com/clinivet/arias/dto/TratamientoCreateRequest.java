package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para la prescripción de un tratamiento o receta médica en una consulta (RF-23 / RN-12).
 */
@Schema(description = "Solicitud para registrar un tratamiento médico con cálculo automático de dosis")
public class TratamientoCreateRequest {

    @NotNull(message = "El identificador del medicamento es obligatorio")
    @Schema(description = "ID del medicamento prescrito en el catálogo", example = "c0000000-0000-0000-0000-000000000001")
    private UUID medicamentoId;

    @NotNull(message = "La dosis por kilogramo es obligatoria")
    @DecimalMin(value = "0.01", message = "La dosis por kg debe ser mayor a 0 mg/kg")
    @Schema(description = "Dosis prescrita en mg por kg de peso", example = "12.50")
    private BigDecimal dosisMgPorKg;

    @Min(value = 1, message = "La duración del tratamiento debe ser de al menos 1 día")
    @Schema(description = "Duración del tratamiento en días", example = "7")
    private Integer duracionDias;

    @Schema(description = "Indicaciones de administración para el propietario", example = "Administrar cada 12 horas con alimento")
    private String indicaciones;

    public TratamientoCreateRequest() {
    }

    public TratamientoCreateRequest(UUID medicamentoId, BigDecimal dosisMgPorKg, Integer duracionDias, String indicaciones) {
        this.medicamentoId = medicamentoId;
        this.dosisMgPorKg = dosisMgPorKg;
        this.duracionDias = duracionDias;
        this.indicaciones = indicaciones;
    }

    public UUID getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(UUID medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public BigDecimal getDosisMgPorKg() {
        return dosisMgPorKg;
    }

    public void setDosisMgPorKg(BigDecimal dosisMgPorKg) {
        this.dosisMgPorKg = dosisMgPorKg;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }
}
