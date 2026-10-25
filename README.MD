# Clinivet Arias — Backend

> Sistema de gestión veterinaria · Backend Java/Spring Boot  
> Curso: Desarrollo de Software · UTP · Grupo 5

---

## Índice

1. [Descripción del proyecto](#1-descripción-del-proyecto)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura de 3 capas y SOLID](#3-arquitectura-de-3-capas-y-solid)
4. [Estructura del repositorio](#4-estructura-del-repositorio)
5. [Módulos y endpoints principales](#5-módulos-y-endpoints-principales)
6. [Guía de instalación y configuración de la BD](#6-guía-de-instalación-y-configuración-de-la-bd)
7. [Flujo de trabajo Git (Gitflow)](#7-flujo-de-trabajo-git-gitflow)
8. [Gestión de tareas con GitHub Projects](#8-gestión-de-tareas-con-github-projects)
9. [Equivalencias framework vs. conceptos del curso](#9-equivalencias-framework-vs-conceptos-del-curso)

---

## 1. Descripción del proyecto

**Clinivet Arias** es un aplicativo web para la gestión integral de una clínica veterinaria. Permite administrar propietarios, mascotas, citas, consultas, historial de vacunación, inventario de medicamentos y notificaciones.

**Problema que resuelve:** La clínica opera de forma manual (cuadernos y hojas de cálculo), lo que genera pérdida de historial clínico, doble agendamiento de citas y descontrol de stock de medicamentos.

**Alcance del sistema:**

- Autenticación segura con JWT y control de roles (Administrador, Veterinario, Recepcionista)
- Registro y gestión de propietarios y mascotas
- Agendamiento de citas con validación de disponibilidad del veterinario (RN-03)
- Registro de consultas con diagnóstico y tratamiento
- Historial de vacunación por mascota
- Control de inventario por lotes con alertas de stock bajo y vencimiento
- Notificaciones internas al personal
- Exportación de reportes en Excel (Apache POI)

---

## 2. Stack tecnológico

| Capa                 | Tecnología                          |
| -------------------- | ----------------------------------- |
| Frontend             | Angular 17+ + Tailwind CSS          |
| Backend              | Java 21 + Spring Boot 3.x           |
| Base de datos        | PostgreSQL 15 (alojado en Supabase) |
| Autenticación        | JWT (Spring Security + JJWT 0.12)   |
| Despliegue frontend  | Vercel                              |
| Despliegue backend   | Render / Azure App Service          |
| Control de versiones | Git + GitHub                        |
| Gestión de tareas    | GitHub Projects (Kanban)            |
| Documentación API    | Swagger / OpenAPI 3.0 (Springdoc)   |

**Librerías obligatorias del curso:**

| Librería           | Versión   | Uso en el proyecto                                              |
| ------------------ | --------- | --------------------------------------------------------------- |
| Google Guava       | 33.2.1    | `Preconditions` en servicios, colecciones inmutables            |
| Apache Commons Lang3 | 3.14.0 | `StringUtils` para sanitizar inputs en la capa de servicio      |
| Apache POI         | 5.3.0     | Generación de reportes `.xlsx` (citas, pacientes)               |
| Logback Classic    | (Spring)  | Logs estructurados, rotación de archivos, enmascaramiento de datos sensibles |

---

## 3. Arquitectura de 3 capas y SOLID

El sistema sigue el patrón de **arquitectura de 3 capas** (Three-Tier Architecture), separando responsabilidades entre presentación, lógica de negocio y datos.

```
┌─────────────────────────────────────────────┐
│             CAPA DE PRESENTACIÓN            │
│         Angular 17+ + Tailwind CSS          │
│  - Arquitectura Feature-Driven (por módulos)│
│  - Componentes Standalone / UI reutilizable │
│  - Estado reactivo (Signals / RxJS)         │
│  - Consumo de API REST vía HttpClient       │
│                  Vercel                     │
└────────────────────┬────────────────────────┘
                     │ HTTP / JSON
                     ▼
┌─────────────────────────────────────────────┐
│           CAPA DE LÓGICA DE NEGOCIO         │
│         Java 21 + Spring Boot 3.x           │
│  - Controllers  → reciben peticiones HTTP   │
│  - Services     → reglas de negocio         │
│  - Repositories → acceso a datos (JPA/DAO)  │
│  - DTOs / Mappers                           │
│  - Validaciones (RN-01 a RN-12)             │
│  - JWT Security (Spring Security)           │
│           Render / Azure                    │
└────────────────────┬────────────────────────┘
                     │ JDBC / JPA
                     ▼
┌─────────────────────────────────────────────┐
│              CAPA DE DATOS                  │
│         PostgreSQL 15 en Supabase           │
│  - 15 tablas relacionales                   │
│  - Triggers para auditoría                  │
│  - Funciones para cálculo de dosis          │
│  - Row Level Security (RLS)                 │
│  - Enums: estado_cita, tipo_movimiento,     │
│    tipo_notificacion                        │
└─────────────────────────────────────────────┘
```

### Principios SOLID aplicados

| Principio | Dónde se aplica |
| --------- | --------------- |
| **S** — Single Responsibility | Cada clase tiene una única razón para cambiar: el `Controller` solo orquesta HTTP, el `Service` solo ejecuta reglas de negocio, el `Repository` solo persiste datos. |
| **O** — Open/Closed | Los servicios se extienden mediante nuevas implementaciones de interfaz, sin modificar código existente (ej. `ExportadorService` → implementaciones Excel/PDF). |
| **L** — Liskov Substitution | Las implementaciones de servicio y repositorio son sustituibles por sus interfaces sin alterar el comportamiento del sistema. |
| **I** — Interface Segregation | Interfaces específicas por dominio (`IAuthService`, `ICitaService`) en lugar de una mega-interfaz. |
| **D** — Dependency Inversion | Los `Controllers` dependen de interfaces `IXxxService`; los `Services` dependen de interfaces `JpaRepository`. Inyección por constructor en todas las capas. |

---

## 4. Estructura del repositorio

```
clinivet-arias-backend/
├── backend/                              ← Proyecto Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/clinivet/arias/
│   │   │   │   ├── ClinivetAriasApplication.java  ← Punto de entrada
│   │   │   │   ├── controller/           ← @RestController (MVC-C)
│   │   │   │   ├── service/              ← Interfaces + @Service (lógica de negocio)
│   │   │   │   ├── repository/           ← JpaRepository (DAO)
│   │   │   │   ├── model/                ← @Entity JPA (MVC-M)
│   │   │   │   ├── dto/                  ← Request/Response DTOs
│   │   │   │   ├── security/             ← JWT + Spring Security
│   │   │   │   └── config/               ← SecurityConfig, OpenApiConfig
│   │   │   └── resources/
│   │   │       ├── application-example.yml  ← Plantilla sin secretos ✓
│   │   │       └── logback-spring.xml       ← Configuración de logs
│   │   └── test/java/com/clinivet/arias/    ← Tests JUnit 5 + Mockito
│   └── pom.xml
│
├── frontend/                             ← Proyecto Angular (próximo sprint)
├── database/                             ← DDL SQL, seeds, migrations
├── .gitignore
└── README.md
```

> **Nota de seguridad:** `application.yml` (con credenciales reales) está en `.gitignore` y nunca se sube al repositorio. Usar `application-example.yml` como plantilla.

---

## 5. Módulos y endpoints principales

### 5.1 Autenticación

| Método | Endpoint           | Descripción                   | Roles       |
| ------ | ------------------ | ----------------------------- | ----------- |
| `POST` | `/api/auth/login`  | Login → retorna JWT + rol     | Público     |
| `POST` | `/api/auth/logout` | Invalida sesión               | Autenticado |

### 5.2 Propietarios

| Método | Endpoint                 | Descripción                           | Roles       |
| ------ | ------------------------ | ------------------------------------- | ----------- |
| `GET`  | `/api/propietarios`      | Listar propietarios activos           | ADMIN, RECEPCIONISTA |
| `GET`  | `/api/propietarios/{id}` | Obtener propietario por ID            | ADMIN, RECEPCIONISTA |
| `POST` | `/api/propietarios`      | Registrar nuevo propietario           | RECEPCIONISTA |
| `PUT`  | `/api/propietarios/{id}` | Actualizar datos del propietario      | RECEPCIONISTA |

### 5.3 Mascotas

| Método | Endpoint                       | Descripción                | Roles        |
| ------ | ------------------------------ | -------------------------- | ------------ |
| `GET`  | `/api/mascotas`                | Listar mascotas            | Autenticado  |
| `GET`  | `/api/mascotas/{id}`           | Detalle de mascota         | Autenticado  |
| `GET`  | `/api/mascotas/{id}/historial` | Historial clínico completo | Autenticado  |
| `POST` | `/api/mascotas`                | Registrar mascota          | RECEPCIONISTA |
| `PUT`  | `/api/mascotas/{id}`           | Actualizar datos           | RECEPCIONISTA |

### 5.4 Citas

| Método  | Endpoint                 | Descripción                                | Roles       |
| ------- | ------------------------ | ------------------------------------------ | ----------- |
| `GET`   | `/api/citas`             | Listar citas (filtros: fecha, estado, vet) | Autenticado |
| `GET`   | `/api/citas/{id}`        | Detalle de cita                            | Autenticado |
| `POST`  | `/api/citas`             | Agendar nueva cita                         | RECEPCIONISTA |
| `PATCH` | `/api/citas/{id}/estado` | Cambiar estado (atendida / cancelada)      | VETERINARIO |

> **RN-03:** No se pueden registrar dos citas para el mismo veterinario en la misma fecha/hora.

### 5.5 Consultas

| Método | Endpoint                           | Descripción                               |
| ------ | ---------------------------------- | ----------------------------------------- |
| `GET`  | `/api/consultas/{id}`              | Detalle de consulta                       |
| `POST` | `/api/consultas`                   | Registrar consulta (vinculada a una cita) |
| `GET`  | `/api/consultas/{id}/tratamientos` | Listar tratamientos de la consulta        |
| `POST` | `/api/consultas/{id}/tratamientos` | Agregar tratamiento                       |

### 5.6 Inventario

| Método | Endpoint                       | Descripción                                     |
| ------ | ------------------------------ | ----------------------------------------------- |
| `GET`  | `/api/medicamentos`            | Listar medicamentos                             |
| `GET`  | `/api/medicamentos/{id}/lotes` | Ver lotes de un medicamento                     |
| `POST` | `/api/medicamentos/{id}/lotes` | Registrar nuevo lote                            |
| `POST` | `/api/inventario/movimientos`  | Registrar entrada o salida de stock             |
| `GET`  | `/api/inventario/alertas`      | Medicamentos con stock bajo o próximos a vencer |

### 5.7 Reportes (Apache POI)

| Método | Endpoint                   | Descripción                          |
| ------ | -------------------------- | ------------------------------------ |
| `GET`  | `/api/reportes/citas`      | Exportar reporte de citas en `.xlsx` |
| `GET`  | `/api/reportes/mascotas`   | Exportar listado de mascotas en `.xlsx` |

---

## 6. Guía de instalación y configuración de la BD

### 6.1 Prerrequisitos

- Java 21 (JDK)
- Maven 3.9+
- Cuenta en [Supabase](https://supabase.com) (gratuita)
- Git

### 6.2 Configurar la base de datos en Supabase

1. Crear un nuevo proyecto en Supabase.
2. Ir a **SQL Editor** dentro del proyecto.
3. Abrir el archivo `database/clinivet_schema.sql` y pegarlo en el editor.
4. Ejecutar el script. Crea las 15 tablas, los enums, los índices y las restricciones.
5. Ir a **Project Settings → Database** y copiar la **Connection String** (modo `URI`).

### 6.3 Configurar el backend

```bash
git clone https://github.com/Edudev2004/clinivet-arias-backend.git
cd clinivet-arias-backend/backend
```

Copiar la plantilla y completar los valores:

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# Editar application.yml con tus credenciales reales
```

Ejecutar:

```bash
./mvnw spring-boot:run
# o en Windows:
mvnw.cmd spring-boot:run
```

Verificar: `http://localhost:8080/swagger-ui/index.html`

### 6.4 Ejecutar los tests

```bash
cd backend
./mvnw test
```

Los tests usan H2 (base de datos en memoria), no necesitan conexión a Supabase.

---

## 7. Flujo de trabajo Git (Gitflow)

### 7.1 Estructura de ramas

| Rama                              | Propósito                                                                    |
| --------------------------------- | ---------------------------------------------------------------------------- |
| `main`                            | Código en producción. Solo recibe merges desde `release/` o `hotfix/`.       |
| `develop`                         | Rama de integración. Todas las feature branches terminan aquí.               |
| `feature/RF-XX-descripcion-corta` | Una rama por requerimiento funcional. Nace de `develop`.                     |
| `release/sprint-N`                | Se abre al cerrar cada sprint para estabilizar antes de pasar a `main`.      |
| `hotfix/descripcion`              | Solo para bugs urgentes en producción, sale de `main`.                       |

> **Importante:** Al mergear una feature, **NO borrar la rama**. Sirve de evidencia del trabajo realizado.

### 7.2 Flujo diario

```bash
# Antes de empezar un requerimiento
git checkout develop
git pull origin develop
git checkout -b feature/RF-01-endpoint-login

# Trabajar y commitear en español sin prefijos
git add .
git commit -m "Agregar test de login correcto con JWT"
git commit -m "Implementar endpoint POST /api/auth/login"

# Subir la rama y abrir Pull Request hacia develop
git push origin feature/RF-01-endpoint-login
```

---

## 8. Gestión de tareas con GitHub Projects

Tablero Kanban con columnas: **Backlog → Sprint actual → En progreso → En revisión → Completado**

Cada Issue sigue el formato:

```
Título: [RF-01] Endpoint POST /api/auth/login
Como usuario del sistema,
quiero autenticarme con usuario y contraseña,
para obtener un token JWT y acceder a las funciones del sistema.

Criterios de aceptación:
- [ ] Retorna JWT válido si las credenciales son correctas
- [ ] Retorna HTTP 401 con mensaje claro si la contraseña es incorrecta
- [ ] Retorna HTTP 401 con mensaje claro si el usuario no existe
- [ ] El log registra el intento fallido SIN la contraseña
```

---

## 9. Equivalencias framework vs. conceptos del curso

| Concepto del curso | Equivalente en Spring Boot | Justificación |
| ------------------ | -------------------------- | ------------- |
| **MVC** | `@RestController` (C) + `@Entity`/DTO (M) + Angular SPA (V desacoplada) | El patrón MVC se mantiene; la Vista es la SPA Angular que consume la API REST. |
| **DAO** | `@Repository` extendiendo `JpaRepository<T,ID>` | Spring Data JPA es la evolución estándar del patrón DAO. Abstrae completamente el acceso a datos. |
| **TDD** | JUnit 5 + Mockito + `@WebMvcTest` / `@SpringBootTest` | Tests escritos antes del código de producción (ciclo Red-Green-Refactor). |
| **Seguridad** | BCrypt + JWT + `@PreAuthorize` + Spring Security 6 | Autenticación stateless, roles granulares, logs sin datos sensibles. |
| **Librerías Java** | Guava, Commons Lang3, Apache POI, Logback | Integradas en la capa de servicio, reportes y configuración de logs. |

---

*Documentación del Avance de Proyecto Final — Grupo 5 · Desarrollo de Software · UTP*
