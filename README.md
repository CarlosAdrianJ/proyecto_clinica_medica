# Sistema de Gestión de Turnos para una Clínica Médica

## Trabajo Final Integrador

### Integrantes

- García Galfione, Fiorella
- Jabazze, Carlos
- Pascutti, Valentina

## Descripción del proyecto

El proyecto consiste en desarrollar una primera versión funcional de un sistema de gestión de turnos para un centro de salud multidisciplinario de pequeña o mediana escala. El centro contará con distintas especialidades, como clínica médica, odontología, dermatología, psicología, nutrición, kinesiología y medicina estética.

La solución estará orientada principalmente al personal administrativo y permitirá centralizar la información de pacientes, profesionales, especialidades, horarios disponibles y turnos.

## Definición del problema

El presente proyecto se basa en un caso simulado de un centro de salud multidisciplinario de
pequeña o mediana escala, que brinda atención en especialidades como clínica médica,
odontología, dermatología, psicología, nutrición, kinesiología y medicina estética. En esta
institución se coordina diariamente la atención de los pacientes con la disponibilidad de
distintos profesionales, consultorios, fechas y horarios.
Actualmente, la solicitud de turnos puede realizarse por teléfono, WhatsApp o de manera
presencial. Luego, el personal administrativo debe consultar la disponibilidad del profesional y
registrar el turno en una agenda o planilla. Cuando un paciente necesita cancelar o
reprogramar su turno, la información debe buscarse y actualizarse manualmente. Como los
datos pueden encontrarse distribuidos entre diferentes agendas, mensajes y archivos, resulta
difícil mantenerlos actualizados y disponibles para todos los involucrados.
Esta forma de trabajo puede generar turnos duplicados o superpuestos, errores en las fechas y
horarios, pérdida de información, demoras en la atención y dificultades para conocer la
disponibilidad real de cada profesional. También complica el seguimiento de los turnos
atendidos, cancelados, reprogramados o ausentes.
El problema afecta principalmente al personal administrativo, que debe dedicar tiempo a
buscar, registrar y verificar información. También afecta a los profesionales, porque necesitan
contar con una agenda actualizada, y a los pacientes, que pueden experimentar demoras para
obtener un turno o recibir información incorrecta sobre las fechas y los horarios disponibles.
El impacto del problema podría evaluarse mediante indicadores como el tiempo necesario
para asignar un turno, la cantidad de turnos superpuestos, el número de cancelaciones y
ausencias, los errores en la carga de datos y el tiempo utilizado para consultar las agendas.
Estos indicadores permitirían comprobar posteriormente si la solución implementada produce
una mejora real.
Por lo tanto, el problema central es la falta de una gestión centralizada y actualizada de los
turnos, pacientes, profesionales y especialidades. Una solución tecnológica podría aportar
valor al organizar la información en un mismo lugar, mostrar la disponibilidad de horarios,
impedir la asignación de turnos superpuestos y facilitar las cancelaciones o reprogramaciones.
De esta manera, no se buscaría solamente pasar una agenda de papel a un formato digital,
sino reducir errores, ahorrar tiempo y mejorar la organización de la atención.


## Validación del problema
El problema se considera relevante porque la gestión de los turnos influye directamente en la
organización diaria del centro de salud y en la calidad de la atención brindada a los pacientes.
Aunque existen soluciones parciales, como agendas, planillas y aplicaciones de mensajería,
estas herramientas no siempre permiten centralizar la información, controlar
automáticamente la disponibilidad ni evitar la superposición de turnos.
Además, el problema admite una solución tecnológica y resulta posible abordarlo dentro del
proyecto. Para que sea viable para un equipo de tres integrantes, la primera versión se
concentrará principalmente en la gestión de pacientes, profesionales, especialidades, horarios
y turnos. Las funcionalidades más complejas podrán quedar para futuras ampliaciones.

## Alcance del proyecto
El proyecto tendrá como alcance el desarrollo de una primera versión funcional de un sistema
de gestión de turnos para un centro de salud multidisciplinario de pequeña o mediana escala.
La solución estará orientada principalmente al personal administrativo y buscará centralizar en
un mismo sistema la información relacionada con pacientes, profesionales, especialidades,
horarios disponibles y turnos.
La primera versión permitirá registrar, consultar y actualizar los datos básicos de los pacientes
y profesionales del centro de salud, así como administrar las distintas especialidades y asociar
cada profesional con la especialidad correspondiente.
También permitirá definir los días y horarios de atención de cada profesional y consultar su
disponibilidad para una fecha determinada. A partir de esta información, el personal
administrativo podrá registrar nuevos turnos vinculando un paciente con un profesional, una
fecha y un horario. El sistema deberá controlar la disponibilidad antes de realizar la asignación,
evitando que se registren turnos superpuestos para un mismo profesional.
Los turnos registrados podrán consultarse desde una agenda centralizada y será posible
cancelarlos o reprogramarlos cuando sea necesario. Además, se podrá identificar su estado,
diferenciando al menos entre turnos pendientes, atendidos, cancelados y ausentes, facilitando
así el seguimiento de la atención.

De esta manera, el alcance de la primera versión comprenderá principalmente:
- Gestión de pacientes.
- Gestión de profesionales.
- Gestión de especialidades y su asociación con los profesionales.
- Configuración de días y horarios de atención de cada profesional.
- Consulta de disponibilidad.
- Asignación de turnos.
- Control de superposición de turnos.
- Consulta de la agenda de turnos.
- Cancelación y reprogramación de turnos.
- Registro y consulta del estado de los turnos.

## Fuera del alcance

Con el objetivo de mantener una primera versión viable de acuerdo con los tiempos, recursos
disponibles y tamaño del equipo, quedarán fuera del alcance inicial las funcionalidades que no
sean indispensables para resolver el problema principal identificado.

Por lo tanto, esta primera versión no incluirá:

- Gestión de historias clínicas, diagnósticos, recetas, estudios o tratamientos.
- Gestión de obras sociales, autorizaciones o coberturas médicas.
- Facturación, cobros o administración de pagos.
- Integraciones con WhatsApp, correo electrónico u otros servicios externos.
- Envío automático de recordatorios o notificaciones.
- Reserva o gestión autónoma de turnos por parte de los pacientes mediante un portal o
aplicación.
- Gestión de consultas virtuales o videollamadas.
- Reportes estadísticos o herramientas avanzadas de análisis.

Estas funcionalidades podrán considerarse como posibles ampliaciones futuras del sistema.

La primera versión se considerará funcional cuando permita realizar de manera centralizada - el
circuito básico de gestión de un turno:- registrar los datos necesarios -consultar la
disponibilidad de un profesional - asignar un turno sin generar superposiciones y
posteriormente consultarlo - cancelarlo- reprogramarlo o actualizar su estado.

## Stack Tecnológico

Para la primera versión del sistema optamos por un stack basado en Java, permitiendo aplicar
y reforzar los conocimientos adquiridos durante la carrera. Además, Java junto con el
framework Spring Boot resulta adecuado para sistemas de gestión con múltiples entidades
relacionadas entre sí, ya que ofrece herramientas para la persistencia de datos, la validación
de la información y el control de acceso por roles.

- **Backend**: Java con el framework Spring Boot, que provee una estructura basada en
controlar servicios y repositorios, facilitando la organización de la lógica de negocio.
- **Persistencia de datos**: Spring Data JPA, nos permite mapear las entidades del dominio
directamente a la base de datos, reduciendo el código repetitivo de acceso a datos.
- **Base de datos**: MySQL, motor de base de datos robusto y utilizado en Java.
- **Frontend**: React con TypeScript para la construcción de la interfaz, junto con Bootstrap (react-bootstrap) para el diseño y estilos de los componentes. Esto permite contar con componentes visuales ya probados (formularios, tablas, botones, navegación) y mantener una interfaz consistente sin invertir tiempo excesivo en diseño desde cero. React consume la información del backend a través de una API REST, manteniendo el frontend y el backend desacoplados.
-  **Seguridad:** Spring Security junto con JWT (JSON Web Tokens) para la autenticación y autorización de usuarios, controlando el acceso a los distintos endpoints según los roles Administrador, Recepcionista y Profesional. Las contraseñas se almacenarán de forma segura mediante un hash generado con BCrypt y nunca como texto visible. Después de iniciar sesión, el usuario recibirá un token JWT que deberá enviar en las solicitudes realizadas al backend. Además, se configurará CORS para permitir la comunicación con el frontend autorizado y se aplicarán validaciones de datos del lado del servidor mediante Bean Validation.
- **Control de versiones**: Git y GitHub, para la gestión colaborativa del código fuente y el
seguimiento de los cambios realizados por el equipo.
- **Despliegue**: vamos a utilizar plataformas cloud, con el objetivo de contar con un
entorno accesible para la demostración del proyecto.


## Repositorio


[Repositorio del proyecto en GitHub](https://github.com/CarlosAdrianJ/proyecto_clinica_medica)

## Estado del proyecto

Proyecto en etapa inicial de planificación y organización.

## 3. Arquitectura y módulos

### Arquitectura del sistema

El sistema utilizará una arquitectura cliente-servidor dividida en frontend, backend y base de datos.

-  **Frontend:** interfaz desarrollada con React, TypeScript y Vite. Para los estilos se utilizarán React-Bootstrap, Bootstrap y hojas CSS propias. Permitirá que el personal administrativo utilice las funciones del sistema y se comunicará con el backend mediante una API REST.
- **Backend:** aplicación desarrollada con Java y Spring Boot. Contendrá la lógica de negocio, las validaciones y la API REST.
- **Persistencia:** se utilizará Spring Data JPA para relacionar las entidades Java con las tablas de la base de datos.
- **Base de datos:** se utilizará MySQL para almacenar pacientes, profesionales, especialidades, horarios, usuarios y turnos.
- **Comunicación:** el frontend se comunicará con el backend mediante una API REST utilizando solicitudes HTTP.
- **Control de versiones:** el código y la documentación se administrarán mediante Git y GitHub.
- **Despliegue:** se utilizarán plataformas cloud para contar con un entorno accesible durante la demostración.

El backend estará organizado en las siguientes capas:

- **Controller:** recibe las solicitudes enviadas desde el frontend.
- **Service:** contiene la lógica de negocio y las validaciones.
- **Repository:** realiza las operaciones de acceso a la base de datos.
- **Model o Entity:** representa las entidades del sistema.
- **DTO:** transporta información entre el frontend y el backend.
- **Exception:** centraliza el manejo de errores.
- **Security:** controla la autenticación y los permisos según el rol del usuario.

### Módulos del sistema

Para organizar el desarrollo del sistema se definieron los siguientes módulos funcionales. Cada módulo agrupa las funcionalidades relacionadas con una parte específica de la gestión de turnos de la clínica.

#### 1. Módulo de pacientes

Este módulo permitirá administrar la información de los pacientes registrados en el sistema.
Las principales funcionalidades serán:
- Registrar nuevos pacientes.
- Consultar pacientes registrados.
- Buscar pacientes por sus datos identificatorios.
- Modificar los datos de un paciente.
- Realizar la baja lógica de un paciente mediante el campo activo.
  
Los datos principales administrados serán nombre, apellido, DNI, fecha de nacimiento, teléfono, correo electrónico y dirección.


#### 2. Módulo de profesionales

Permitirá administrar los profesionales que brindan atención en la clínica.
Las principales funcionalidades serán:
- Registrar profesionales.
- Consultar y buscar profesionales.
- Modificar sus datos.
- Registrar número de matrícula.
- Realizar la baja lógica de profesionales.
- Asociar uno o más profesionales con las especialidades correspondientes.
  
De esta manera, un profesional podrá encontrarse relacionado con una o más especialidades ofrecidas por la institución.


#### 3. Módulo de especialidades

Permitirá administrar las especialidades médicas disponibles en la clínica.
Las principales funcionalidades serán:
- Registrar nuevas especialidades.
- Consultar las especialidades disponibles.
- Modificar sus datos.
- Realizar su baja lógica.
- Asociar especialidades con los profesionales que las brindan.
  
La relación entre profesionales y especialidades será de muchos a muchos, permitiendo que un profesional pueda contar con más de una especialidad y que una especialidad pueda ser brindada por diferentes profesionales.


#### 4. Módulo de horarios de atención

Permitirá configurar los días y horarios en los que cada profesional se encuentra disponible para brindar atención.
Las principales funcionalidades serán:
- Registrar días de atención.
- Definir hora de inicio y finalización de cada jornada o bloque horario.
- Establecer la duración de los turnos.
- Consultar los horarios configurados para cada profesional.
- Modificar o desactivar horarios existentes.
  
Esta información será utilizada posteriormente para calcular la disponibilidad de turnos.


#### 5. Módulo de disponibilidad

Este módulo será responsable de determinar los horarios disponibles de un profesional para una fecha determinada.
Para ello tendrá en cuenta:
- Los días y horarios de atención configurados para el profesional.
- La duración establecida para los turnos.
- Los turnos que ya se encuentran registrados.
- La fecha solicitada.
  
El sistema deberá mostrar únicamente horarios disponibles y evitar que se asignen dos turnos al mismo profesional en la misma fecha y horario.


#### 6. Módulo de turnos

Será el módulo central del sistema y permitirá administrar el ciclo de vida de los turnos de la clínica.
Las principales funcionalidades serán:
- Registrar un nuevo turno.
- Asociar el turno con un paciente.
- Seleccionar un profesional y una especialidad válida para dicho profesional.
- Seleccionar fecha y horario.
- Validar la disponibilidad antes de registrar el turno.
- Consultar los turnos existentes.
- Cancelar turnos.
- Reprogramar turnos modificando su fecha u horario.
- Actualizar el estado de un turno.
  
Los estados contemplados inicialmente serán:
* PENDIENTE
* ATENDIDO
* CANCELADO
* AUSENTE
  
Además, el sistema deberá impedir la creación de turnos superpuestos para un mismo profesional.


#### 7. Módulo de agenda

Permitirá visualizar de manera organizada los turnos registrados en el sistema.
La agenda podrá consultarse principalmente utilizando criterios como:
- Profesional.
- Fecha.
- Estado del turno.
  
Esto permitirá al personal administrativo consultar rápidamente la planificación de atención y a los profesionales visualizar los turnos correspondientes a su agenda.


#### 8. Módulo de usuarios y control de acceso

Este módulo administrará el acceso de los usuarios al sistema.
Las principales funcionalidades serán:
- Autenticación mediante nombre de usuario y contraseña.
- Control de acceso mediante roles.
- Activación o desactivación de usuarios.
- Asociación de una cuenta con un profesional cuando corresponda.
- Restricción de funcionalidades según el rol del usuario.
  
Los roles contemplados inicialmente serán:
- Administrador: tendrá acceso a la administración general del sistema.
- Recepcionista: podrá gestionar pacientes, consultar disponibilidad y administrar turnos.
- Profesional: podrá consultar la información relacionada con su propia agenda.
  
La autenticación y autorización serán implementadas en el backend utilizando Spring Security y JWT.
Los pacientes formarán parte de la información administrada por el sistema, pero no tendrán acceso mediante una cuenta propia en esta primera versión. El portal de autogestión para pacientes podrá incorporarse como una ampliación futura.

### Estado de los módulos subidos al repositorio

El listado anterior describe los módulos planificados para el sistema. A continuación se distingue el código implementado y subido al repositorio del trabajo que aún está en desarrollo:

| Módulo | Estado al 15/09/2026 | Ubicación en el repositorio |
| --- | --- | --- |
| Pacientes | Implementado, probado e integrado en `main`. | `backend/src/main/java/com/clinica/medica/paciente/` |
| Profesionales | Código inicial subido a la rama de Fiorella; pendiente de ajustes, pruebas e integración en `main`. | `backend/src/main/java/com/clinica/medica/profesional/` en `feature/profesionales-fiorella` |
| Especialidades | Código subido a la rama de Valentina; pendiente de pruebas e integración en `main`. | `backend/src/main/java/com/clinica/medica/especialidad/` en `feature/especialidades-frontend-valentina` |
| Horarios, disponibilidad, turnos, agenda y usuarios | Planificados; aún no implementados como módulos funcionales. | Descritos en este README |

La configuración actual de Spring Security permite proteger y probar la API durante el desarrollo. La autenticación con usuarios, roles y JWT descrita en la arquitectura sigue pendiente de implementación.

### Esquema de la base de datos

La base de datos relacional `clinica_medica` fue diseñada para MySQL y contiene las siguientes tablas:

| Tabla | Responsabilidad |
|---|---|
| `usuarios` | Almacena los datos de acceso y el rol de cada usuario. |
| `pacientes` | Almacena la información personal y de contacto de los pacientes. |
| `profesionales` | Almacena los datos y la matrícula de los profesionales. |
| `especialidades` | Almacena las especialidades ofrecidas por la clínica. |
| `profesional_especialidad` | Representa la relación entre profesionales y especialidades. |
| `horarios_atencion` | Registra los días y horarios de atención de cada profesional. |
| `turnos` | Relaciona pacientes, profesionales, especialidades, fechas, horarios y estados. |

### Relaciones principales

- Un paciente puede tener varios turnos.
- Un profesional puede atender varios turnos.
- Un profesional puede tener una o más especialidades.
- Una especialidad puede estar asociada con varios profesionales.
- Un profesional puede tener varios horarios de atención.
- Cada turno corresponde a un paciente, un profesional y una especialidad.
- La base de datos impide dos turnos con el mismo profesional, fecha y hora de inicio. La validación de intervalos superpuestos y la reutilización de horarios de turnos cancelados quedan pendientes para el módulo de turnos.

### Archivos de base de datos

Los archivos se encuentran en la carpeta `database`:

- `schema.sql`: crea la base de datos, las tablas, las relaciones y las restricciones.
- `datos_pruebas.sql`: incorpora información de ejemplo.

En un entorno de pruebas, ejecutar primero `schema.sql` y luego `datos_pruebas.sql`.

### Precaución al ejecutar el esquema

`schema.sql` elimina las tablas existentes antes de recrearlas, por lo que borra sus datos. Utilizarlo únicamente en un entorno de desarrollo o pruebas sin información que se necesite conservar.

### Pruebas del módulo de pacientes

El servicio de pacientes cuenta con 12 pruebas unitarias integradas en `main`. Verifican consultas, creación, actualización, DNI duplicado, pacientes inexistentes y baja lógica.

Desde la raíz del proyecto:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml "-Dtest=PacienteServiceTest" test
```

Estas pruebas utilizan un repositorio simulado; no verifican la conexión con MySQL ni el funcionamiento completo de la API.