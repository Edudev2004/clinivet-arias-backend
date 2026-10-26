package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa una cita médica veterinaria programada.
 * Mapea la tabla {@code citas} según el esquema físico de APF2 (Tabla 30 / RN-02 / RN-03 / RN-07).
 */
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "mascota_id", nullable = false)
    private UUID mascotaId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Convert(converter = EstadoCitaConverter.class)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoCita.PROGRAMADA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Cita() {
    }

    public Cita(UUID id, UUID mascotaId, Veterinario veterinario,
                LocalDateTime fechaHora, EstadoCita estado, String motivo, UUID creadoPor) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.estado = estado != null ? estado : EstadoCita.PROGRAMADA;
        this.motivo = motivo;
        this.creadoPor = creadoPor;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(UUID mascotaId) {
        this.mascotaId = mascotaId;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public UUID getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(UUID creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
