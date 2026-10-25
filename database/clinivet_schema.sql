-- ============================================================
-- Clinivet Arias — Esquema de Base de Datos (PostgreSQL 15 / Supabase)
-- ============================================================

-- 1. Tabla de Usuarios del Personal
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(30) NOT NULL CHECK (rol IN ('ADMIN', 'VETERINARIO', 'RECEPCIONISTA')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- 2. Tabla de Propietarios (Clientes)
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
    fecha_registro TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_propietarios_documento ON propietarios(numero_documento);

-- 3. Tabla de Mascotas (Pacientes)
CREATE TABLE IF NOT EXISTS mascotas (
    id_mascota BIGSERIAL PRIMARY KEY,
    id_propietario BIGINT NOT NULL REFERENCES propietarios(id_propietario) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raza VARCHAR(100),
    sexo VARCHAR(10) NOT NULL CHECK (sexo IN ('MACHO', 'HEMBRA')),
    fecha_nacimiento DATE,
    peso_kg NUMERIC(5,2),
    microchip VARCHAR(50) UNIQUE,
    alergias TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mascotas_propietario ON mascotas(id_propietario);

-- 4. Tabla de Citas Médicas
CREATE TABLE IF NOT EXISTS citas (
    id_cita BIGSERIAL PRIMARY KEY,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    fecha_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA' CHECK (estado IN ('PROGRAMADA', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA', 'NO_ASISTIO')),
    observaciones TEXT,
    fecha_registro TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_citas_vet_fecha ON citas(id_veterinario, fecha_hora);

-- 5. Tabla de Consultas Clínicas
CREATE TABLE IF NOT EXISTS consultas (
    id_consulta BIGSERIAL PRIMARY KEY,
    id_cita BIGINT REFERENCES citas(id_cita) ON DELETE SET NULL,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    fecha_hora TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    motivo_consulta VARCHAR(255) NOT NULL,
    anamnesis TEXT,
    diagnostico TEXT NOT NULL,
    temperatura NUMERIC(4,1),
    frecuencia_cardiaca INT,
    frecuencia_respiratoria INT,
    peso_actual NUMERIC(5,2)
);

-- 6. Tabla de Tratamientos / Recetas
CREATE TABLE IF NOT EXISTS tratamientos (
    id_tratamiento BIGSERIAL PRIMARY KEY,
    id_consulta BIGINT NOT NULL REFERENCES consultas(id_consulta) ON DELETE CASCADE,
    medicamento VARCHAR(150) NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion_dias INT NOT NULL,
    instrucciones TEXT
);

-- 7. Catálogo de Medicamentos e Insumos
CREATE TABLE IF NOT EXISTS medicamentos (
    id_medicamento BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre_comercial VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150),
    presentacion VARCHAR(100) NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    precio_venta NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 8. Lotes de Medicamentos (PEPS / FIFO)
CREATE TABLE IF NOT EXISTS lotes (
    id_lote BIGSERIAL PRIMARY KEY,
    id_medicamento BIGINT NOT NULL REFERENCES medicamentos(id_medicamento) ON DELETE RESTRICT,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial INT NOT NULL,
    cantidad_disponible INT NOT NULL,
    fecha_ingreso TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lotes_vencimiento ON lotes(fecha_vencimiento ASC);

-- 9. Movimientos de Inventario (Kardex)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id_movimiento BIGSERIAL PRIMARY KEY,
    id_medicamento BIGINT NOT NULL REFERENCES medicamentos(id_medicamento) ON DELETE RESTRICT,
    id_lote BIGINT REFERENCES lotes(id_lote),
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'VENCIDO')),
    cantidad INT NOT NULL,
    motivo TEXT,
    fecha_movimiento TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Catálogo de Vacunas
CREATE TABLE IF NOT EXISTS catalogo_vacunas (
    id_vacuna BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especie_destino VARCHAR(50) NOT NULL,
    descripcion TEXT
);

-- 11. Vacunas Aplicadas
CREATE TABLE IF NOT EXISTS vacunas_aplicadas (
    id_vacuna_aplicada BIGSERIAL PRIMARY KEY,
    id_mascota BIGINT NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    id_vacuna BIGINT NOT NULL REFERENCES catalogo_vacunas(id_vacuna) ON DELETE RESTRICT,
    id_veterinario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    fecha_aplicacion DATE NOT NULL,
    proxima_dosis DATE,
    numero_lote VARCHAR(50)
);

-- 12. Notificaciones del Personal
CREATE TABLE IF NOT EXISTS notificaciones (
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
