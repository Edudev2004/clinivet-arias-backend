package com.clinivet.arias.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad JPA que representa una vacuna en el catálogo maestro.
 * Mapea la tabla {@code vacunas_catalogo} según el esquema APF2 (Tabla 31).
 */
@Entity
@Table(name = "vacunas_catalogo")
public class VacunaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    public VacunaCatalogo() {
    }

    public VacunaCatalogo(String nombre) {
        this.nombre = nombre;
    }

    public VacunaCatalogo(UUID id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
