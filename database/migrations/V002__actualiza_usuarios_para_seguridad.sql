USE clinica_medica;

ALTER TABLE usuarios
    CHANGE COLUMN contrasena contrasena_hash VARCHAR(255) NOT NULL,
    ADD COLUMN profesional_id BIGINT NULL AFTER rol,
    ADD CONSTRAINT fk_usuario_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE SET NULL;