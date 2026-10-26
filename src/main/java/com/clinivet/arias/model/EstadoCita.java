package com.clinivet.arias.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración que representa el ciclo de vida de una cita médica (APF2 Tabla 30 y Tabla 33).
 * Mapea a tipo PostgreSQL {@code estado_cita}: 'programada', 'atendida', 'cancelada'.
 */
public enum EstadoCita {
    programada("programada", "PROGRAMADA"),
    atendida("atendida", "ATENDIDA"),
    cancelada("cancelada", "CANCELADA");

    private final String dbValue;
    private final String displayValue;

    EstadoCita(String dbValue, String displayValue) {
        this.dbValue = dbValue;
        this.displayValue = displayValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator
    public static EstadoCita fromString(String value) {
        if (value == null) {
            return null;
        }
        for (EstadoCita e : values()) {
            if (e.dbValue.equalsIgnoreCase(value) || e.displayValue.equalsIgnoreCase(value) || e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de cita no válido: " + value);
    }
}
