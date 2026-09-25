# Listado de módulos

El Sistema de Gestión de Turnos para una Clínica Médica se organizará en módulos funcionales. Las prioridades indican el orden y la importancia previstos para el desarrollo de la primera versión.

## Criterios de prioridad

- **Alta:** funcionalidad indispensable para que el sistema pueda operar.
- **Media:** funcionalidad necesaria para mejorar la organización y el uso del sistema.
- **Baja:** funcionalidad complementaria que puede incorporarse después de completar los procesos principales.

| Código | Módulo | Descripción | Prioridad |
|---|---|---|---|
| MOD01 | Pacientes | Permitirá registrar, consultar, actualizar y realizar la baja lógica de los pacientes. Administrará sus datos personales y de contacto. | Alta |
| MOD02 | Profesionales | Permitirá registrar, consultar, actualizar y realizar la baja lógica de los profesionales, incluyendo nombre, apellido, matrícula y datos de contacto. | Alta |
| MOD03 | Especialidades | Permitirá administrar las especialidades ofrecidas por la clínica y asociarlas con los profesionales correspondientes. | Alta |
| MOD04 | Horarios de atención | Permitirá definir los días, franjas horarias y duración de los turnos de cada profesional. | Alta |
| MOD05 | Disponibilidad | Permitirá calcular y consultar los horarios libres de cada profesional según sus horarios de atención y los turnos ya registrados. | Alta |
| MOD06 | Turnos | Permitirá registrar, consultar, cancelar y reprogramar turnos, relacionando pacientes, profesionales, especialidades, fechas y horarios. | Alta |
| MOD07 | Agenda profesional | Permitirá consultar los turnos de un profesional por día, fecha o estado para organizar su atención. | Media |
| MOD08 | Usuarios y autenticación | Permitirá iniciar sesión y controlar el acceso al sistema según los roles de administrador, recepcionista y profesional. | Alta |
| MOD09 | Reportes | Permitirá obtener información administrativa sobre turnos, cancelaciones, ausencias y atención por profesional o especialidad. | Baja |

## Dependencias entre módulos

- El módulo de disponibilidad depende de profesionales, especialidades y horarios de atención.
- El módulo de turnos depende de pacientes, profesionales, especialidades y disponibilidad.
- La agenda profesional depende de los turnos registrados.
- El módulo de usuarios y autenticación controlará el acceso a las funcionalidades según el rol asignado.
- Los reportes utilizarán la información producida por los demás módulos.

## Alcance de la primera versión

La primera versión se concentrará en la gestión de pacientes, profesionales, especialidades, horarios, disponibilidad y turnos. La autenticación, la agenda y los reportes podrán completarse progresivamente según el avance del equipo y la validación del tutor.