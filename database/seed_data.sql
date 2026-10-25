-- ============================================================
-- Clinivet Arias — Datos Iniciales Normalizados (Seed Data)
-- Compatible con UUIDs y Esquema APF2 (16 Tablas)
-- ============================================================

-- Variables temporales para IDs consistentes
DO $$
DECLARE
    -- Roles
    v_rol_admin UUID := 'a0000000-0000-0000-0000-000000000001'::uuid;
    v_rol_vet   UUID := 'a0000000-0000-0000-0000-000000000002'::uuid;
    v_rol_recep UUID := 'a0000000-0000-0000-0000-000000000003'::uuid;
    v_rol_inv   UUID := 'a0000000-0000-0000-0000-000000000004'::uuid;

    -- Usuarios
    v_user_admin UUID := 'b0000000-0000-0000-0000-000000000001'::uuid;
    v_user_vet   UUID := 'b0000000-0000-0000-0000-000000000002'::uuid;
    v_user_recep UUID := 'b0000000-0000-0000-0000-000000000003'::uuid;

    -- Especialidades
    v_esp_gral   UUID := 'c0000000-0000-0000-0000-000000000001'::uuid;
    v_esp_derm   UUID := 'c0000000-0000-0000-0000-000000000002'::uuid;
    v_esp_trauma UUID := 'c0000000-0000-0000-0000-000000000003'::uuid;

    -- Veterinario
    v_vet_beckhan UUID := 'd0000000-0000-0000-0000-000000000001'::uuid;

    -- Propietario y Mascota Demo
    v_prop_carlos UUID := 'e0000000-0000-0000-0000-000000000001'::uuid;
    v_masc_bobby  UUID := 'f0000000-0000-0000-0000-000000000001'::uuid;

    -- Vacunas
    v_vac_sextuple UUID := '10000000-0000-0000-0000-000000000001'::uuid;
    v_vac_triple   UUID := '10000000-0000-0000-0000-000000000002'::uuid;
    v_vac_rabia    UUID := '10000000-0000-0000-0000-000000000003'::uuid;

    -- Medicamentos
    v_med_amox   UUID := '20000000-0000-0000-0000-000000000001'::uuid;
    v_med_melox  UUID := '20000000-0000-0000-0000-000000000002'::uuid;
    v_med_brav   UUID := '20000000-0000-0000-0000-000000000003'::uuid;

BEGIN
    -- 1. Roles del Personal (Tabla 28 / Tabla 40)
    INSERT INTO roles (id, nombre, descripcion)
    VALUES 
      (v_rol_admin, 'ADMINISTRADOR', 'Administrador con acceso total a módulos y reportes'),
      (v_rol_vet,   'VETERINARIO',   'Médico veterinario especialista: atención clínica, recetas y vacunas'),
      (v_rol_recep, 'RECEPCIONISTA', 'Personal de recepción: propietarios, mascotas y calendario de citas'),
      (v_rol_inv,   'ENCARGADO_INVENTARIO', 'Responsable de medicamentos, lotes PEPS y kardex')
    ON CONFLICT (nombre) DO NOTHING;

    -- 2. Usuarios del Personal (Password inicial: Admin123! con hash BCrypt)
    INSERT INTO usuarios (id, nombre, apellido, email, password_hash, rol_id, activo)
    VALUES 
      (v_user_admin, 'Rommel', 'Navarro', 'admin@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', v_rol_admin, true),
      (v_user_vet,   'Beckhan', 'Elias', 'veterinario@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', v_rol_vet, true),
      (v_user_recep, 'Lucero', 'Perez', 'recepcion@clinivet.pe', '$2a$10$wE0v1n9z3l.VwDqP3h4Q0eYn7.hGzG8vS/jWj1yG5f1.o5X5o/Xl2', v_rol_recep, true)
    ON CONFLICT (email) DO NOTHING;

    -- 3. Especialidades Veterinarias
    INSERT INTO especialidades (id, nombre, descripcion)
    VALUES
      (v_esp_gral,   'Medicina General y Cirugía Menor', 'Consultas preventivas y cirugía ambulatoria'),
      (v_esp_derm,   'Dermatología Veterinaria', 'Diagnóstico de alergias, piel y pelaje'),
      (v_esp_trauma, 'Traumatología y Ortopedia', 'Cirugía traumatológica y ortopedia')
    ON CONFLICT (nombre) DO NOTHING;

    -- 4. Veterinarios Especialistas
    INSERT INTO veterinarios (id, usuario_id, especialidad_id, numero_colegiatura, activo)
    VALUES
      (v_vet_beckhan, v_user_vet, v_esp_gral, 'CMVP-14892', true)
    ON CONFLICT (usuario_id) DO NOTHING;

    -- 5. Horarios de Disponibilidad (Lunes a Viernes de 09:00 a 17:00)
    INSERT INTO horarios_disponibilidad (veterinario_id, dia_semana, hora_inicio, hora_fin)
    VALUES
      (v_vet_beckhan, 1, '09:00:00', '13:00:00'),
      (v_vet_beckhan, 1, '14:00:00', '18:00:00'),
      (v_vet_beckhan, 2, '09:00:00', '13:00:00'),
      (v_vet_beckhan, 2, '14:00:00', '18:00:00'),
      (v_vet_beckhan, 3, '09:00:00', '13:00:00'),
      (v_vet_beckhan, 3, '14:00:00', '18:00:00'),
      (v_vet_beckhan, 4, '09:00:00', '13:00:00'),
      (v_vet_beckhan, 4, '14:00:00', '18:00:00'),
      (v_vet_beckhan, 5, '09:00:00', '13:00:00'),
      (v_vet_beckhan, 5, '14:00:00', '18:00:00')
    ON CONFLICT DO NOTHING;

    -- 6. Propietarios y Mascotas de Ejemplo (RN-01)
    INSERT INTO propietarios (id, nombre, apellido, dni, telefono, email, direccion, activo)
    VALUES
      (v_prop_carlos, 'Carlos', 'Mendoza Ramos', '45892314', '987123456', 'carlos.mendoza@gmail.com', 'Av. Larco 456, Miraflores', true)
    ON CONFLICT (dni) DO NOTHING;

    INSERT INTO mascotas (id, propietario_id, nombre, especie, raza, color, sexo, fecha_nacimiento, peso_actual_kg, activo)
    VALUES
      (v_masc_bobby, v_prop_carlos, 'Bobby', 'Canino', 'Golden Retriever', 'Dorado', 'M', '2022-04-10', 28.50, true)
    ON CONFLICT DO NOTHING;

    -- 7. Catálogo de Vacunas
    INSERT INTO vacunas_catalogo (id, nombre, descripcion)
    VALUES
      (v_vac_sextuple, 'Séxtuple Canina (DHPPi/L)', 'Protección contra distemper, adenovirus, parvovirus, parainfluenza y leptospira'),
      (v_vac_triple,   'Triple Felina (FVRCP)', 'Protección contra rinotraqueitis, calicivirus y panleucopenia felina'),
      (v_vac_rabia,    'Antirrábica', 'Inmunización anual contra el virus de la rabia')
    ON CONFLICT (nombre) DO NOTHING;

    -- 8. Catálogo Maestro de Medicamentos e Insumos
    INSERT INTO medicamentos (id, nombre, principio_activo, presentacion, unidad_medida, stock_minimo, activo)
    VALUES
      (v_med_amox,  'Amoxicilina + Clavulánico 250mg', 'Amoxicilina / Ác. Clavulánico', 'Suspensión 60ml', 'FRASCO', 5.00, true),
      (v_med_melox, 'Meloxicam 0.5% Gotas', 'Meloxicam', 'Frasco gotero 10ml', 'FRASCO', 3.00, true),
      (v_med_brav,  'Bravecto Antiparasitario 10-20kg', 'Fluralaner', 'Tableta masticable', 'TABLETA', 4.00, true)
    ON CONFLICT DO NOTHING;

    -- 9. Lotes de Medicamento (PEPS / FIFO)
    INSERT INTO lotes_medicamento (medicamento_id, numero_lote, fecha_vencimiento, cantidad_inicial, cantidad_actual, precio_unitario)
    VALUES
      (v_med_amox, 'LOT-AMX-2026A', '2026-12-31', 10.00, 10.00, 32.50),
      (v_med_amox, 'LOT-AMX-2027B', '2027-06-30', 5.00,  5.00,  34.00),
      (v_med_melox,'LOT-MLX-2026X', '2026-11-15', 8.00,  8.00,  28.00)
    ON CONFLICT (medicamento_id, numero_lote) DO NOTHING;

END $$;
