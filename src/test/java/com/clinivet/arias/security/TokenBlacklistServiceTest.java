package com.clinivet.arias.security;

import com.clinivet.arias.security.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link TokenBlacklistServiceImpl}.
 * Valida los criterios de aceptación de RF-04 y RNF-04 (logout e inactividad).
 */
class TokenBlacklistServiceTest {

    private TokenBlacklistServiceImpl blacklistService;
    private final String sampleToken = "sample.mocked.jwt.token.12345";

    @BeforeEach
    void setUp() {
        // Timeout corto de 100 ms para pruebas de inactividad
        blacklistService = new TokenBlacklistServiceImpl(100L);
    }

    @Test
    @DisplayName("RF-04: Token recién emitido no se encuentra en lista negra")
    void tokenNuevo_NoEstaEnBlacklist() {
        assertFalse(blacklistService.isTokenInvalidated(sampleToken));
    }

    @Test
    @DisplayName("RF-04: Invalida token correctamente tras logout manual")
    void invalidateToken_AgregaTokenABlacklist() {
        blacklistService.invalidateToken(sampleToken);
        assertTrue(blacklistService.isTokenInvalidated(sampleToken));
    }

    @Test
    @DisplayName("RF-04 / RNF-04: Sesión no se considera inactiva dentro de la ventana de tiempo")
    void isSessionInactive_DentroDeVentana_RetornaFalse() {
        blacklistService.recordActivity(sampleToken);
        assertFalse(blacklistService.isSessionInactive(sampleToken));
    }

    @Test
    @DisplayName("RF-04 / RNF-04: Sesión expira automáticamente al superar el tiempo de inactividad")
    void isSessionInactive_SuperaTiempoInactividad_RetornaTrueEInvalida() throws InterruptedException {
        blacklistService.recordActivity(sampleToken);
        
        // Esperar 150 ms para superar el timeout de 100 ms
        Thread.sleep(150);

        assertTrue(blacklistService.isSessionInactive(sampleToken), "La sesión debió marcarse como inactiva");
        assertTrue(blacklistService.isTokenInvalidated(sampleToken), "El token debió agregarse a blacklist automáticamente");
    }

    @Test
    @DisplayName("RF-04: Cada petición activa reinicia el contador de inactividad")
    void recordActivity_ReiniciaContadorDeInactividad() throws InterruptedException {
        blacklistService.recordActivity(sampleToken);
        
        // Esperar 60 ms
        Thread.sleep(60);
        // Reiniciar actividad
        blacklistService.recordActivity(sampleToken);
        // Esperar otros 60 ms (total 120 ms transcurridos pero con reset intermedio)
        Thread.sleep(60);

        assertFalse(blacklistService.isSessionInactive(sampleToken), "La sesión debe mantenerse activa por el reset");
    }
}
