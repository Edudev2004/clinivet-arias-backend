# Reglas de Negocio Oficiales de Clinivet Arias (APF2)

Este archivo formaliza las **Reglas de Negocio (RN)** y restricciones del sistema definidas en el documento oficial **`Grupo4-DWI-APF2.docx`** (Capítulo 3: SRS, Capítulo 4: BPMN y Capítulo 5: Base de Datos).
**Cualquier desarrollo en el backend DEBE cumplir estrictamente estas reglas.**

---

## 1. Catálogo de Reglas de Negocio (RN-01 a RN-12)

| Código | Regla de Negocio | Implementación en Backend | Código HTTP en Error |
| :--- | :--- | :--- | :---: |
| **RN-01** | **Asociación obligatoria de mascota:** Toda mascota debe estar asociada obligatoriamente a un propietario registrado en la base de datos. | Validar existencia de `propietario_id` antes de crear o actualizar mascota. FK `NOT NULL`. | `400 Bad Request` |
| **RN-02** | **Asociación obligatoria de cita:** Toda cita debe estar asociada a una mascota y a un veterinario específico. | `mascota_id` y `veterinario_id` son requeridos y deben existir en la base de datos. | `400 Bad Request` |
| **RN-03** | **Prevención de cruces de horario:** Un veterinario no puede tener dos citas en la misma fecha y hora. | Validar en servicio de citas y respaldar con el índice único `uq_cita_vet_fecha WHERE estado <> 'cancelada'`. | `409 Conflict` |
| **RN-04** | **Restricción de rol para atención clínica:** Solo usuarios con rol `VETERINARIO` pueden registrar diagnósticos, emitir recetas médicas (tratamientos) o aplicar vacunas. | Control de acceso en la capa de seguridad con `@PreAuthorize("hasRole('VETERINARIO')")` o verificación del token JWT. | `403 Forbidden` |
| **RN-05** | **Prohibición de stock negativo:** No se puede registrar una salida de inventario superior a la cantidad disponible del lote. | Validar en `IInventarioService`: `cantidad_salida <= lote.cantidadActual`. Respaldado por `CHECK (cantidad_actual >= 0)`. | `400 Bad Request` |
| **RN-06** | **Alerta automática de stock bajo:** Los productos con stock total igual o menor al stock mínimo definido activan una notificación interna de reposición. | Al persistir una salida o consumo, evaluar si `stockTotal <= stockMinimo` y disparar notificación interna de tipo `stock_bajo`. | Notificación en BD |
| **RN-07** | **Borrado lógico de citas e historial:** Las citas canceladas se conservan con estado `cancelada` y liberan el horario para agendar; jamás se aplica un DELETE físico. | Operación de cancelación actualiza `estado = 'cancelada'`, preservando la auditoría del registro. | `200 OK` |
| **RN-08** | **Responsable de movimiento de inventario:** Cada movimiento en el Kardex (entrada, salida, ajuste, vencido) debe registrar obligatoriamente al usuario que ejecutó la operación. | Asignar `usuario_id` del usuario autenticado en la entidad `MovimientoInventario`. | `400 Bad Request` |
| **RN-09** | **Inmutabilidad de la historia clínica:** La información histórica de diagnósticos, consultas y tratamientos se conserva para garantizar trazabilidad médica legal (Ley N.° 29733). | No permitir edición retroactiva ni eliminación física de consultas registradas. | `400 / 403` |
| **RN-10** | **Dispensación PEPS / FIFO:** La salida de medicamentos e insumos debe priorizar siempre los lotes activos con fecha de vencimiento más próxima. | Consultar lotes con `cantidad_actual > 0` ordenados por `fecha_vencimiento ASC` (índice `ix_lotes_fifo`). | `200 OK` |
| **RN-11** | **Validación contra horarios de disponibilidad:** Una cita solo puede registrarse dentro de los días y franjas horarias configuradas en `horarios_disponibilidad` del veterinario. | Validar que el día de semana (0-6) y hora_inicio/fin de la cita encajen en el turno del especialista. | `400 Bad Request` |
| **RN-12** | **Cálculo automático de dosis:** La dosis total del tratamiento se calcula matemáticamente: `DosisTotal (mg) = Peso (kg) * Dosis (mg/kg)`. | Calcular y redondear a 2 decimales en backend y almacenar en `tratamientos.dosis_total_mg`. | Automático |

---

## 2. Tipos y Estados de Negocio (ENUMs)

1. **Estado de Cita:**
   - `programada`: Cita agendada pendiente de atención.
   - `atendida`: Cita completada con consulta clínica registrada.
   - `cancelada`: Cita anulada (libera el horario del veterinario).
2. **Tipo de Movimiento de Inventario:**
   - `entrada`: Ingreso de nuevo lote o reposición de stock.
   - `salida`: Dispensación en consulta médica, venta o merma.
3. **Tipo de Notificación:**
   - `cita_creada`: Aviso de nueva cita agendada para el veterinario o recepción.
   - `stock_bajo`: Alerta automática cuando el inventario cae al stock mínimo.
   - `proximo_vencimiento`: Alerta preventiva sobre lotes cercanos a caducar.
