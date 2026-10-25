# Convenciones de Base de Datos y Persistencia JPA (APF2)

Este archivo establece las directrices de base de datos relacional y mapeo objeto-relacional (ORM / JPA) para mantener el esquema 100% alineado con el **Capítulo 5 y Capítulo 6 de `Grupo4-DWI-APF2.docx`**.

---

## 1. Identificadores Primarios (UUID Obligatorio)

* **Todas las 16 entidades utilizan `UUID` como clave primaria:**
  ```sql
  id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  ```
* En Java / JPA:
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;
  ```
* **Prohibido:** Usar identificadores numéricos incrementales (`Long`, `Integer`, `BIGSERIAL`, `GenerationType.IDENTITY`).

---

## 2. Las 16 Tablas y Entidades del Modelo de Dominio

| N.° | Tabla en PostgreSQL | Entidad JPA en Java | Área de Dominio | Responsabilidad Principal |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `roles` | `Rol` | Seguridad | Perfiles de acceso (`ADMINISTRADOR`, `VETERINARIO`, `RECEPCIONISTA`, `ENCARGADO_INVENTARIO`). |
| **2** | `usuarios` | `Usuario` | Seguridad | Personal del sistema, credenciales con hash BCrypt y token JWT. |
| **3** | `especialidades` | `Especialidad` | Personal Médico | Especialidades clínicas (Dermatología, Traumatología, etc.). |
| **4** | `veterinarios` | `Veterinario` | Personal Médico | Médico veterinario colegiado, vinculado 1 a 1 con su `Usuario`. |
| **5** | `horarios_disponibilidad`| `HorarioDisponibilidad` | Personal Médico | Franjas de atención semanal por día (`0..6`) y horas inicio/fin. |
| **6** | `notificaciones` | `Notificacion` | Seguridad | Avisos internos para usuarios (`cita_creada`, `stock_bajo`, `proximo_vencimiento`). |
| **7** | `propietarios` | `Propietario` | Pacientes | Clientes dueños de mascotas (DNI único, teléfono, email, dirección). |
| **8** | `mascotas` | `Mascota` | Pacientes | Ficha del paciente vinculada a un propietario (RN-01). |
| **9** | `citas` | `Cita` | Atención Clínica | Reserva médica entre mascota y veterinario con estado. |
| **10**| `consultas` | `Consulta` | Atención Clínica | Acto médico: anamnesis, peso, temperatura, diagnóstico y observaciones. |
| **11**| `tratamientos` | `Tratamiento` | Atención Clínica | Receta médica prescrita: medicamento, dosis (mg/kg), dosis total y duración. |
| **12**| `vacunas_catalogo` | `VacunaCatalogo` | Vacunación | Catálogo maestro de vacunas disponibles según especie. |
| **13**| `vacunas_aplicadas` | `VacunaAplicada` | Vacunación | Registro histórico de vacunación del paciente con fecha y próxima dosis. |
| **14**| `medicamentos` | `Medicamento` | Inventario | Catálogo de productos médicos con stock mínimo de reposición. |
| **15**| `lotes_medicamento` | `LoteMedicamento` | Inventario | Control de stock por lote, fecha de vencimiento y saldo (RN-05). |
| **16**| `movimientos_inventario` | `MovimientoInventario` | Inventario | Kardex de auditoría: registro de toda entrada o salida de stock (RN-08). |

---

## 3. Desnormalizaciones Controladas Permitidas (APF2 5.2)

Solo se permiten las 3 siguientes desnormalizaciones explícitamente autorizadas en el informe por motivos de rendimiento y trazabilidad:
1. `mascotas.peso_actual_kg`: Almacena el último peso del paciente para consulta ágil en expedientes.
2. `tratamientos.dosis_total_mg`: Almacena el producto calculado de $\text{peso} \times \text{dosis}$ (RF-23).
3. `lotes_medicamento.cantidad_actual`: Almacena el saldo remanente del lote para no recalcular todo el histórico del Kardex en cada lectura.

**Cualquier otra columna duplicada o calculada está terminantemente prohibida** para garantizar el cumplimiento de la Tercera Forma Normal (3FN).

---

## 4. Convenciones de Mapeo JPA y Auditoría

* **Nombres de columnas en snake_case:** Todas las columnas en PostgreSQL deben seguir `snake_case` (ej. `password_hash`, `fecha_nacimiento`, `stock_minimo`, `propietario_id`).
* **Atributos Java en camelCase:** (`passwordHash`, `fechaNacimiento`, `stockMinimo`).
* **Auditoría temporal:** Tablas transaccionales deben poseer `created_at` y `updated_at` de tipo `TIMESTAMPTZ` mapeados con `LocalDateTime` y anotados con `@PrePersist` / `@PreUpdate`.
