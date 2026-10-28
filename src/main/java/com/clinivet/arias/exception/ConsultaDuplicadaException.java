package com.clinivet.arias.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando ya existe una consulta registrada para la cita (RN-09: historia clínica inmutable).
 * Mapea a código de estado HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConsultaDuplicadaException extends RuntimeException {

    public ConsultaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
