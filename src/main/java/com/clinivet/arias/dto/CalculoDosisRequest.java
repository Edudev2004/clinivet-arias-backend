package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO que encapsula los parámetros para el cálculo reactivo o directo de dosis total (RN-12 / RF-23).
 */
@Schema(description = "Solicitud para el cálculo de dosis total de medicamento")
public class CalculoDosisRequest {

    @NotNull(message = "El peso de la mascota en kilogramos es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso de la mascota debe ser mayor a 0 kg")
    @Schema(description = "Peso actual de la mascota en kilogramos (kg)", example = "28.50")
    private BigDecimal pesoKg;

    @NotNull(message = "La dosis por kilogramo es obligatoria")
    @DecimalMin(value = "0.01", message = "La dosis por kg debe ser mayor a 0 mg/kg")
    @Schema(description = "Dosis recomendada en mg por cada kg de peso corporal", example = "12.50")
    private BigDecimal dosisMgPorKg;

    public CalculoDosisRequest() {
    }

    public CalculoDosisRequest(BigDecimal pesoKg, BigDecimal dosisMgPorKg) {
        this.pesoKg = pesoKg;
        this.dosisMgPorKg = dosisMgPorKg;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getDosisMgPorKg() {
        return dosisMgPorKg;
    }

    public void setDosisMgPorKg(BigDecimal dosisMgPorKg) {
        this.dosisMgPorKg = dosisMgPorKg;
    }
}
