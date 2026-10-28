package com.clinivet.arias.exception;

/**
 * Excepción lanzada cuando se intenta registrar un recurso que ya existe en el sistema
 * y viola una restricción de unicidad (como un DNI duplicado en propietarios).
 * Mapeada a HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
