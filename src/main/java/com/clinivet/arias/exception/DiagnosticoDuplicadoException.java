package com.clinivet.arias.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando ya existe un diagnóstico registrado para la consulta médica (RF-21).
 * Mapea a código de estado HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DiagnosticoDuplicadoException extends RuntimeException {

    public DiagnosticoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
