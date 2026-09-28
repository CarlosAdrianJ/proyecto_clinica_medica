# Requisitos del sistema

Los requisitos se identifican mediante códigos para facilitar su trazabilidad durante las etapas de desarrollo y pruebas.

## Requisitos funcionales

### Pacientes

- **RF01:** El sistema debe permitir registrar pacientes con nombre, apellido, DNI, fecha de nacimiento, teléfono, correo electrónico y dirección.
- **RF02:** El sistema debe permitir consultar el listado de pacientes.
- **RF03:** El sistema debe permitir buscar un paciente por su identificador o DNI.
- **RF04:** El sistema debe permitir actualizar los datos de un paciente.
- **RF05:** El sistema debe permitir realizar la baja lógica de un paciente.
- **RF06:** El sistema debe impedir el registro de dos pacientes con el mismo DNI.

### Profesionales

- **RF07:** El sistema debe permitir registrar profesionales con nombre, apellido, matrícula y datos de contacto.
- **RF08:** El sistema debe permitir consultar y actualizar profesionales.
- **RF09:** El sistema debe permitir realizar la baja lógica de un profesional.
- **RF10:** El sistema debe impedir el registro de dos profesionales con la misma matrícula.

### Especialidades

- **RF11:** El sistema debe permitir registrar, consultar y actualizar especialidades.
- **RF12:** El sistema debe permitir realizar la baja lógica de una especialidad.
- **RF13:** El sistema debe impedir el registro de especialidades con nombres repetidos.
- **RF14:** El sistema debe permitir asociar una o más especialidades con cada profesional.

### Horarios y disponibilidad

- **RF15:** El sistema debe permitir registrar los días y horarios de atención de cada profesional.
- **RF16:** El sistema debe permitir definir la duración de los turnos para cada bloque horario.
- **RF17:** El sistema debe permitir consultar los horarios disponibles de un profesional.
- **RF18:** El sistema debe excluir de la disponibilidad los horarios que ya tengan un turno asignado.

### Turnos

- **RF19:** El sistema debe permitir registrar un turno para un paciente con un profesional y una especialidad.
- **RF20:** El sistema debe comprobar que la especialidad seleccionada esté habilitada para el profesional.
- **RF21:** El sistema debe impedir la creación de turnos superpuestos para un mismo profesional.
- **RF22:** El sistema debe permitir consultar turnos por paciente, profesional, fecha y estado.
- **RF23:** El sistema debe permitir cancelar un turno.
- **RF24:** El sistema debe permitir reprogramar un turno.
- **RF25:** El sistema debe permitir cambiar el estado de un turno a pendiente, atendido, cancelado o ausente.
- **RF26:** El sistema debe permitir consultar la agenda diaria de un profesional.

### Usuarios y seguridad

- **RF27:** El sistema debe permitir el inicio de sesión de usuarios autorizados.
- **RF28:** El sistema debe controlar el acceso según los roles de administrador, recepcionista y profesional.
- **RF29:** El profesional debe poder consultar únicamente la información autorizada para su función.
- **RF30:** El sistema debe permitir desactivar usuarios mediante baja lógica.

## Requisitos no funcionales

- **RNF01:** El backend debe desarrollarse con Java y Spring Boot.
- **RNF02:** El frontend debe desarrollarse con React y TypeScript.
- **RNF03:** La información debe almacenarse en una base de datos MySQL.
- **RNF04:** La comunicación entre frontend y backend debe realizarse mediante una API REST y utilizar JSON.
- **RNF05:** Las contraseñas deben almacenarse mediante BCrypt y nunca en texto plano.
- **RNF06:** La autenticación debe utilizar tokens JWT.
- **RNF07:** Los endpoints deben estar protegidos según el rol del usuario.
- **RNF08:** El sistema debe validar los datos recibidos antes de almacenarlos.
- **RNF09:** La base de datos debe mantener la integridad referencial mediante claves primarias y foráneas.
- **RNF10:** El código debe organizarse en capas para facilitar su mantenimiento y sus pruebas.
- **RNF11:** El repositorio debe mantener separados el frontend, el backend, la base de datos y la documentación.
- **RNF12:** Las funcionalidades principales deben contar con pruebas automatizadas.
- **RNF13:** La interfaz debe presentar mensajes claros ante operaciones correctas y errores.
- **RNF14:** La aplicación debe permitir su ejecución en los navegadores web modernos más utilizados.
- **RNF15:** Los datos sensibles y las credenciales no deben almacenarse directamente en el repositorio.

## Trazabilidad por módulo

| Módulo | Requisitos relacionados |
|---|---|
| Pacientes | RF01 a RF06 |
| Profesionales | RF07 a RF10 |
| Especialidades | RF11 a RF14 |
| Horarios y disponibilidad | RF15 a RF18 |
| Turnos y agenda | RF19 a RF26 |
| Usuarios y seguridad | RF27 a RF30 |
| Arquitectura y calidad | RNF01 a RNF15 |