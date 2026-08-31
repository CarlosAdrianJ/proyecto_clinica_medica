-- ============================================================
-- Sistema de Gestión de Turnos para una Clínica Médica
-- schema.sql — creación de base de datos, tablas, relaciones
--              y restricciones
-- Motor: MySQL 8.x / MariaDB 10.x
--
-- Convención de nombres: cada tabla usa "id" como clave primaria
-- y "tabla_id" para las claves foráneas (ej. profesional_id).
-- Se eligió así porque es la convención por defecto de Spring
-- Data JPA / Hibernate: evita tener que anotar @Column en cada
-- entidad para que coincida con el nombre de columna.
-- ============================================================

CREATE DATABASE IF NOT EXISTS clinica_medica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clinica_medica;

-- Se dropean las tablas en orden inverso a sus dependencias
-- (las que tienen FKs primero) para poder re-ejecutar este
-- script las veces que haga falta durante el desarrollo.
DROP TABLE IF EXISTS turnos;
DROP TABLE IF EXISTS horarios_atencion;
DROP TABLE IF EXISTS profesional_especialidad;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS pacientes;
DROP TABLE IF EXISTS profesionales;
DROP TABLE IF EXISTS especialidades;

-- ------------------------------------------------------------
-- especialidades
-- Especialidades que ofrece la clínica (odontología, nutrición,
-- etc.). No depende de ninguna otra tabla.
-- ------------------------------------------------------------
CREATE TABLE especialidades (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo      BOOLEAN NOT NULL DEFAULT TRUE  -- baja lógica
);

-- ------------------------------------------------------------
-- profesionales
-- Datos del profesional de la salud. La especialidad NO va acá
-- directo: un profesional puede tener más de una, por eso se
-- resuelve en la tabla intermedia profesional_especialidad.
-- ------------------------------------------------------------
CREATE TABLE profesionales (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    apellido   VARCHAR(100) NOT NULL,
    matricula  VARCHAR(50) NOT NULL UNIQUE,
    telefono   VARCHAR(30),
    email      VARCHAR(150),
    activo     BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- PACIENTES
-- Datos de los pacientes que solicitan turnos.  

CREATE TABLE pacientes (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    dni              VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE,
    telefono         VARCHAR(30),
    email            VARCHAR(150),
    direccion        VARCHAR(200),
    activo           BOOLEAN NOT NULL DEFAULT TRUE,  -- baja lógica
    fecha_alta       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- USUARIOS
-- Login y rol de acceso al sistema. Cuando rol = 'PROFESIONAL',
-- profesional_id indica a qué profesional corresponde ese login
-- (para que, por ejemplo, solo pueda ver su propia agenda).
-- Para ADMINISTRADOR/RECEPCIONISTA, profesional_id queda NULL.

CREATE TABLE usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario  VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,  -- guardar SIEMPRE hasheada, nunca en texto plano
    rol             ENUM('ADMINISTRADOR', 'RECEPCIONISTA', 'PROFESIONAL') NOT NULL,
    profesional_id  BIGINT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_usuario_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE SET NULL
);

-- PROFESIONAL_ESPECIALIDAD
-- Relación N:M entre profesionales y especialidades.
-- Si se borra un profesional o una especialidad, se borran acá
-- las relaciones asociadas (ON DELETE CASCADE) — pero en la
-- práctica no deberían borrarse filas de profesionales/
-- especialidades, sino desactivarlas con "activo = false".

CREATE TABLE profesional_especialidad (
    profesional_id  BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,

    PRIMARY KEY (profesional_id, especialidad_id),

    CONSTRAINT fk_profesional_especialidad_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_profesional_especialidad_especialidad
        FOREIGN KEY (especialidad_id)
        REFERENCES especialidades(id)
        ON DELETE CASCADE
);


-- HORARIOS DE ATENCIÓN
-- Días/horarios en los que atiende cada profesional y la
-- duración de sus turnos (usada por el backend para calcular
-- los huecos disponibles en el módulo de disponibilidad).

CREATE TABLE horarios_atencion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    profesional_id  BIGINT NOT NULL,
    dia_semana      ENUM('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO') NOT NULL,
    hora_desde      TIME NOT NULL,
    hora_hasta      TIME NOT NULL,
    duracion_turno  INT NOT NULL,  -- minutos por turno para ese bloque horario
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_horario_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_horas_atencion
        CHECK (hora_desde < hora_hasta),

    CONSTRAINT chk_duracion_turno
        CHECK (duracion_turno > 0)
);


-- TURNOS
-- hora_inicio/hora_fin quedan grabados en el propio turno 


CREATE TABLE turnos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id     BIGINT NOT NULL,
    profesional_id  BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    fecha           DATE NOT NULL,
    hora_inicio     TIME NOT NULL,
    hora_fin        TIME NOT NULL,
    estado          ENUM('PENDIENTE', 'ATENDIDO', 'CANCELADO', 'AUSENTE') NOT NULL DEFAULT 'PENDIENTE',
    observaciones   VARCHAR(500),
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_turno_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id),

    CONSTRAINT fk_turno_profesional_especialidad
        FOREIGN KEY (profesional_id, especialidad_id)
        REFERENCES profesional_especialidad(profesional_id, especialidad_id),

    CONSTRAINT chk_turno_rango
        CHECK (hora_fin > hora_inicio),

    -- Evita dos turnos para el mismo profesional que arranquen a
    -- la misma fecha/hora. 
    CONSTRAINT uq_turno_profesional_fecha_hora
        UNIQUE (profesional_id, fecha, hora_inicio)
);


-- Índices para las consultas más frecuentes del sistema

CREATE INDEX idx_turnos_paciente ON turnos (paciente_id);
CREATE INDEX idx_turnos_profesional_fecha ON turnos (profesional_id, fecha);
CREATE INDEX idx_turnos_estado ON turnos (estado);
CREATE INDEX idx_horarios_profesional ON horarios_atencion (profesional_id);
CREATE INDEX idx_pacientes_apellido ON pacientes (apellido);
CREATE INDEX idx_profesionales_apellido ON profesionales (apellido);
