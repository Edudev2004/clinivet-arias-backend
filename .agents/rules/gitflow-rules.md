# Flujo de trabajo con Git (Gitflow) — Clinivet Arias

El equipo trabaja bajo el modelo Gitflow, adaptado a los Requerimientos Funcionales (códigos RF-XX) registrados en el backlog del repositorio. Esto garantiza trazabilidad directa entre los Issues de GitHub, las ramas y los Pull Requests.

---

## ⚠️ REGLA DE ORO OBLIGATORIA
> **IMPORTANTE: AL MERGEAR NO BORRAR LAS RAMAS FEATURE**
> Todas las ramas `feature/RF-XX-...` deben permanecer vivas tanto en el repositorio local como en el remoto (GitHub) como evidencia del trabajo ordenado para la evaluación del profesor.

---

## 1. Definición de Ramas

| Rama | Uso y Propósito |
| :--- | :--- |
| **`main`** | Código en producción. Solo recibe merges desde `release/` o `hotfix/`, nunca directo. |
| **`develop`** | Rama de integración. Todas las feature branches terminan aquí primero. |
| **`feature/RF-XX-descripción-corta`** | Una rama por requerimiento funcional o historia de usuario. Ej: `feature/RF-01-endpoint-login`, `feature/RF-13-agendar-cita`. Nace de `develop`. **NO BORRAR AL MERGEAR**. |
| **`release/sprint-N`** | Se abre al cerrar cada sprint para estabilizar antes de pasar a `main`. |
| **`hotfix/descripcion`** | Solo para arreglos urgentes directo sobre `main` (bugs en producción). |

---

## 2. Referencia Obligatoria del Esquema de BD

* **Esquema de Base de Datos:** Todo desarrollo en Backend y Frontend debe tomar estrictamente como referencia el esquema SQL definido en **`database/clinivet_schema.sql`** (16 tablas en Supabase).
* Todas las entidades JPA, nombres de campos, claves foráneas y tipos de datos deben alinearse a este archivo para garantizar consistencia total.

---

## 3. Reglas de Commits y Mensajes

* **Idioma:** Español.
* **Estilo:** Atómicos, claros y descriptivos en tiempo imperativo o presente (ej. *"Implementar autenticación con JWT y BCrypt en AuthService"*).
* **SIN prefijos:** **NO usar** prefijos como `feat:`, `fix:`, `chore:`, `refactor:`.
* **Cierre automático:** En el Pull Request o commit final de la rama se puede incluir `Closes #N` para cerrar el issue en GitHub automáticamente.

---

## 4. Flujo de Trabajo Diario, Paso a Paso

1. Antes de empezar un requerimiento, actualiza tu `develop` local: 
   ```bash
   git checkout develop && git pull origin develop
   ```
2. Crea tu rama desde `develop` con el formato definido: 
   ```bash
   git checkout -b feature/RF-01-endpoint-login
   ```
3. Trabaja y commitea en tu rama con mensajes atómicos en español:
   ```bash
   git add .
   git commit -m "Crear entidad Usuario y repositorio JpaRepository"
   git commit -m "Implementar endpoint POST /api/auth/login con generador de token JWT"
   ```
4. Al terminar, ejecuta la suite de pruebas unitarias (**Quality Gate TDD obligatorio**):
   ```powershell
   .\mvnw.cmd test
   ```
   **Regla estricta:** Ningún commit ni PR puede subirse o mergearse si hay pruebas fallidas o si falta la suite de pruebas (JUnit 5 + Mockito / MockMvc) del RF desarrollado.
5. Sube tu rama al repositorio remoto: 
   ```bash
   git push origin feature/RF-XX-descripcion-corta
   ```
6. Abre un **Pull Request hacia `develop`** (base: `develop` <- compare: `feature/RF-XX-...`), referenciando el Issue (`Closes #N`) y adjuntando evidencia de pruebas unitarias exitosas (JUnit 5 / Mockito / MockMvc).
7. Al menos un compañero revisa y aprueba el PR antes de mergear. **Recordar:** NO marcar "Delete branch" al mergear.
8. Al cerrar un sprint completo, se abre `release/sprint-N` desde `develop` para pruebas finales, y luego se mergea a `main` con su respectiva etiqueta de versión (ej. `v1.0-sprint-1`).
