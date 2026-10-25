package com.clinivet.arias.service;

import com.clinivet.arias.dto.LoginRequest;
import com.clinivet.arias.dto.LoginResponse;

/**
 * Contrato de servicio para operaciones de autenticación y sesión.
 * Cumple con el principio de Inversión de Dependencias (DIP) e
 * Interface Segregation (ISP).
 */
public interface IAuthService {

    /**
     * Valida las credenciales del usuario y genera un token JWT.
     *
     * @param request credenciales enviadas (email/username y contraseña)
     * @return respuesta con el token JWT y perfil del usuario
     */
    LoginResponse login(LoginRequest request);
}
