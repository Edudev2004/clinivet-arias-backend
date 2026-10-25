# Reglas de Diseño y Principios SOLID — Backend (Java / Spring Boot)

El desarrollo del backend de Clinivet Arias debe seguir rigurosamente los **5 principios SOLID** y la arquitectura limpia de 3 capas.

---

### 1. S — Single Responsibility Principle (Responsabilidad Única)
* Cada clase debe tener una sola razón para cambiar:
  * **Controllers (`@RestController`):** Únicamente orquestan HTTP, reciben el request, invocan el servicio correspondiente y devuelven el `ResponseEntity<T>` con el código HTTP apropiado (200, 201, 400, 401, 404, 409). **Cero lógica de negocio**.
  * **Services (`@Service`):** Contienen el 100% de la lógica de negocio, reglas de validación (RN-01 a RN-12), cálculos y transaccionalidad (`@Transactional`).
  * **Repositories (`@Repository`):** Exclusivamente acceso y persistencia a la base de datos extendiendo `JpaRepository`.
  * **DTOs:** Clases inmutables o POJOs dedicados solo a transferir datos entre cliente y servidor.
  * **Entities (`@Entity`):** Mapeo de tablas de la base de datos sin lógica de presentación ni negocio ajeno a su estado.

---

### 2. O — Open / Closed Principle (Abierto / Cerrado)
* Las clases deben estar **abiertas a la extensión pero cerradas a la modificación**:
  * Definir contratos mediante interfaces para los servicios de negocio (ej. `IAuthService`, `ICitaService`).
  * Si se requiere un nuevo comportamiento (ej. autenticación OAuth2, nuevo exportador PDF/Excel), se crea una nueva implementación o estrategia sin modificar el código existente probado.

---

### 3. L — Liskov Substitution Principle (Sustitución de Liskov)
* Las implementaciones concretas de una interfaz deben ser sustituibles por sus contratos de interfaz sin alterar la corrección del programa:
  * Ninguna implementación debe lanzar `UnsupportedOperationException` para métodos del contrato.
  * Los tipos de retorno y excepciones respetan las promesas de la interfaz.

---

### 4. I — Interface Segregation Principle (Segregación de Interfaces)
* Ningún cliente debe ser forzado a depender de métodos que no usa:
  * Interfaces pequeñas y específicas por módulo de negocio (ej. `IAuthService`, `ICitaService`, `IInventarioService`) en lugar de una interfaz genérica gigante.
  * Separar interfaces de lectura y escritura si el caso de uso lo amerita.

---

### 5. D — Dependency Inversion Principle (Inversión de Dependencias)
* Los módulos de alto nivel no deben depender de módulos de bajo nivel; ambos deben depender de abstracciones:
  * Los **Controllers** inyectan la interfaz (`IAuthService`), nunca la clase concreta (`AuthServiceImpl`).
  * Los **Services** inyectan interfaces de repositorio (`UsuarioRepository`), nunca implementaciones JDBC directas.
  * **Inyección estricta por constructor:** Usar inyección por constructor (con `final` fields o `@RequiredArgsConstructor` de Lombok). **Evitar `@Autowired` directo en atributos privados** para facilitar testing unitario con Mockito.
