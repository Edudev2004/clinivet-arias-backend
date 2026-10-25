/**
 * Capa de Servicios — Lógica de Negocio.
 *
 * <p>Contiene interfaces de servicio y sus implementaciones anotadas con
 * {@code @Service}. Esta capa:
 * <ul>
 *   <li>Aloja todas las reglas de negocio del sistema (RN-01 a RN-12).</li>
 *   <li>Coordina llamadas a uno o más repositorios cuando es necesario.</li>
 *   <li>Aplica validaciones con Google Guava ({@code Preconditions}) y
 *       Apache Commons Lang3 ({@code StringUtils}).</li>
 *   <li>Nunca depende de clases HTTP (sin {@code HttpServletRequest} aquí).</li>
 * </ul>
 *
 * <p><strong>SOLID aplicado:</strong>
 * <ul>
 *   <li>SRP — cada servicio tiene una única responsabilidad de negocio.</li>
 *   <li>OCP — se extiende con nuevas implementaciones sin modificar las interfaces.</li>
 *   <li>DIP — los controllers dependen de las interfaces declaradas aquí.</li>
 * </ul>
 */
package com.clinivet.arias.service;
