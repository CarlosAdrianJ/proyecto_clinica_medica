-- Verificacion de Usuarios y Seguridad sobre MySQL.
-- Solo lectura: no crea, modifica ni elimina registros.
USE clinica_medica;

-- 1. Cuentas, roles y estado.
SELECT id, nombre_usuario, rol, profesional_id, activo
FROM usuarios
ORDER BY id;

-- 2. Comprueba formato BCrypt sin mostrar contrasenas ni hashes.
-- Esperado: formato_bcrypt = 1 para las cuentas creadas mediante la API.
SELECT id, nombre_usuario,
       (CHAR_LENGTH(contrasena_hash) = 60
        AND contrasena_hash REGEXP '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$') AS formato_bcrypt
FROM usuarios
ORDER BY id;

-- 3. Comprueba las asociaciones.
-- Esperado: asociacion_valida = 1; profesional activo para cuentas PROFESIONAL activas.
SELECT u.id, u.nombre_usuario, u.rol, u.activo,
       u.profesional_id, p.nombre, p.apellido, p.activo AS profesional_activo,
       CASE
           WHEN u.rol = 'PROFESIONAL' THEN p.id IS NOT NULL
           ELSE u.profesional_id IS NULL
       END AS asociacion_valida
FROM usuarios u
LEFT JOIN profesionales p ON p.id = u.profesional_id
ORDER BY u.id;

-- 4. Guardar el resultado y repetir tras reiniciar el backend con MySQL.
-- Los mismos IDs y nombres deben seguir presentes.
SELECT id, nombre_usuario, rol, activo
FROM usuarios
ORDER BY id;
