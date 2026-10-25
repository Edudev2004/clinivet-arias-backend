-- ============================================================
-- Clinivet Arias — Esquema de Base de Datos Normalizado (PostgreSQL 15 / Supabase)
-- 100% Alineado con APF2 (Capítulo 5: Tablas 28 a 33 y Sección 5.6)
-- Cumple con 1FN, 2FN y 3FN (16 Tablas con UUID y Restricciones de Negocio)
-- ============================================================

-- Habilitar extensión para generación de UUIDs si no está activa
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── ENUMS Y TIPOS SEGÚN APF2 (Tabla 33) ───────────────────────
DO $$ BEGIN
    CREATE TYPE estado_cita AS ENUM ('programada', 'atendida', 'cancelada');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_movimiento AS ENUM ('entrada', 'salida');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE tipo_notificacion AS ENUM ('cita_creada', 'stock_bajo', 'proximo_vencimiento');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- ============================================================
-- ÁREA 1: SEGURIDAD Y PERSONAL (Tablas 1 a 6)
-- ============================================================

-- 1. Roles de usuario (Tabla 28)
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. Usuarios del sistema (Tabla 28)
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    activo BOOLEAN NOT NULL DEFAULT true,
    ultimo_login TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- 3. Especialidades veterinarias (Tabla 28)
CREATE TABLE IF NOT EXISTS especialidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- 4. Veterinarios (Tabla 28 - Especialización de Usuario con número de colegiatura)
CREATE TABLE IF NOT EXISTS veterinarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    especialidad_id UUID NOT NULL REFERENCES especialidades(id) ON DELETE RESTRICT,
    numero_colegiatura VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT true
);

-- 5. Horarios de disponibilidad (Tabla 28)
CREATE TABLE IF NOT EXISTS horarios_disponibilidad (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    veterinario_id UUID NOT NULL REFERENCES veterinarios(id) ON DELETE CASCADE,
    dia_semana INT2 NOT NULL CHECK (dia_semana BETWEEN 0 AND 6), -- 0=Domingo, 6=Sábado
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CHECK (hora_fin > hora_inicio)
);

-- 6. Notificaciones internas (Tabla 28)
CREATE TABLE IF NOT EXISTS notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo tipo_notificacion NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- ÁREA 2: PACIENTES Y PROPIETARIOS (Tablas 7 y 8)
-- ============================================================

-- 7. Propietarios / Clientes (Tabla 29)
CREATE TABLE IF NOT EXISTS propietarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    direccion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_propietarios_dni ON propietarios(dni);

-- 8. Mascotas / Pacientes (Tabla 29 - RN-01: Propietario obligatorio)
CREATE TABLE IF NOT EXISTS mascotas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    propietario_id UUID NOT NULL REFERENCES propietarios(id) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raza VARCHAR(100),
    color VARCHAR(50),
    sexo CHAR(1) CHECK (sexo IN ('M', 'H', 'Macho', 'Hembra')),
    fecha_nacimiento DATE,
    peso_actual_kg NUMERIC(5,2), -- Desnormalización controlada para acceso ágil (APF2 5.2)
    foto_url TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mascotas_propietario ON mascotas(propietario_id);

-- ============================================================
-- ÁREA 3: ATENCIÓN CLÍNICA Y CITAS (Tablas 9 a 11)
-- ============================================================

-- 9. Citas Médicas (Tabla 30 - RN-02: Mascota y Veterinario obligatorios)
CREATE TABLE IF NOT EXISTS citas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mascota_id UUID NOT NULL REFERENCES mascotas(id) ON DELETE RESTRICT,
    veterinario_id UUID NOT NULL REFERENCES veterinarios(id) ON DELETE RESTRICT,
    fecha_hora TIMESTAMPTZ NOT NULL,
    estado estado_cita NOT NULL DEFAULT 'programada',
    motivo TEXT,
    creado_por UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- RN-03 / RF-15: Un veterinario no puede tener dos citas activas a la misma hora
CREATE UNIQUE INDEX IF NOT EXISTS uq_cita_vet_fecha ON citas (veterinario_id, fecha_hora)
    WHERE estado <> 'cancelada';

-- 10. Consultas Clínicas (Tabla 30)
CREATE TABLE IF NOT EXISTS consultas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cita_id UUID NOT NULL UNIQUE REFERENCES citas(id) ON DELETE RESTRICT,
    peso_kg NUMERIC(5,2),
    temperatura_c NUMERIC(4,1),
    motivo TEXT NOT NULL,
    diagnostico TEXT NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 11. Tratamientos / Prescripción Médica (Tabla 30 - Relación con Medicamentos)
CREATE TABLE IF NOT EXISTS tratamientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consulta_id UUID NOT NULL REFERENCES consultas(id) ON DELETE CASCADE,
    medicamento_id UUID NOT NULL REFERENCES medicamentos(id) ON DELETE RESTRICT,
    dosis_mg_por_kg NUMERIC(8,2) NOT NULL CHECK (dosis_mg_por_kg > 0),
    dosis_total_mg NUMERIC(8,2) NOT NULL, -- Desnormalización controlada RF-23: peso × dosis
    duracion_dias INT4,
    indicaciones TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- ÁREA 4: VACUNACIÓN (Tablas 12 y 13)
-- ============================================================

-- 12. Catálogo de Vacunas (Tabla 31)
CREATE TABLE IF NOT EXISTS vacunas_catalogo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- 13. Vacunas Aplicadas / Historial de Vacunación (Tabla 31)
CREATE TABLE IF NOT EXISTS vacunas_aplicadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mascota_id UUID NOT NULL REFERENCES mascotas(id) ON DELETE RESTRICT,
    vacuna_id UUID NOT NULL REFERENCES vacunas_catalogo(id) ON DELETE RESTRICT,
    consulta_id UUID REFERENCES consultas(id) ON DELETE SET NULL,
    veterinario_id UUID NOT NULL REFERENCES veterinarios(id) ON DELETE RESTRICT,
    fecha_aplicacion DATE NOT NULL,
    proxima_dosis DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- ÁREA 5: INVENTARIO Y LOTES (Tablas 14 a 16)
-- ============================================================

-- 14. Medicamentos e Insumos (Tabla 32)
CREATE TABLE IF NOT EXISTS medicamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150),
    presentacion VARCHAR(100),
    unidad_medida VARCHAR(30) NOT NULL,
    stock_minimo NUMERIC(10,2) NOT NULL DEFAULT 5.00,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 15. Lotes de Medicamento (Tabla 32)
CREATE TABLE IF NOT EXISTS lotes_medicamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medicamento_id UUID NOT NULL REFERENCES medicamentos(id) ON DELETE RESTRICT,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial NUMERIC(10,2) NOT NULL,
    cantidad_actual NUMERIC(10,2) NOT NULL CHECK (cantidad_actual >= 0), -- RN-05: Sin stock negativo
    precio_unitario NUMERIC(10,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (medicamento_id, numero_lote)
);

-- RN-10 / RF-32: Índice optimizado para dispensación PEPS / FIFO
CREATE INDEX IF NOT EXISTS ix_lotes_fifo ON lotes_medicamento (medicamento_id, fecha_vencimiento)
    WHERE cantidad_actual > 0;

-- 16. Movimientos de Inventario / Kardex (Tabla 32)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lote_id UUID NOT NULL REFERENCES lotes_medicamento(id) ON DELETE RESTRICT,
    tipo tipo_movimiento NOT NULL,
    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    motivo TEXT,
    consulta_id UUID REFERENCES consultas(id) ON DELETE SET NULL,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT, -- RN-08
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- FUNCIONES Y TRIGGERS AUXILIARES (APF2 Sección 5.4 y 5.6)
-- ============================================================

-- RF-23: Función matemática para cálculo de dosis total (peso * dosis)
CREATE OR REPLACE FUNCTION calcular_dosis_total(peso_kg NUMERIC, dosis_mg_kg NUMERIC)
RETURNS NUMERIC LANGUAGE sql IMMUTABLE AS $$
    SELECT ROUND(peso_kg * dosis_mg_kg, 2);
$$;

-- Trigger para actualización automática de updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    CREATE TRIGGER set_timestamp_usuarios BEFORE UPDATE ON usuarios FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TRIGGER set_timestamp_propietarios BEFORE UPDATE ON propietarios FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TRIGGER set_timestamp_mascotas BEFORE UPDATE ON mascotas FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TRIGGER set_timestamp_citas BEFORE UPDATE ON citas FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();
EXCEPTION WHEN duplicate_object THEN null; END $$;
