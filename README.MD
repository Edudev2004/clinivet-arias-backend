# Clinivet Arias — Documentación Técnica

> Sistema de gestión veterinaria desarrollado con arquitectura de 3 capas.  
> Curso: Desarrollo de Software · UTP · Grupo 5

---

## Índice

1. [Descripción del proyecto](#1-descripción-del-proyecto)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura de 3 capas](#3-arquitectura-de-3-capas)
4. [Estructura del repositorio](#4-estructura-del-repositorio)
5. [Módulos y endpoints principales](#5-módulos-y-endpoints-principales)
6. [Guía de instalación y configuración de la BD](#6-guía-de-instalación-y-configuración-de-la-bd)
7. [Flujo de trabajo Git (Gitflow)](#7-flujo-de-trabajo-git-gitflow)
8. [Gestión de tareas con GitHub Projects](#8-gestión-de-tareas-con-github-projects)

---

## 1. Descripción del proyecto

**Clinivet Arias** es un aplicativo web para la gestión integral de una clínica veterinaria. Permite administrar propietarios, mascotas, citas, consultas, historial de vacunación, inventario de medicamentos y notificaciones.

**Problema que resuelve:** La clínica opera de forma manual (cuadernos y hojas de cálculo), lo que genera pérdida de historial clínico, doble agendamiento de citas y descontrol de stock de medicamentos.

**Alcance del sistema:**

- Registro y gestión de propietarios y mascotas
- Agendamiento de citas con validación de disponibilidad del veterinario
- Registro de consultas con diagnóstico y tratamiento
- Historial de vacunación por mascota
- Control de inventario por lotes con alertas de stock bajo y vencimiento
- Notificaciones internas al personal

---

## 2. Stack tecnológico

| Capa                 | Tecnología                          |
| -------------------- | ----------------------------------- |
| Frontend             | Angular 17+ + Tailwind CSS          |
| Backend              | Java 21 + Spring Boot 3.x           |
| Base de datos        | PostgreSQL 15 (alojado en Supabase) |
| Autenticación        | JWT (Spring Security)               |
| Despliegue frontend  | Vercel                              |
| Despliegue backend   | Render / Azure App Service          |
| Control de versiones | Git + GitHub                        |
| Gestión de tareas    | GitHub Projects (Kanban)            |
| Documentación API    | Swagger / OpenAPI 3.0               |

---

## 3. Arquitectura de 3 capas

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
│  - Repositories → acceso a datos (JPA)      │
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

### Flujo de una petición (ejemplo: agendar cita)

```
Usuario (Angular)
  → POST /api/citas
    → CitaController.crearCita()
      → CitaService.validarDisponibilidad()   ← RN-03: no doble cita mismo vet
        → CitaRepository.save()
          → PostgreSQL tabla `citas`
            ← Respuesta 201 Created (CitaDTO)
```

### Paquetes del backend (Spring Boot)

```
com.clinivet.arias
├── controller/       ← Endpoints REST (@RestController)
│   ├── CitaController.java
│   ├── ConsultaController.java
│   ├── MascotaController.java
│   ├── InventarioController.java
│   └── UsuarioController.java
├── service/          ← Lógica de negocio (@Service)
│   ├── CitaService.java
│   ├── ConsultaService.java
│   └── InventarioService.java
├── repository/       ← Acceso a BD (@Repository, JPA)
│   ├── CitaRepository.java
│   └── MascotaRepository.java
├── model/            ← Entidades JPA (@Entity)
│   ├── Cita.java
│   ├── Mascota.java
│   └── ...
├── dto/              ← Objetos de transferencia
├── security/         ← JWT + Spring Security
└── config/           ← Configuración CORS, Beans
```

---

## 4. Estructura del repositorio

```
clinivet-arias/
├── frontend/                  ← Proyecto Angular (Feature-Driven)
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/          ← Servicios globales, interceptores HTTP, guards
│   │   │   ├── shared/        ← Componentes UI reutilizables, pipes, directivas
│   │   │   └── features/      ← Modular por características de negocio
│   │   │       ├── auth/
│   │   │       ├── citas/
│   │   │       │   ├── components/
│   │   │       │   ├── pages/
│   │   │       │   └── services/
│   │   │       ├── consultas/
│   │   │       ├── mascotas/
│   │   │       └── inventario/
│   │   ├── environments/      ← Configuración de variables de entorno
│   │   ├── main.ts
│   │   └── styles.css
│   ├── angular.json
│   ├── package.json
│   └── tailwind.config.js
│
├── backend/                   ← Proyecto Spring Boot
│   ├── src/main/java/com/clinivet/arias/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   └── security/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── database/
│   ├── clinivet_schema.sql    ← DDL completo (tablas, enums, índices)
│   ├── seed_data.sql          ← Datos de prueba
│   └── migrations/            ← Cambios incrementales
│
└── README.md
```

---

## 5. Módulos y endpoints principales

### 5.1 Autenticación

| Método | Endpoint           | Descripción                   |
| ------ | ------------------ | ----------------------------- |
| `POST` | `/api/auth/login`  | Login de usuario, retorna JWT |
| `POST` | `/api/auth/logout` | Invalida sesión               |

### 5.2 Propietarios

| Método | Endpoint                 | Descripción                           |
| ------ | ------------------------ | ------------------------------------- |
| `GET`  | `/api/propietarios`      | Listar todos los propietarios activos |
| `GET`  | `/api/propietarios/{id}` | Obtener propietario por ID            |
| `POST` | `/api/propietarios`      | Registrar nuevo propietario           |
| `PUT`  | `/api/propietarios/{id}` | Actualizar datos del propietario      |

### 5.3 Mascotas

| Método | Endpoint                       | Descripción                |
| ------ | ------------------------------ | -------------------------- |
| `GET`  | `/api/mascotas`                | Listar mascotas            |
| `GET`  | `/api/mascotas/{id}`           | Detalle de mascota         |
| `GET`  | `/api/mascotas/{id}/historial` | Historial clínico completo |
| `POST` | `/api/mascotas`                | Registrar mascota          |
| `PUT`  | `/api/mascotas/{id}`           | Actualizar datos           |

### 5.4 Citas

| Método  | Endpoint                 | Descripción                                |
| ------- | ------------------------ | ------------------------------------------ |
| `GET`   | `/api/citas`             | Listar citas (filtros: fecha, estado, vet) |
| `GET`   | `/api/citas/{id}`        | Detalle de cita                            |
| `POST`  | `/api/citas`             | Agendar nueva cita                         |
| `PATCH` | `/api/citas/{id}/estado` | Cambiar estado (atendida / cancelada)      |

> **RN-03:** No se pueden registrar dos citas para el mismo veterinario en la misma fecha/hora. El backend valida con el índice único `(veterinario_id, fecha_hora)`.

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

### 5.7 Vacunas

| Método | Endpoint                     | Descripción                            |
| ------ | ---------------------------- | -------------------------------------- |
| `GET`  | `/api/vacunas/catalogo`      | Listar catálogo de vacunas             |
| `POST` | `/api/vacunas/aplicadas`     | Registrar vacuna aplicada              |
| `GET`  | `/api/mascotas/{id}/vacunas` | Historial de vacunación de una mascota |

### 5.8 Notificaciones

| Método  | Endpoint                        | Descripción                                   |
| ------- | ------------------------------- | --------------------------------------------- |
| `GET`   | `/api/notificaciones`           | Listar notificaciones del usuario autenticado |
| `PATCH` | `/api/notificaciones/{id}/leer` | Marcar como leída                             |

---

## 6. Guía de instalación y configuración de la BD

### 6.1 Prerrequisitos

- Java 21 (JDK)
- Node.js 20+
- Cuenta en [Supabase](https://supabase.com) (gratuita)
- Git

### 6.2 Configurar la base de datos en Supabase

1. Crear un nuevo proyecto en Supabase.
2. Ir a **SQL Editor** dentro del proyecto.
3. Abrir el archivo `database/clinivet_schema.sql` del repositorio y pegarlo en el editor.
4. Ejecutar el script. Esto crea las 15 tablas, los enums, los índices y las restricciones.
5. Ir a **Project Settings → Database** y copiar la **Connection String** (modo `URI`).

```
postgresql://postgres:[PASSWORD]@db.[REF].supabase.co:5432/postgres
```

### 6.3 Configurar el backend (Spring Boot)

1. Clonar el repositorio:

```bash
git clone https://github.com/[org]/clinivet-arias.git
cd clinivet-arias/backend
```

2. Crear el archivo `src/main/resources/application.yml` (no se sube al repo, está en `.gitignore`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.[REF].supabase.co:5432/postgres
    username: postgres
    password: [TU_PASSWORD]
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: [CLAVE_SECRETA_MINIMO_256_BITS]
  expiration-ms: 86400000

server:
  port: 8080
```

3. Ejecutar el backend:

```bash
./mvnw spring-boot:run
```

4. Verificar que el servidor esté corriendo: `http://localhost:8080/api/auth/login`

### 6.4 Configurar el frontend (Angular)

1. Instalar dependencias:

```bash
cd ../frontend
npm install
```

2. Configurar la URL de la API en `src/environments/environment.development.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

3. Ejecutar en desarrollo:

```bash
ng serve
# o alternativamente: npm start
```

La aplicación estará disponible en `http://localhost:4200`.

### 6.5 Datos de prueba

Para cargar datos iniciales de prueba (roles, especialidades, un usuario administrador):

```bash
# En el SQL Editor de Supabase, ejecutar:
database/seed_data.sql
```

Usuario de prueba:

- **Email:** `admin@clinivet.pe`
- **Password:** `Admin123!`

---

## 7. Flujo de trabajo Git (Gitflow)

### 7.1 Estructura de ramas

| Rama                              | Propósito                                                                                      |
| --------------------------------- | ---------------------------------------------------------------------------------------------- |
| `main`                            | Código en producción. Solo recibe merges desde `release/` o `hotfix/`. Nunca commits directos. |
| `develop`                         | Rama de integración. Todas las feature branches terminan aquí.                                 |
| `feature/US-XX-descripcion-corta` | Una rama por historia de usuario. Nace de `develop`.                                           |
| `release/sprint-N`                | Se abre al cerrar cada sprint para estabilizar antes de pasar a `main`.                        |
| `hotfix/descripcion`              | Solo para bugs urgentes en producción, sale de `main`.                                         |

> **Importante:** Al mergear una feature, **NO borrar la rama**. Sirve de evidencia del trabajo realizado.

### 7.2 Diagrama del flujo

```
main       ──────────────────────────────────────●─────────────────●──
                                                ↑                 ↑
release    ───────────────────────────────── sprint-1 ──     sprint-2
                                               ↑                ↑
develop    ──●───────────────────────────────●──────────────────●────
             ↑     ↑           ↑           ↑
feature       US-01  US-03      US-07       US-14
```

### 7.3 Flujo diario paso a paso

**Antes de empezar una historia de usuario:**

```bash
git checkout develop
git pull origin develop
```

**Crear la rama de la historia:**

```bash
git checkout -b feature/US-14-agendar-cita
```

**Trabajar y commitear con mensajes descriptivos en español:**

```bash
git add .
git commit -m "Agregar validación de disponibilidad del veterinario en agendamiento"
git commit -m "Implementar endpoint POST /api/citas con respuesta 201"
git commit -m "Agregar manejo de error 409 cuando existe conflicto de horario"
```

> Los mensajes deben ser atómicos y en español. Sin prefijos como `feat:`, `fix:` o `chore:`.

**Subir la rama y abrir Pull Request:**

```bash
git push origin feature/US-14-agendar-cita
```

Luego en GitHub: **New Pull Request → base: `develop` ← compare: `feature/US-14-agendar-cita`**

El PR debe describir:

- Qué historia de usuario resuelve
- Qué cambios incluye
- Cómo probar

**Revisión:** Al menos 1 compañero revisa y aprueba antes de mergear.

**Al cerrar un sprint:**

```bash
git checkout develop
git pull origin develop
git checkout -b release/sprint-1
# Pruebas finales, correcciones menores
git checkout main
git merge release/sprint-1
git tag v1.0-sprint-1
git push origin main --tags
```

### 7.4 Convención de nombres de ramas

```
feature/US-01-registro-propietario
feature/US-03-registro-mascota
feature/US-07-historial-vacunacion
feature/US-14-agendar-cita
feature/US-22-control-inventario
release/sprint-1
release/sprint-2
hotfix/error-login-produccion
```

---

## 8. Gestión de tareas con GitHub Projects

### 8.1 Tablero Kanban

El proyecto usa **GitHub Projects** en modo Kanban con las siguientes columnas:

| Columna           | Descripción                                      |
| ----------------- | ------------------------------------------------ |
| **Backlog**       | Historias de usuario pendientes de planificación |
| **Sprint actual** | Issues asignados al sprint en curso              |
| **En progreso**   | Issue con rama activa y desarrollador asignado   |
| **En revisión**   | Pull Request abierto, esperando aprobación       |
| **Completado**    | PR mergeado a `develop`, issue cerrado           |

### 8.2 Estructura de un Issue

Cada historia de usuario se crea como un **Issue** en GitHub con el siguiente formato:

```
Título: [US-14] Agendar cita con veterinario

Como propietario,
quiero agendar una cita para mi mascota con un veterinario disponible,
para que sea atendida en el horario que me convenga.

Criterios de aceptación:
- [ ] El sistema muestra los veterinarios disponibles por fecha
- [ ] No permite agendar si el vet ya tiene cita en ese horario (RN-03)
- [ ] Genera notificación interna al crear la cita
- [ ] Retorna error 409 si hay conflicto de horario

Labels: enhancement · sprint-1 · backend · frontend
Assignees: @US-14-responsable
```

### 8.3 Relación Issue → Rama → PR

```
Issue #14 "Agendar cita"
    └── Rama: feature/US-14-agendar-cita
            └── Pull Request: "US-14: Implementar agendamiento de citas"
                    └── Merge a develop → Issue #14 se cierra automáticamente
```

Para que el issue se cierre automáticamente al mergear, incluir en la descripción del PR:

```
Closes #14
```

---

_Documentación generada para el Avance de Proyecto Final 2 — Grupo 5 · DWI · UTP_
