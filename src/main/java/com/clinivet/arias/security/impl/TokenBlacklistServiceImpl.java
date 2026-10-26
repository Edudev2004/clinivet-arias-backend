package com.clinivet.arias.security.impl;

import com.clinivet.arias.security.ITokenBlacklistService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria y concurrente del servicio de invalidación de tokens y control de inactividad.
 * Cumple con SRP (solo gestiona el ciclo de vida y blacklist de tokens) y RF-04 / RNF-04.
 */
@Service
public class TokenBlacklistServiceImpl implements ITokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    // Tokens invalidados tras logout: token -> timestamp de invalidación
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    // Registro de última actividad: token -> timestamp en milisegundos
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();

    private final long inactivityTimeoutMs;

    public TokenBlacklistServiceImpl(
            @Value("${jwt.inactivity-timeout-ms:1800000}") long inactivityTimeoutMs) {
        this.inactivityTimeoutMs = inactivityTimeoutMs;
    }

    @Override
    public void invalidateToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        long now = System.currentTimeMillis();
        blacklistedTokens.put(token, now);
        lastActivityMap.remove(token);
        log.info("Token JWT invalidado exitosamente tras cierre de sesión (blacklist)");
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        return blacklistedTokens.containsKey(token);
    }

    @Override
    public void recordActivity(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        lastActivityMap.put(token, System.currentTimeMillis());
    }

    @Override
    public boolean isSessionInactive(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        Long lastActive = lastActivityMap.get(token);
        if (lastActive == null) {
            // Primera petición con este token, se registra actividad y no se considera inactivo
            recordActivity(token);
            return false;
        }

        long elapsed = System.currentTimeMillis() - lastActive;
        boolean inactive = elapsed > inactivityTimeoutMs;

        if (inactive) {
            log.warn("Sesión expirada por inactividad tras {} ms (límite: {} ms)", elapsed, inactivityTimeoutMs);
            // Invalida el token automáticamente al detectar inactividad
            invalidateToken(token);
        }

        return inactive;
    }

    @Override
    public long getInactivityTimeoutMs() {
        return inactivityTimeoutMs;
    }

    // Método utilitario para pruebas unitarias: limpiar estado
    public void clear() {
        blacklistedTokens.clear();
        lastActivityMap.clear();
    }
}
