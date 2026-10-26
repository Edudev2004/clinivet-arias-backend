package com.clinivet.arias.security;

/**
 * Contrato de servicio para la invalidación de tokens JWT y control de inactividad.
 * Cumple con el Principio de Inversión de Dependencias (DIP) y Requerimiento RF-04 / RNF-04.
 */
public interface ITokenBlacklistService {

    /**
     * Invalida un token JWT agregándolo a la lista negra tras el cierre de sesión (logout).
     *
     * @param token token JWT en formato compacto
     */
    void invalidateToken(String token);

    /**
     * Verifica si un token ha sido invalidado previamente por logout.
     *
     * @param token token JWT a verificar
     * @return true si el token está en lista negra, false si está activo
     */
    boolean isTokenInvalidated(String token);

    /**
     * Registra o actualiza la marca de tiempo de la última actividad del usuario.
     * Reinicia el período de inactividad con cada petición autenticada (RF-04).
     *
     * @param token token JWT de la petición activa
     */
    void recordActivity(String token);

    /**
     * Determina si la sesión ha expirado por superar el período máximo de inactividad permitido.
     *
     * @param token token JWT a verificar
     * @return true si la sesión ha expirado por inactividad, false en caso contrario
     */
    boolean isSessionInactive(String token);

    /**
     * Retorna el tiempo máximo de inactividad configurado en milisegundos.
     *
     * @return milisegundos de inactividad tolerados
     */
    long getInactivityTimeoutMs();
}
