/**
 * Capa de Modelo — MVC (M de Model).
 *
 * <p>Contiene las entidades JPA anotadas con {@code @Entity} que representan
 * las tablas de la base de datos PostgreSQL. Esta capa:
 * <ul>
 *   <li>Define la estructura de datos del dominio (Usuario, Mascota, Cita, etc.).</li>
 *   <li>Establece las relaciones entre entidades ({@code @OneToMany}, {@code @ManyToOne}).</li>
 *   <li>Configura restricciones de integridad a nivel de Java ({@code @Column(nullable=false)}).</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Las entidades NO se exponen directamente en la API.
 * Se usan DTOs (ver paquete {@code dto}) para desacoplar la representación
 * interna de la representación pública.
 */
package com.clinivet.arias.model;
