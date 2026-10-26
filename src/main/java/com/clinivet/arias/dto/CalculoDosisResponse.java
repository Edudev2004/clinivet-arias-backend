package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO de respuesta con el resultado del cálculo matemático de la dosis total (RN-12 / RF-23).
 */
@Schema(description = "Resultado del cálculo de dosis total")
public class CalculoDosisResponse {

    @Schema(description = "Peso de la mascota en kg", example = "28.50")
    private BigDecimal pesoKg;

    @Schema(description = "Dosis base en mg/kg", example = "12.50")
    private BigDecimal dosisMgPorKg;

    @Schema(description = "Dosis total calculada en mg (pesoKg * dosisMgPorKg)", example = "356.25")
    private BigDecimal dosisTotalMg;

    @Schema(description = "Unidad de medida resultante", example = "mg")
    private String unidadMedida;

    @Schema(description = "Fórmula matemática aplicada", example = "DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)")
    private String formula;

    public CalculoDosisResponse() {
    }

    public CalculoDosisResponse(BigDecimal pesoKg, BigDecimal dosisMgPorKg, BigDecimal dosisTotalMg) {
        this.pesoKg = pesoKg;
        this.dosisMgPorKg = dosisMgPorKg;
        this.dosisTotalMg = dosisTotalMg;
        this.unidadMedida = "mg";
        this.formula = "DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)";
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

    public BigDecimal getDosisTotalMg() {
        return dosisTotalMg;
    }

    public void setDosisTotalMg(BigDecimal dosisTotalMg) {
        this.dosisTotalMg = dosisTotalMg;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}
