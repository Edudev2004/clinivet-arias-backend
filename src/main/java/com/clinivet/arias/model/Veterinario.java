package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad JPA para la tabla {@code veterinarios} (Tabla 28 de APF2).
 * Especialización de Usuario con número de colegiatura CMVP y especialidad médica.
 */
@Entity
@Table(name = "veterinarios")
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @Column(name = "numero_colegiatura", length = 50)
    private String numeroColegiatura;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Veterinario() {
    }

    public Veterinario(UUID id, Usuario usuario, Especialidad especialidad, String numeroColegiatura, Boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.numeroColegiatura = numeroColegiatura;
        this.activo = activo != null ? activo : true;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroColegiatura() {
        return numeroColegiatura;
    }

    public void setNumeroColegiatura(String numeroColegiatura) {
        this.numeroColegiatura = numeroColegiatura;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        if (usuario != null) {
            return (usuario.getNombre() != null ? usuario.getNombre() : "") + " " +
                   (usuario.getApellido() != null ? usuario.getApellido() : "");
        }
        return "";
    }
}
