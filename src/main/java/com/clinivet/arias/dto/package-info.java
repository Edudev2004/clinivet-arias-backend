/**
 * DTOs — Data Transfer Objects.
 *
 * <p>Contiene los objetos de transferencia de datos que viajan entre
 * el cliente (Angular) y el backend. Se dividen en:
 * <ul>
 *   <li><strong>Request DTOs</strong> — datos que llegan del cliente al controller
 *       (con anotaciones de validación Bean Validation).</li>
 *   <li><strong>Response DTOs</strong> — datos que el backend devuelve al cliente
 *       (nunca incluyen campos sensibles como contraseñas o tokens internos).</li>
 * </ul>
 *
 * <p>El desacoplamiento entre entidades JPA y DTOs evita exponer la estructura
 * interna de la base de datos y facilita la evolución independiente de la API.
 */
package com.clinivet.arias.dto;
