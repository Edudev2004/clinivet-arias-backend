package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.CalculoDosisRequest;
import com.clinivet.arias.dto.CalculoDosisResponse;
import com.clinivet.arias.service.IDosisCalculatorService;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementación del servicio de cálculo automático de dosis de medicamentos veterinarios.
 * Aplica principios SOLID:
 * - SRP: Exclusivamente responsable de validar rangos numéricos y ejecutar la fórmula matemática.
 * - DIP: Implementa la abstracción {@link IDosisCalculatorService}.
 * - Librerías obligatorias: Google Guava {@link Preconditions} para validaciones defensivas.
 * Cumple con RN-12 y RF-23 según APF2.
 */
@Service
public class DosisCalculatorServiceImpl implements IDosisCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(DosisCalculatorServiceImpl.class);

    @Override
    public BigDecimal calcularDosisTotal(BigDecimal pesoKg, BigDecimal dosisMgPorKg) {
        Preconditions.checkArgument(pesoKg != null, "El peso de la mascota no puede ser nulo");
        Preconditions.checkArgument(pesoKg.compareTo(BigDecimal.ZERO) > 0,
                "El peso de la mascota debe ser mayor a 0 kg (recibido: %s)", pesoKg);

        Preconditions.checkArgument(dosisMgPorKg != null, "La dosis por kg no puede ser nula");
        Preconditions.checkArgument(dosisMgPorKg.compareTo(BigDecimal.ZERO) > 0,
                "La dosis por kg debe ser mayor a 0 mg/kg (recibido: %s)", dosisMgPorKg);

        // Fórmula RN-12 / RF-23: DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)
        BigDecimal dosisTotal = pesoKg.multiply(dosisMgPorKg).setScale(2, RoundingMode.HALF_UP);

        log.debug("Cálculo de dosis ejecutado: Peso={} kg * DosisBase={} mg/kg = Total={} mg",
                pesoKg, dosisMgPorKg, dosisTotal);

        return dosisTotal;
    }

    @Override
    public CalculoDosisResponse calcular(CalculoDosisRequest request) {
        Preconditions.checkArgument(request != null, "La solicitud de cálculo no puede ser nula");

        BigDecimal dosisTotal = calcularDosisTotal(request.getPesoKg(), request.getDosisMgPorKg());
        return new CalculoDosisResponse(request.getPesoKg(), request.getDosisMgPorKg(), dosisTotal);
    }
}
