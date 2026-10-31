USE clinica_medica;

ALTER TABLE usuarios
    MODIFY COLUMN rol ENUM(
        'ADMINISTRADOR',
        'RECEPCIONISTA',
        'PROFESIONAL',
        'PACIENTE'
    ) NOT NULL,
    ADD COLUMN paciente_id BIGINT NULL AFTER profesional_id,
    ADD CONSTRAINT uq_usuario_paciente UNIQUE (paciente_id),
    ADD CONSTRAINT fk_usuario_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id)
        ON DELETE SET NULL;