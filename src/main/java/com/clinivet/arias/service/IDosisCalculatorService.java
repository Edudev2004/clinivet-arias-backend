package com.clinivet.arias.service;

import com.clinivet.arias.dto.CalculoDosisRequest;
import com.clinivet.arias.dto.CalculoDosisResponse;

import java.math.BigDecimal;

/**
 * Contrato de servicio para el cálculo automático de dosis de medicamentos.
 * Cumple con SRP (exclusivamente lógica de cálculo matemático), DIP y RN-12 / RF-23.
 */
public interface IDosisCalculatorService {

    /**
     * Calcula la dosis total en miligramos aplicando la fórmula:
     * {@code DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)}.
     * Redondea el resultado a 2 decimales (RoundingMode.HALF_UP).
     *
     * @param pesoKg peso corporal de la mascota en kilogramos (debe ser mayor a cero)
     * @param dosisMgPorKg dosis prescrita por kilogramo en mg/kg (debe ser mayor a cero)
     * @return dosis total calculada en miligramos (mg)
     * @throws IllegalArgumentException si el peso o la dosis son nulos, menores o iguales a cero
     */
    BigDecimal calcularDosisTotal(BigDecimal pesoKg, BigDecimal dosisMgPorKg);

    /**
     * Procesa una solicitud de cálculo reactivo y retorna el DTO con los detalles matemáticos.
     *
     * @param request datos de peso y dosis por kg
     * @return respuesta con la dosis total calculada y la fórmula aplicada
     */
    CalculoDosisResponse calcular(CalculoDosisRequest request);
}
