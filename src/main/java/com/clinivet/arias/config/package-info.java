/**
 * Configuración de la aplicación.
 *
 * <p>Contiene las clases de configuración de Spring Boot anotadas con
 * {@code @Configuration}. Incluye:
 * <ul>
 *   <li>{@code SecurityConfig} — cadena de filtros HTTP, CORS, rutas públicas
 *       y privadas, y el bean {@code BCryptPasswordEncoder}.</li>
 *   <li>{@code OpenApiConfig} — metadatos de la documentación Swagger/OpenAPI
 *       (título, versión, descripción, esquema de seguridad JWT).</li>
 * </ul>
 */
package com.clinivet.arias.config;
