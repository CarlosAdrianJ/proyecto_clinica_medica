USE clinica_medica;

INSERT INTO especialidades (nombre, descripcion) VALUES
('Clínica médica', 'Atención médica general'),
('Odontología', 'Atención y tratamientos odontológicos'),
('Dermatología', 'Diagnóstico y tratamiento de afecciones de la piel'),
('Psicología', 'Atención y acompañamiento psicológico'),
('Nutrición', 'Evaluación y asesoramiento nutricional'),
('Kinesiología', 'Rehabilitación y tratamiento kinésico'),
('Medicina estética', 'Tratamientos médicos estéticos');

INSERT INTO profesionales (
    nombre,
    apellido,
    matricula,
    telefono,
    email
) VALUES
('Laura', 'Gómez', 'MAT-1001', '3415551001', 'laura.gomez@clinica.com'),
('Martín', 'Rodríguez', 'MAT-1002', '3415551002', 'martin.rodriguez@clinica.com'),
('Carolina', 'Fernández', 'MAT-1003', '3415551003', 'carolina.fernandez@clinica.com');

INSERT INTO profesional_especialidad (
    profesional_id,
    especialidad_id
) VALUES
(1, 1),
(2, 2),
(3, 4);

INSERT INTO pacientes (
    nombre,
    apellido,
    dni,
    fecha_nacimiento,
    telefono,
    email,
    direccion
) VALUES
('Juan', 'Pérez', '30111222', '1983-05-12', '3415552001', 'juan.perez@email.com', 'San Martín 1250'),
('María', 'López', '32999888', '1986-09-23', '3415552002', 'maria.lopez@email.com', 'Córdoba 850'),
('Pedro', 'Martínez', '28777666', '1979-02-15', '3415552003', 'pedro.martinez@email.com', 'Mendoza 2100');

INSERT INTO horarios_atencion (
    profesional_id,
    dia_semana,
    hora_desde,
    hora_hasta,
    duracion_turno
) VALUES
(1, 'LUNES', '08:00:00', '12:00:00', 30),
(1, 'MIERCOLES', '14:00:00', '18:00:00', 30),
(2, 'MARTES', '09:00:00', '13:00:00', 45),
(3, 'VIERNES', '14:00:00', '18:00:00', 60);

INSERT INTO turnos (
    paciente_id,
    profesional_id,
    especialidad_id,
    fecha,
    hora,
    estado,
    observaciones
) VALUES
(1, 1, 1, '2026-09-07', '08:00:00', 'PENDIENTE', 'Primera consulta'),
(2, 2, 2, '2026-09-08', '09:00:00', 'PENDIENTE', 'Control odontológico'),
(3, 3, 4, '2026-09-11', '14:00:00', 'PENDIENTE', 'Consulta inicial');