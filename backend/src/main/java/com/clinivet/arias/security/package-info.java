/**
 * Seguridad — Spring Security + JWT.
 *
 * <p>Contiene las clases responsables de la autenticación y autorización:
 * <ul>
 *   <li>{@code JwtUtil} — generación y validación de tokens JWT con JJWT.</li>
 *   <li>{@code JwtAuthenticationFilter} — filtro que intercepta cada petición HTTP,
 *       extrae el token del header {@code Authorization: Bearer <token>} y
 *       establece el contexto de seguridad de Spring.</li>
 *   <li>{@code UserDetailsServiceImpl} — adaptador entre Spring Security y
 *       nuestro {@code UsuarioRepository}.</li>
 * </ul>
 *
 * <p><strong>Regla crítica:</strong> Ningún log de esta capa debe registrar
 * contraseñas, tokens completos ni datos sensibles del usuario.
 */
package com.clinivet.arias.security;
