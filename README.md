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

- Backend: Java con el framework Spring Boot, que provee una estructura basada en
controlar servicios y repositorios, facilitando la organización de la lógica de negocio.
- Persistencia de datos: Spring Data JPA, nos permite mapear las entidades del dominio
directamente a la base de datos, reduciendo el código repetitivo de acceso a datos.
- Base de datos: MySQL, motor de base de datos robusto y utilizado en Java.
- Frontend: framework basado en JavaScript/Typescript, que consume la información del
backend a través de una API REST. Esto permite mantener el frontend y el backend
desacoplados y facilita el trabajo en paralelo dentro del equipo
- Control de versiones: Git y GitHub, para la gestión colaborativa del código fuente y el
seguimiento de los cambios realizados por el equipo.
- Despliegue: vamos a utilizar plataformas cloud, con el objetivo de contar con un
entorno accesible para la demostración del proyecto.


## Repositorio


[Repositorio del proyecto en GitHub](https://github.com/CarlosAdrianJ/proyecto_clinica_medica)

## Estado del proyecto

Proyecto en etapa inicial de planificación y organización.
