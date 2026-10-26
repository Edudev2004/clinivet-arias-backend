package com.clinivet.arias.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando ocurre un conflicto de horario o cruce de cita para un veterinario (RN-03 / RF-15).
 * Mapea a código de estado HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class HorarioOcupadoException extends RuntimeException {

    public HorarioOcupadoException(String mensaje) {
        super(mensaje);
    }
}
