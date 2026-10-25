/**
 * Capa de Repositorios — DAO (Data Access Object).
 *
 * <p>Contiene las interfaces que extienden {@code JpaRepository<T, ID>}
 * de Spring Data JPA. Esta capa:
 * <ul>
 *   <li>Abstrae completamente el acceso a la base de datos PostgreSQL.</li>
 *   <li>Provee operaciones CRUD de forma automática sin código boilerplate.</li>
 *   <li>Permite definir consultas derivadas ({@code findByUsername}) y
 *       consultas JPQL con {@code @Query} para casos específicos.</li>
 * </ul>
 *
 * <p><strong>Patrón DAO:</strong> Spring Data JPA es la evolución estándar del
 * patrón DAO en el ecosistema Java/Spring. La interfaz {@code JpaRepository}
 * actúa como el DAO, ocultando los detalles de Hibernate y JDBC.
 */
package com.clinivet.arias.repository;
