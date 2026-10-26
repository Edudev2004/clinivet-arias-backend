package com.clinivet.arias.model;

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

    public static EstadoCita fromCodigo(String codigo) {
        for (EstadoCita e : values()) {
            if (e.codigo.equalsIgnoreCase(codigo) || e.name().equalsIgnoreCase(codigo)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de cita no válido: " + codigo);
    }
}
