/**
 * Capa de Controladores — MVC (C de Controller).
 *
 * <p>Contiene las clases anotadas con {@code @RestController} que:
 * <ul>
 *   <li>Reciben las peticiones HTTP del cliente (Angular).</li>
 *   <li>Validan el DTO de entrada mediante Bean Validation.</li>
 *   <li>Delegan <em>toda</em> la lógica de negocio al servicio correspondiente.</li>
 *   <li>Retornan la respuesta HTTP con el código de estado adecuado
 *       (200, 201, 400, 401, 409, etc.).</li>
 * </ul>
 *
 * <p><strong>SOLID aplicado:</strong> Los controllers no contienen lógica de negocio
 * (SRP). Dependen de interfaces de servicio, no de implementaciones concretas (DIP).
 */
package com.clinivet.arias.controller;
