package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.LoginRequest;
import com.clinivet.arias.dto.LoginResponse;
import com.clinivet.arias.model.Usuario;
import com.clinivet.arias.repository.UsuarioRepository;
import com.clinivet.arias.security.JwtUtil;
import com.clinivet.arias.service.IAuthService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para autenticación de usuarios.
 * Aplica principios SOLID:
 * - SRP: Se encarga exclusivamente de la verificación de credenciales y expedición de tokens.
 * - DIP: Inyecta interfaces (UsuarioRepository, PasswordEncoder, IAuthService).
 * - Uso de librerías del curso: Google Guava (Preconditions) y Apache Commons (StringUtils).
 */
@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Inyección estricta por constructor (DIP)
    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Validación defensiva con Google Guava
        Preconditions.checkNotNull(request, "La solicitud de inicio de sesión no puede ser nula");
        
        String identifier = request.getIdentifier();
        if (StringUtils.isBlank(identifier)) {
            log.warn("Intento de login fallido: identificador (email/username) vacío");
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        if (StringUtils.isBlank(request.getPassword())) {
            log.warn("Intento de login fallido para [{}]: contraseña vacía", identifier);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // Buscar el usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(identifier)
                .orElse(null);

        // Si el usuario no existe en la base de datos
        if (usuario == null) {
            log.warn("Intento de login fallido: el usuario [{}] no existe en el sistema", identifier);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // Si la cuenta está desactivada (RF-02)
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            log.warn("Intento de login bloqueado: la cuenta de [{}] se encuentra desactivada", identifier);
            throw new DisabledException("Tu cuenta está desactivada");
        }

        // Comparar contraseñas mediante BCrypt (nunca en texto plano)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash());
        if (!passwordMatches) {
            log.warn("Intento de login fallido para [{}]: contraseña no coincide", identifier);
            // Mensaje genérico de seguridad (RF-02)
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // Generar token JWT con claims de usuario y rol
        String token = jwtUtil.generateToken(usuario);
        log.info("Inicio de sesión exitoso para usuario [{}] con rol [{}]", usuario.getEmail(), usuario.getRol());

        return new LoginResponse(
                token,
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getRol().name(),
                jwtUtil.getExpirationMs()
        );
    }
}
