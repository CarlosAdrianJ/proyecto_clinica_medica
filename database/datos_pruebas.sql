-- ============================================================
-- Sistema de Gestión de Turnos para una Clínica Médica
-- datos_prueba.sql — datos de ejemplo para comprobar el
--                     funcionamiento del esquema
-- Ejecutar después de schema.sql
-- ============================================================

USE clinica_medica;

-- ------------------------------------------------------------
-- Especialidades
-- ------------------------------------------------------------
INSERT INTO especialidades (nombre, descripcion) VALUES
    ('Clínica médica',    'Atención médica general'),
    ('Odontología',       'Atención y tratamientos odontológicos'),
    ('Dermatología',      'Diagnóstico y tratamiento de afecciones de la piel'),
    ('Psicología',        'Atención y acompañamiento psicológico'),
    ('Nutrición',         'Evaluación y asesoramiento nutricional'),
    ('Kinesiología',      'Rehabilitación y tratamiento kinésico'),
    ('Medicina estética', 'Tratamientos médicos estéticos');

-- ------------------------------------------------------------
-- Profesionales
-- ------------------------------------------------------------
INSERT INTO profesionales (nombre, apellido, matricula, telefono, email) VALUES
    ('Laura',    'Gómez',      'MAT-1001', '3415551001', 'laura.gomez@clinica.com'),
    ('Martín',   'Rodríguez',  'MAT-1002', '3415551002', 'martin.rodriguez@clinica.com'),
    ('Carolina', 'Fernández',  'MAT-1003', '3415551003', 'carolina.fernandez@clinica.com');

-- ------------------------------------------------------------
-- Relación profesional - especialidad
-- Carolina Fernández queda con dos especialidades (Psicología y
-- Nutrición) a propósito, para ejercitar el lado "muchos" de la
-- relación N:M.
-- ------------------------------------------------------------
INSERT INTO profesional_especialidad (profesional_id, especialidad_id) VALUES
    (1, 1),  -- Laura Gómez      -> Clínica médica
    (2, 2),  -- Martín Rodríguez -> Odontología
    (3, 4),  -- Carolina Fernández -> Psicología
    (3, 5);  -- Carolina Fernández -> Nutrición

-- ------------------------------------------------------------
-- Pacientes
-- ------------------------------------------------------------
INSERT INTO pacientes (nombre, apellido, dni, fecha_nacimiento, telefono, email, direccion) VALUES
    ('Juan',  'Pérez',     '30111222', '1983-05-12', '3415552001', 'juan.perez@email.com',     'San Martín 1250'),
    ('María', 'López',     '32999888', '1986-09-23', '3415552002', 'maria.lopez@email.com',    'Córdoba 850'),
    ('Pedro', 'Martínez',  '28777666', '1979-02-15', '3415552003', 'pedro.martinez@email.com', 'Mendoza 2100');

-- ------------------------------------------------------------
-- Usuarios
-- ------------------------------------------------------------
INSERT INTO usuarios (nombre_usuario, contrasena_hash, rol, profesional_id) VALUES
    ('admin',       '$2a$10$examplehash.administrador.0000000000000000000', 'ADMINISTRADOR', NULL),
    ('recepcion1',  '$2a$10$examplehash.recepcionista.0000000000000000000', 'RECEPCIONISTA', NULL),
    ('laura.gomez', '$2a$10$examplehash.profesional.00000000000000000000',  'PROFESIONAL',   1);

-- ------------------------------------------------------------
-- Horarios de atención
-- ------------------------------------------------------------
INSERT INTO horarios_atencion (profesional_id, dia_semana, hora_desde, hora_hasta, duracion_turno) VALUES
    (1, 'LUNES',     '08:00:00', '12:00:00', 30),
    (1, 'MIERCOLES', '14:00:00', '18:00:00', 30),
    (2, 'MARTES',    '09:00:00', '13:00:00', 45),
    (3, 'VIERNES',   '14:00:00', '18:00:00', 60);

-- ------------------------------------------------------------
-- Turnos
-- ------------------------------------------------------------
INSERT INTO turnos (paciente_id, profesional_id, especialidad_id, fecha, hora_inicio, hora_fin, estado, observaciones) VALUES
    -- turnos pasados
    (1, 1, 1, '2026-08-24', '08:00:00', '08:30:00', 'ATENDIDO',  'Consulta de control'),
    (2, 1, 1, '2026-08-24', '08:30:00', '09:00:00', 'AUSENTE',   'Paciente no se presentó'),
    (3, 2, 2, '2026-08-25', '09:00:00', '09:45:00', 'ATENDIDO',  'Control odontológico realizado'),
    (3, 1, 1, '2026-08-26', '14:00:00', '14:30:00', 'CANCELADO', 'Cancelado por el paciente'),
    (1, 3, 4, '2026-08-28', '14:00:00', '15:00:00', 'ATENDIDO',  'Consulta psicológica realizada'),
    -- turnos futuros (pendientes)
    (1, 1, 1, '2026-09-07', '08:00:00', '08:30:00', 'PENDIENTE', 'Primera consulta'),
    (2, 2, 2, '2026-09-08', '09:00:00', '09:45:00', 'PENDIENTE', 'Control odontológico'),
    (3, 3, 4, '2026-09-11', '14:00:00', '15:00:00', 'PENDIENTE', 'Consulta inicial'),
    (2, 3, 5, '2026-09-11', '15:00:00', '16:00:00', 'PENDIENTE', 'Primera consulta nutricional');
