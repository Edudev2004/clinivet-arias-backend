package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un medicamento o insumo clínico.
 * Mapea la tabla {@code medicamentos} de PostgreSQL según el esquema físico de APF2 (Tabla 32).
 */
@Entity
@Table(name = "medicamentos")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "principio_activo", length = 150)
    private String principioActivo;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida;

    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.valueOf(5.00);

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.stockMinimo == null) {
            this.stockMinimo = BigDecimal.valueOf(5.00);
        }
    }

    public Medicamento() {
    }

    public Medicamento(UUID id, String nombre, String principioActivo, String presentacion,
                       String unidadMedida, BigDecimal stockMinimo, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.principioActivo = principioActivo;
        this.presentacion = presentacion;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
