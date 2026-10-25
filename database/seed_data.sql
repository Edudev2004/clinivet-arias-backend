-- ============================================================
-- Clinivet Arias — Datos Iniciales (Seed Data)
-- Contraseña inicial para todas las cuentas: Admin123!
-- Hash BCrypt: $2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2
-- ============================================================

INSERT INTO usuarios (nombres, apellidos, email, password_hash, telefono, rol, activo)
VALUES 
  ('Rommel', 'Navarro', 'admin@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654321', 'ADMIN', true),
  ('Beckhan', 'Elias', 'veterinario@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654322', 'VETERINARIO', true),
  ('Lucero', 'Perez', 'recepcion@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', '987654323', 'RECEPCIONISTA', true)
ON CONFLICT (email) DO NOTHING;

-- Catálogo de Vacunas frecuentes
INSERT INTO catalogo_vacunas (nombre, especie_destino, descripcion)
VALUES
  ('Séxtuple Canina (DHPPi/L)', 'CANINO', 'Protege contra distemper, hepatitis, parvovirus, parainfluenza y leptospirosis'),
  ('Triple Felina (FVRCP)', 'FELINO', 'Protege contra rinotraqueitis, calicivirus y panleucopenia'),
  ('Antirrábica', 'TODAS', 'Vacunación obligatoria contra la rabia'),
  ('KC Tos de las Perreras', 'CANINO', 'Bordetella bronchiseptica')
ON CONFLICT DO NOTHING;

-- Medicamentos iniciales de muestra
INSERT INTO medicamentos (codigo, nombre_comercial, principio_activo, presentacion, stock_actual, stock_minimo, precio_venta)
VALUES
  ('MED-001', 'Amoxicilina + Clavulánico 250mg', 'Amoxicilina', 'Frasco 60ml', 15, 5, 45.00),
  ('MED-002', 'Meloxicam 0.5% Gotas', 'Meloxicam', 'Frasco gotero 10ml', 8, 3, 38.50),
  ('MED-003', 'Bravecto 10-20kg', 'Fluralaner', 'Tableta masticable', 12, 4, 110.00),
  ('MED-004', 'Doxiciclina 100mg', 'Doxiciclina', 'Caja x 10 tabletas', 20, 5, 25.00)
ON CONFLICT (codigo) DO NOTHING;
