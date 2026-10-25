-- ============================================================
-- Clinivet Arias — Esquema de Base de Datos Normalizado (PostgreSQL 15 / Supabase)
-- Cumple con 1FN, 2FN y 3FN (16 Tablas Relacionales según APF2)
-- ============================================================

-- ── ENUMS Y TIPOS ───────────────────────────────────────────
DO $$ BEGIN
    CREATE TYPE estado_cita AS ENUM ('programada', 'confirmada', 'atendida', 'cancelada', 'no_asistio');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_movimiento AS ENUM ('entrada', 'salida', 'ajuste', 'vencido');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_notificacion AS ENUM ('cita_creada', 'stock_bajo', 'proximo_vencimiento', 'recordatorio');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- ============================================================
-- ÁREA 1: SEGURIDAD Y PERSONAL (6 tablas)
-- ============================================================

-- 1. Roles (3FN: desacopla el rol de la entidad usuario)
CREATE TABLE IF NOT EXISTS roles (
    id_rol BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Usuarios del sistema
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    id_rol BIGINT NOT NULL REFERENCES roles(id_rol) ON DELETE RESTRICT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(30) NOT NULL DEFAULT 'ADMIN', -- Soporte directo para Enum en Spring Security
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- 3. Especialidades veterinarias (3FN: normaliza especialidad)
CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidad BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- 4. Veterinarios (3FN: atributos específicos del médico veterinario)
CREATE TABLE IF NOT EXISTS veterinarios (
    id_veterinario BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    id_especialidad BIGINT REFERENCES especialidades(id_especialidad) ON DELETE SET NULL,
    numero_colegiatura VARCHAR(30) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. Horarios de disponibilidad (1FN: no almacena listas ni rangos multivaluados)
CREATE TABLE IF NOT EXISTS horarios_disponibilidad (
    id_horario BIGSERIAL PRIMARY KEY,
    id_veterinario BIGINT NOT NULL REFERENCES veterinarios(id_veterinario) ON DELETE CASCADE,
    dia_semana SMALLINT NOT NULL CHECK (dia_semana BETWEEN 0 AND 6), -- 0=Domingo, 6=Sábado
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CHECK (hora_fin > hora_inicio)
);

-- 6. Notificaciones internas
CREATE TABLE IF NOT EXISTS notificaciones (
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ÁREA 2: PACIENTES Y PROPIETARIOS (2 tablas)
-- ============================================================

-- 7. Propietarios (Clientes)
CREATE TABLE IF NOT EXISTS propietarios (
    id_propietario BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    direccion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_propietarios_documento ON propietarios(numero_documento);

-- 8. Mascotas (Pacientes) (3FN: datos del propietario no se repiten)
CREATE TABLE IF NOT EXISTS mascotas (
    id_mascota BIGSERIAL PRIMARY KEY,
    id_propietario BIGINT NOT NULL REFERENCES propietarios(id_propietario) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raza VARCHAR(100),
    color VARCHAR(50),
    sexo VARCHAR(10) NOT NULL CHECK (sexo IN ('MACHO', 'HEMBRA')),
    fecha_nacimiento DATE,
    peso_actual_kg NUMERIC(5,2), -- Desnormalización controlada para acceso rápido (APF2)
    microchip VARCHAR(50) UNIQUE,
    alergias TEXT,
    foto_url TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mascotas_propietario ON mascotas(id_propietario);

-- ============================================================
-- ÁREA 3: ATENCIÓN CLÍNICA Y CITAS (3 tablas)
-- ============================================================

-- 9. Citas Médicas
CREATE TABLE IF NOT EXISTS citas (
    id_cita BIGSERIAL PRIMARY KEY,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    fecha_hora TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
    motivo VARCHAR(255) NOT NULL,
    observaciones TEXT,
    creado_por BIGINT REFERENCES usuarios(id_usuario),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- RN-03: Bloqueo de citas duplicadas para el mismo veterinario a la misma hora
CREATE UNIQUE INDEX IF NOT EXISTS uq_cita_vet_fecha ON citas (id_veterinario, fecha_hora)
    WHERE estado <> 'CANCELADA';

-- 10. Consultas Clínicas
CREATE TABLE IF NOT EXISTS consultas (
    id_consulta BIGSERIAL PRIMARY KEY,
    id_cita BIGINT UNIQUE REFERENCES citas(id_cita) ON DELETE SET NULL,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo_consulta VARCHAR(255) NOT NULL,
    anamnesis TEXT,
    diagnostico TEXT NOT NULL,
    peso_kg NUMERIC(5,2), -- Historial real e inmutable del peso en la consulta (1FN)
    temperatura_c NUMERIC(4,1),
    frecuencia_cardiaca INT,
    frecuencia_respiratoria INT,
    observaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. Tratamientos / Prescripción Médica (3FN: normaliza medicamento recetado)
CREATE TABLE IF NOT EXISTS tratamientos (
    id_tratamiento BIGSERIAL PRIMARY KEY,
    id_consulta BIGINT NOT NULL REFERENCES consultas(id_consulta) ON DELETE CASCADE,
    medicamento VARCHAR(150) NOT NULL,
    dosis_mg_por_kg NUMERIC(8,2) NOT NULL CHECK (dosis_mg_por_kg > 0),
    dosis_total_mg NUMERIC(8,2) NOT NULL, -- Desnormalización controlada RF-23: peso × dosis
    duracion_dias INT NOT NULL CHECK (duracion_dias > 0),
    indicaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ÁREA 4: VACUNACIÓN (2 tablas)
-- ============================================================

-- 12. Catálogo de Vacunas (3FN: maestro de vacunas)
CREATE TABLE IF NOT EXISTS vacunas_catalogo (
    id_vacuna BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    especie_destino VARCHAR(50) NOT NULL,
    descripcion TEXT
);

-- 13. Vacunas Aplicadas (Historial)
CREATE TABLE IF NOT EXISTS vacunas_aplicadas (
    id_vacuna_aplicada BIGSERIAL PRIMARY KEY,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_vacuna BIGINT NOT NULL REFERENCES vacunas_catalogo(id_vacuna) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_consulta BIGINT REFERENCES consultas(id_consulta) ON DELETE SET NULL,
    fecha_aplicacion DATE NOT NULL,
    proxima_dosis DATE,
    numero_lote VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ÁREA 5: INVENTARIO Y LOTES (3 tablas)
-- ============================================================

-- 14. Medicamentos e Insumos (Catálogo Maestro)
CREATE TABLE IF NOT EXISTS medicamentos (
    id_medicamento BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre_comercial VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150),
    presentacion VARCHAR(100) NOT NULL,
    unidad_medida VARCHAR(30) NOT NULL DEFAULT 'UNIDAD',
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    precio_venta NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. Lotes de Medicamentos (2FN y 3FN: trazabilidad PEPS/FIFO por lote)
CREATE TABLE IF NOT EXISTS lotes_medicamento (
    id_lote BIGSERIAL PRIMARY KEY,
    id_medicamento BIGINT NOT NULL REFERENCES medicamentos(id_medicamento) ON DELETE RESTRICT,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial INT NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_actual INT NOT NULL CHECK (cantidad_actual >= 0), -- RN-05: nunca negativo
    precio_unitario NUMERIC(10,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_medicamento, numero_lote)
);

-- RN-10: Índice para dispensación rápida PEPS / FIFO
CREATE INDEX IF NOT EXISTS idx_lotes_fifo ON lotes_medicamento (id_medicamento, fecha_vencimiento ASC)
    WHERE cantidad_actual > 0;

-- 16. Movimientos de Inventario (Kardex / Auditoría)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id_movimiento BIGSERIAL PRIMARY KEY,
    id_lote BIGINT NOT NULL REFERENCES lotes_medicamento(id_lote) ON DELETE RESTRICT,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    id_consulta BIGINT REFERENCES consultas(id_consulta) ON DELETE SET NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'VENCIDO')),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    motivo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- FUNCIONES AUXILIARES (RF-23: Cálculo de dosis)
-- ============================================================
CREATE OR REPLACE FUNCTION calcular_dosis_total(peso_kg NUMERIC, dosis_mg_kg NUMERIC)
RETURNS NUMERIC LANGUAGE sql IMMUTABLE AS $$
    SELECT ROUND(peso_kg * dosis_mg_kg, 2);
$$;
