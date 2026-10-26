package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que retorna los datos del tratamiento prescrito con su dosis total calculada automáticamente (RF-23 / RN-12).
 */
@Schema(description = "Datos del tratamiento médico registrado con dosis total calculada")
public class TratamientoResponse {

    @Schema(description = "ID del tratamiento", example = "d0000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "ID de la consulta médica asociada", example = "e0000000-0000-0000-0000-000000000001")
    private UUID consultaId;

    @Schema(description = "ID del medicamento prescrito", example = "c0000000-0000-0000-0000-000000000001")
    private UUID medicamentoId;

    @Schema(description = "Nombre comercial del medicamento", example = "Amoxicilina + Clavulánico 250mg")
    private String medicamentoNombre;

    @Schema(description = "Principio activo del medicamento", example = "Amoxicilina / Ác. Clavulánico")
    private String principioActivo;

    @Schema(description = "Peso de la mascota registrado en la consulta (kg)", example = "28.50")
    private BigDecimal pesoKgAplicado;

    @Schema(description = "Dosis por kg formulada (mg/kg)", example = "12.50")
    private BigDecimal dosisMgPorKg;

    @Schema(description = "Dosis total calculada en mg (pesoKg * dosisMgPorKg)", example = "356.25")
    private BigDecimal dosisTotalMg;

    @Schema(description = "Duración en días", example = "7")
    private Integer duracionDias;

    @Schema(description = "Indicaciones para administración", example = "Administrar cada 12 horas con alimento")
    private String indicaciones;

    @Schema(description = "Fecha de registro del tratamiento")
    private LocalDateTime createdAt;

    public TratamientoResponse() {
    }

    public TratamientoResponse(UUID id, UUID consultaId, UUID medicamentoId,
                               String medicamentoNombre, String principioActivo,
                               BigDecimal pesoKgAplicado, BigDecimal dosisMgPorKg,
                               BigDecimal dosisTotalMg, Integer duracionDias,
                               String indicaciones, LocalDateTime createdAt) {
        this.id = id;
        this.consultaId = consultaId;
        this.medicamentoId = medicamentoId;
        this.medicamentoNombre = medicamentoNombre;
        this.principioActivo = principioActivo;
        this.pesoKgAplicado = pesoKgAplicado;
        this.dosisMgPorKg = dosisMgPorKg;
        this.dosisTotalMg = dosisTotalMg;
        this.duracionDias = duracionDias;
        this.indicaciones = indicaciones;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(UUID consultaId) {
        this.consultaId = consultaId;
    }

    public UUID getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(UUID medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public String getMedicamentoNombre() {
        return medicamentoNombre;
    }

    public void setMedicamentoNombre(String medicamentoNombre) {
        this.medicamentoNombre = medicamentoNombre;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public BigDecimal getPesoKgAplicado() {
        return pesoKgAplicado;
    }

    public void setPesoKgAplicado(BigDecimal pesoKgAplicado) {
        this.pesoKgAplicado = pesoKgAplicado;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
