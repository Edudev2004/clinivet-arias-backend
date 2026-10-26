package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidad JPA que representa una franja horaria de atención semanal configurada para un veterinario.
 * Mapea la tabla {@code horarios_disponibilidad} según el esquema físico de APF2 (Tabla 28 / RN-11).
 * {@code diaSemana}: 0=Domingo, 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado.
 */
@Entity
@Table(name = "horarios_disponibilidad")
public class HorarioDisponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    public HorarioDisponibilidad() {
    }

    public HorarioDisponibilidad(UUID id, Veterinario veterinario, Integer diaSemana,
                                 LocalTime horaInicio, LocalTime horaFin) {
        this.id = id;
        this.veterinario = veterinario;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    /**
     * Verifica si una hora dada se encuentra dentro del rango [horaInicio, horaFin).
     */
    public boolean incluyeHora(LocalTime hora) {
        if (hora == null) {
            return false;
        }
        return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }
}
