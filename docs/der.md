# Diagrama entidad-relación

El siguiente diagrama representa las entidades principales del Sistema de Gestión de Turnos para una Clínica Médica. Incluye las claves primarias, claves foráneas y relaciones definidas en `database/schema.sql`.

```mermaid
erDiagram
    PACIENTES ||--o{ TURNOS : solicita
    PROFESIONALES ||--o{ HORARIOS_ATENCION : posee
    PROFESIONALES ||--o{ PROFESIONAL_ESPECIALIDAD : tiene
    ESPECIALIDADES ||--o{ PROFESIONAL_ESPECIALIDAD : incluye
    PROFESIONAL_ESPECIALIDAD ||--o{ TURNOS : habilita
    PROFESIONALES o|--o{ USUARIOS : vincula

    PACIENTES {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR dni UK
        DATE fecha_nacimiento
        VARCHAR telefono
        VARCHAR email
        VARCHAR direccion
        BOOLEAN activo
        DATETIME fecha_alta
    }

    PROFESIONALES {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR matricula UK
        VARCHAR telefono
        VARCHAR email
        BOOLEAN activo
        DATETIME fecha_alta
    }

    ESPECIALIDADES {
        BIGINT id PK
        VARCHAR nombre UK
        VARCHAR descripcion
        BOOLEAN activo
    }

    PROFESIONAL_ESPECIALIDAD {
        BIGINT profesional_id PK, FK
        BIGINT especialidad_id PK, FK
    }

    HORARIOS_ATENCION {
        BIGINT id PK
        BIGINT profesional_id FK
        ENUM dia_semana
        TIME hora_desde
        TIME hora_hasta
        INT duracion_turno
        BOOLEAN activo
    }

    TURNOS {
        BIGINT id PK
        BIGINT paciente_id FK
        BIGINT profesional_id FK
        BIGINT especialidad_id FK
        DATE fecha
        TIME hora_inicio
        TIME hora_fin
        ENUM estado
        VARCHAR observaciones
        DATETIME fecha_creacion
    }

    USUARIOS {
        BIGINT id PK
        VARCHAR nombre_usuario UK
        VARCHAR contrasena_hash
        ENUM rol
        BIGINT profesional_id FK
        BOOLEAN activo
    }
```

## Relaciones principales

- Un paciente puede tener varios turnos.
- Un profesional puede tener varios horarios de atención.
- Un profesional puede tener una o más especialidades.
- Una especialidad puede estar asociada con varios profesionales.
- La relación entre profesionales y especialidades se representa mediante la tabla `profesional_especialidad`.
- Cada turno corresponde a un paciente, un profesional y una especialidad habilitada para ese profesional.
- Un usuario con rol profesional puede estar vinculado con un registro de la tabla `profesionales`.

## Claves y restricciones principales

- `pacientes.dni` es único.
- `profesionales.matricula` es única.
- `especialidades.nombre` es único.
- La combinación de `profesional_id` y `especialidad_id` constituye la clave primaria de `profesional_especialidad`.
- No se permiten dos turnos con el mismo profesional, fecha y hora de inicio.
- La hora de finalización de un turno debe ser posterior a su hora de inicio.
- La hora final de un horario de atención debe ser posterior a su hora inicial.
- La duración asignada a los turnos debe ser mayor que cero.

## Índices principales

- `idx_turnos_paciente`: facilita las consultas de turnos por paciente.
- `idx_turnos_profesional_fecha`: facilita las consultas por profesional y fecha.
- `idx_turnos_estado`: facilita el filtrado por estado.
- `idx_horarios_profesional`: facilita la consulta de horarios por profesional.
- `idx_pacientes_apellido`: facilita la búsqueda de pacientes por apellido.
- `idx_profesionales_apellido`: facilita la búsqueda de profesionales por apellido.