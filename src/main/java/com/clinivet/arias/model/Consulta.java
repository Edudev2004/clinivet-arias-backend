package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa una consulta médica veterinaria.
 * Mapea la tabla {@code consultas} de PostgreSQL según el esquema físico de APF2 (Tabla 30).
 * Contiene el peso registrado de la mascota para el cálculo de dosis (RF-23).
 */
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "cita_id", nullable = false, unique = true)
    private UUID citaId;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "temperatura_c", precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Consulta() {
    }

    public Consulta(UUID id, UUID citaId, BigDecimal pesoKg, BigDecimal temperaturaC,
                    String motivo, String diagnostico, String observaciones) {
        this.id = id;
        this.citaId = citaId;
        this.pesoKg = pesoKg;
        this.temperaturaC = temperaturaC;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCitaId() {
        return citaId;
    }

    public void setCitaId(UUID citaId) {
        this.citaId = citaId;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(BigDecimal temperaturaC) {
        this.temperaturaC = temperaturaC;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
