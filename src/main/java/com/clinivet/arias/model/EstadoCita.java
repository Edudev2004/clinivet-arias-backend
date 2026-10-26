package com.clinivet.arias.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Estados posibles de una cita médica según el catálogo oficial de APF2 (Tabla 33).
 */
public enum EstadoCita {
    PROGRAMADA("programada"),
    ATENDIDA("atendida"),
    CANCELADA("cancelada");

    private final String codigo;

    EstadoCita(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    @JsonValue
    public String getDisplayValue() {
        return name();
    }

    @JsonCreator
    public static EstadoCita fromCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (EstadoCita e : values()) {
            if (e.codigo.equalsIgnoreCase(codigo) || e.name().equalsIgnoreCase(codigo)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de cita no válido: " + codigo);
    }
}
