-- ============================================================
-- Clinivet Arias — Datos Iniciales Normalizados (Seed Data)
-- ============================================================

-- 1. Roles del Personal
INSERT INTO roles (id_rol, nombre, descripcion)
VALUES 
  (1, 'ADMIN', 'Administrador con acceso total al sistema y reportes'),
  (2, 'VETERINARIO', 'Médico veterinario: atención clínica, recetas y vacunas'),
  (3, 'RECEPCIONISTA', 'Personal de recepción: registro de propietarios, mascotas y citas')
ON CONFLICT (nombre) DO NOTHING;

-- 2. Usuarios del Personal
-- Password inicial: Admin123! (hash BCrypt)
INSERT INTO usuarios (id_usuario, id_rol, nombres, apellidos, email, password_hash, telefono, rol, activo)
VALUES 
  (1, 1, 'Rommel', 'Navarro', 'admin@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654321', 'ADMIN', true),
  (2, 2, 'Beckhan', 'Elias', 'veterinario@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654322', 'VETERINARIO', true),
  (3, 3, 'Lucero', 'Perez', 'recepcion@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654323', 'RECEPCIONISTA', true)
ON CONFLICT (email) DO NOTHING;

-- 3. Especialidades Veterinarias
INSERT INTO especialidades (id_especialidad, nombre, descripcion)
VALUES
  (1, 'Medicina General y Cirugía Menor', 'Consultas preventivas, esterilizaciones y procedimientos ambulatorios'),
  (2, 'Dermatología Veterinaria', 'Diagnóstico y tratamiento de alergias y afecciones cutáneas'),
  (3, 'Traumatología y Ortopedia', 'Cirugía traumatológica y rehabilitación osteoarticular')
ON CONFLICT (nombre) DO NOTHING;

-- 4. Ficha de Veterinario (asociada al usuario veterinario)
INSERT INTO veterinarios (id_veterinario, id_usuario, id_especialidad, numero_colegiatura, activo)
VALUES
  (1, 2, 1, 'CMVP-14892', true)
ON CONFLICT (numero_colegiatura) DO NOTHING;

-- 5. Catálogo de Vacunas
INSERT INTO vacunas_catalogo (id_vacuna, nombre, especie_destino, descripcion)
VALUES
  (1, 'Séxtuple Canina (DHPPi/L)', 'CANINO', 'Protege contra distemper, hepatitis, parvovirus, parainfluenza y leptospirosis'),
  (2, 'Triple Felina (FVRCP)', 'FELINO', 'Protege contra rinotraqueitis, calicivirus y panleucopenia'),
  (3, 'Antirrábica', 'TODAS', 'Vacunación obligatoria contra el virus de la rabia'),
  (4, 'KC Tos de las Perreras', 'CANINO', 'Inmunización contra Bordetella bronchiseptica')
ON CONFLICT (nombre) DO NOTHING;

-- 6. Medicamentos e Insumos Iniciales
INSERT INTO medicamentos (id_medicamento, codigo, nombre_comercial, principio_activo, presentacion, unidad_medida, stock_actual, stock_minimo, precio_venta)
VALUES
  (1, 'MED-001', 'Amoxicilina + Clavulánico 250mg', 'Amoxicilina', 'Frasco 60ml', 'FRASCO', 15, 5, 45.00),
  (2, 'MED-002', 'Meloxicam 0.5% Gotas', 'Meloxicam', 'Frasco gotero 10ml', 'FRASCO', 8, 3, 38.50),
  (3, 'MED-003', 'Bravecto 10-20kg', 'Fluralaner', 'Tableta masticable', 'TABLETA', 12, 4, 110.00),
  (4, 'MED-004', 'Doxiciclina 100mg', 'Doxiciclina', 'Caja x 10 tabletas', 'CAJA', 20, 5, 25.00)
ON CONFLICT (codigo) DO NOTHING;

-- 7. Lotes de Medicamento de Ejemplo (PEPS/FIFO)
INSERT INTO lotes_medicamento (id_lote, id_medicamento, numero_lote, fecha_vencimiento, cantidad_inicial, cantidad_actual, precio_unitario)
VALUES
  (1, 1, 'LOT-AMX-2026-A', '2026-12-31', 10, 10, 32.00),
  (2, 1, 'LOT-AMX-2027-B', '2027-06-30', 5, 5, 33.50),
  (3, 2, 'LOT-MLX-2026-X', '2026-11-15', 8, 8, 28.00)
ON CONFLICT DO NOTHING;
