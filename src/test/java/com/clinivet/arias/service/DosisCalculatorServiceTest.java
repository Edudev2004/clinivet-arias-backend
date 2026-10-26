package com.clinivet.arias.service;

import com.clinivet.arias.dto.CalculoDosisRequest;
import com.clinivet.arias.dto.CalculoDosisResponse;
import com.clinivet.arias.service.impl.DosisCalculatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link DosisCalculatorServiceImpl}.
 * Valida los criterios de aceptación de RF-23 y RN-12:
 * - Fórmula matemática: DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)
 * - Redondeo a 2 decimales
 * - Validación de entradas inválidas o nulas
 */
class DosisCalculatorServiceTest {

    private DosisCalculatorServiceImpl calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new DosisCalculatorServiceImpl();
    }

    @Test
    @DisplayName("RF-23: Cálculo de dosis total exacto con enteros (10 kg * 5 mg/kg = 50.00 mg)")
    void calcularDosisTotal_ValoresEnteros_CalculaExacto() {
        // Arrange
        BigDecimal peso = new BigDecimal("10.00");
        BigDecimal dosis = new BigDecimal("5.00");

        // Act
        BigDecimal resultado = calculatorService.calcularDosisTotal(peso, dosis);

        // Assert
        assertEquals(new BigDecimal("50.00"), resultado);
    }

    @Test
    @DisplayName("RF-23: Cálculo de dosis total con decimales (28.50 kg * 12.50 mg/kg = 356.25 mg)")
    void calcularDosisTotal_ValoresDecimales_CalculaExacto() {
        // Arrange
        BigDecimal peso = new BigDecimal("28.50");
        BigDecimal dosis = new BigDecimal("12.50");

        // Act
        BigDecimal resultado = calculatorService.calcularDosisTotal(peso, dosis);

        // Assert
        assertEquals(new BigDecimal("356.25"), resultado);
    }

    @Test
    @DisplayName("RF-23: Cálculo de dosis para mascota pequeña con redondeo (1.33 kg * 3.33 mg/kg = 4.43 mg)")
    void calcularDosisTotal_RedondeoMitadHaciaArriba() {
        // Arrange
        BigDecimal peso = new BigDecimal("1.33");
        BigDecimal dosis = new BigDecimal("3.33");

        // Act
        BigDecimal resultado = calculatorService.calcularDosisTotal(peso, dosis);

        // Assert: 1.33 * 3.33 = 4.4289 -> redondea a 4.43
        assertEquals(new BigDecimal("4.43"), resultado);
    }

    @Test
    @DisplayName("RF-23: Peso nulo lanza IllegalArgumentException")
    void calcularDosisTotal_PesoNulo_LanzaExcepcion() {
        BigDecimal dosis = new BigDecimal("10.00");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(null, dosis)
        );
        assertTrue(ex.getMessage().contains("peso de la mascota no puede ser nulo"));
    }

    @Test
    @DisplayName("RF-23: Peso menor o igual a cero lanza IllegalArgumentException")
    void calcularDosisTotal_PesoInvalido_LanzaExcepcion() {
        BigDecimal dosis = new BigDecimal("10.00");

        IllegalArgumentException exZero = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(BigDecimal.ZERO, dosis)
        );
        assertTrue(exZero.getMessage().contains("debe ser mayor a 0 kg"));

        IllegalArgumentException exNeg = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(new BigDecimal("-2.5"), dosis)
        );
        assertTrue(exNeg.getMessage().contains("debe ser mayor a 0 kg"));
    }

    @Test
    @DisplayName("RF-23: Dosis nula lanza IllegalArgumentException")
    void calcularDosisTotal_DosisNula_LanzaExcepcion() {
        BigDecimal peso = new BigDecimal("15.00");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(peso, null)
        );
        assertTrue(ex.getMessage().contains("dosis por kg no puede ser nula"));
    }

    @Test
    @DisplayName("RF-23: Dosis menor o igual a cero lanza IllegalArgumentException")
    void calcularDosisTotal_DosisInvalida_LanzaExcepcion() {
        BigDecimal peso = new BigDecimal("15.00");

        IllegalArgumentException exZero = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(peso, BigDecimal.ZERO)
        );
        assertTrue(exZero.getMessage().contains("debe ser mayor a 0 mg/kg"));

        IllegalArgumentException exNeg = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.calcularDosisTotal(peso, new BigDecimal("-5.00"))
        );
        assertTrue(exNeg.getMessage().contains("debe ser mayor a 0 mg/kg"));
    }

    @Test
    @DisplayName("RN-12 / RF-23: Método calcular(request) retorna DTO con fórmula y unidad mg")
    void calcular_RequestValido_RetornaResponseCompleto() {
        // Arrange
        CalculoDosisRequest request = new CalculoDosisRequest(new BigDecimal("20.00"), new BigDecimal("7.50"));

        // Act
        CalculoDosisResponse response = calculatorService.calcular(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("20.00"), response.getPesoKg());
        assertEquals(new BigDecimal("7.50"), response.getDosisMgPorKg());
        assertEquals(new BigDecimal("150.00"), response.getDosisTotalMg());
        assertEquals("mg", response.getUnidadMedida());
        assertTrue(response.getFormula().contains("DosisTotal"));
    }
}
