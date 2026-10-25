# Reglas de Pruebas Unitarias (TDD) y Calidad de Código — Backend

Este archivo establece las directrices para el desarrollo guiado por pruebas (TDD) y el estándar de calidad en el backend de Clinivet Arias según **APF2 (Capítulo 2: WBS 5.1 y Capítulo 3: Criterios de Aceptación CA-01 a CA-15)**.

---

## 1. Enfoque TDD Obligatorio

* **Cada requerimiento funcional (RF-XX) debe contar con sus pruebas unitarias:**
  1. Escribir primero la prueba que define el comportamiento esperado del caso de uso.
  2. Implementar el código mínimo necesario para que la prueba compile y pase.
  3. Refactorizar respetando los principios SOLID.
* **Comando de verificación:**
  ```powershell
  .\mvnw.cmd test
  ```
  Ningún commit ni PR debe mergearse con pruebas rotas o fallidas.

---

## 2. Estructura de Pruebas Unitarias

* **Capa de Servicios (`ServiceTest`):**
  * Uso de `@ExtendWith(MockitoExtension.class)`.
  * Simulación estricta con `@Mock` de dependencias (repositorios, password encoder, JWT utility).
  * Inyección de la clase bajo prueba con `@InjectMocks`.
  * Patrón **Arrange - Act - Assert** (AAA) en cada prueba.
  * Cobertura de mínimo:
    - 1 caso de éxito normal.
    - Casos borde y de error (ej. credenciales inválidas, usuario inexistente, usuario desactivado, stock insuficiente, cruce de horario).

* **Capa Web (`ControllerTest`):**
  * Uso de `MockMvc` en modo standalone (`MockMvcBuilders.standaloneSetup()`) configurado con `GlobalExceptionHandler`.
  * Validación de código de estado HTTP (`status().isOk()`, `isCreated()`, `isBadRequest()`, `isConflict()`, `isUnauthorized()`, `isForbidden()`).
  * Validación del cuerpo JSON mediante `jsonPath()`.

---

## 3. Manejo Estandarizado de Errores (`ApiErrorResponse`)

Toda excepción capturada debe devolver el DTO estándar `ApiErrorResponse` con la siguiente estructura:
```json
{
  "timestamp": "2026-10-25T15:00:00",
  "status": 401,
  "error": "Unauthorized",
  "mensaje": "Usuario o contraseña incorrectos",
  "path": "/api/auth/login"
}
```
**Regla de seguridad (RNF-03):** Jamás retornar trazas de excepción de base de datos (`SQLException`, `PSQLException`, etc.) al cliente para evitar fugas de información interna.
