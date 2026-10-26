package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un tratamiento prescrito en una consulta médica.
 * Mapea la tabla {@code tratamientos} de PostgreSQL según el esquema físico de APF2 (Tabla 30).
 * Almacena la dosis total calculada automáticamente: dosisTotalMg = pesoKg * dosisMgPorKg (RF-23).
 */
@Entity
@Table(name = "tratamientos")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis_mg_por_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal dosisMgPorKg;

    @Column(name = "dosis_total_mg", nullable = false, precision = 8, scale = 2)
    private BigDecimal dosisTotalMg;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Tratamiento() {
    }

    public Tratamiento(UUID id, Consulta consulta, Medicamento medicamento,
                       BigDecimal dosisMgPorKg, BigDecimal dosisTotalMg,
                       Integer duracionDias, String indicaciones) {
        this.id = id;
        this.consulta = consulta;
        this.medicamento = medicamento;
        this.dosisMgPorKg = dosisMgPorKg;
        this.dosisTotalMg = dosisTotalMg;
        this.duracionDias = duracionDias;
        this.indicaciones = indicaciones;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
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
