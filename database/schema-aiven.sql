

CREATE TABLE especialidades (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

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

CREATE TABLE pacientes (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    dni              VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE,
    telefono         VARCHAR(30),
    email            VARCHAR(150),
    direccion        VARCHAR(200),
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario  VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    rol             ENUM('ADMINISTRADOR', 'RECEPCIONISTA', 'PROFESIONAL') NOT NULL,
    profesional_id  BIGINT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_usuario_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE SET NULL
);

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

CREATE TABLE horarios_atencion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    profesional_id  BIGINT NOT NULL,
    dia_semana      ENUM(
        'LUNES', 'MARTES', 'MIERCOLES', 'JUEVES',
        'VIERNES', 'SABADO', 'DOMINGO'
    ) NOT NULL,
    hora_desde      TIME NOT NULL,
    hora_hasta      TIME NOT NULL,
    duracion_turno  INT NOT NULL,
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

CREATE TABLE turnos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id     BIGINT NOT NULL,
    profesional_id  BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    fecha           DATE NOT NULL,
    hora_inicio     TIME NOT NULL,
    hora_fin        TIME NOT NULL,
    estado          ENUM(
        'PENDIENTE', 'ATENDIDO', 'CANCELADO', 'AUSENTE'
    ) NOT NULL DEFAULT 'PENDIENTE',
    observaciones   VARCHAR(500),
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_turno_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id),

    CONSTRAINT fk_turno_profesional_especialidad
        FOREIGN KEY (profesional_id, especialidad_id)
        REFERENCES profesional_especialidad(profesional_id, especialidad_id),

    CONSTRAINT chk_turno_rango
        CHECK (hora_fin > hora_inicio)
);

CREATE INDEX idx_turnos_paciente
    ON turnos (paciente_id);

CREATE INDEX idx_turnos_profesional_fecha
    ON turnos (profesional_id, fecha);

CREATE INDEX idx_turnos_estado
    ON turnos (estado);

CREATE INDEX idx_horarios_profesional
    ON horarios_atencion (profesional_id);

CREATE INDEX idx_pacientes_apellido
    ON pacientes (apellido);

CREATE INDEX idx_profesionales_apellido
    ON profesionales (apellido);