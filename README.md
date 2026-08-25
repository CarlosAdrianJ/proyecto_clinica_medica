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

Actualmente, la solicitud de turnos puede realizarse por teléfono, WhatsApp o de manera presencial. Luego, el personal administrativo debe consultar la disponibilidad del profesional y registrar el turno en una agenda o planilla. Cuando un paciente necesita cancelar o reprogramar su turno, la información debe buscarse y actualizarse manualmente.

Como los datos pueden encontrarse distribuidos entre distintas agendas, mensajes y archivos, resulta difícil mantenerlos actualizados y disponibles para todos los involucrados. Esta forma de trabajo puede generar:

- Turnos duplicados o superpuestos.
- Errores en las fechas y los horarios.
- Pérdida de información.
- Demoras en la atención.
- Dificultades para conocer la disponibilidad real de cada profesional.
- Problemas para realizar el seguimiento de turnos atendidos, cancelados, reprogramados o ausentes.

El problema afecta principalmente al personal administrativo, pero también a los profesionales, que necesitan contar con una agenda actualizada, y a los pacientes, que pueden experimentar demoras o recibir información incorrecta.

Por lo tanto, el problema central es la falta de una gestión centralizada y actualizada de los turnos, pacientes, profesionales y especialidades. El sistema buscará organizar la información en un mismo lugar, mostrar la disponibilidad de horarios, evitar la asignación de turnos superpuestos y facilitar las cancelaciones y reprogramaciones.

## Validación del problema

El problema se considera relevante porque la gestión de los turnos influye directamente en la organización diaria del centro de salud y en la calidad de la atención brindada a los pacientes.

Aunque existen soluciones parciales, como agendas, planillas y aplicaciones de mensajería, estas herramientas no siempre permiten centralizar la información, controlar automáticamente la disponibilidad ni evitar la superposición de turnos.

El problema admite una solución tecnológica y puede abordarse dentro del proyecto. Para que sea viable para un equipo de tres integrantes, la primera versión se concentrará en las funciones principales. Las características más complejas podrán incorporarse en futuras ampliaciones.

## Objetivo general

Desarrollar un sistema web que permita administrar de manera centralizada los pacientes, profesionales, especialidades, horarios y turnos de un centro de salud, reduciendo errores y mejorando la organización de la atención.

## Alcance de la primera versión

La primera versión incluirá:

- Gestión de pacientes.
- Gestión de profesionales.
- Gestión de especialidades y asociación con los profesionales.
- Configuración de los días y horarios de atención de cada profesional.
- Consulta de disponibilidad.
- Asignación de turnos.
- Control de superposición de turnos.
- Consulta de una agenda centralizada.
- Cancelación y reprogramación de turnos.
- Registro y consulta del estado de los turnos: pendiente, atendido, cancelado o ausente.

La versión se considerará funcional cuando permita completar de forma centralizada el circuito básico de un turno: registrar los datos necesarios, consultar la disponibilidad de un profesional, asignar el turno sin generar superposiciones y luego consultarlo, cancelarlo, reprogramarlo o actualizar su estado.

## Fuera del alcance inicial

Para mantener una primera versión viable, no se incluirán inicialmente:

- Historias clínicas, diagnósticos, recetas, estudios o tratamientos.
- Obras sociales, autorizaciones o coberturas médicas.
- Facturación, cobros o administración de pagos.
- Integraciones con WhatsApp, correo electrónico u otros servicios externos.
- Recordatorios o notificaciones automáticas.
- Reserva autónoma de turnos por parte de los pacientes.
- Consultas virtuales o videollamadas.
- Reportes estadísticos o herramientas avanzadas de análisis.

Estas funcionalidades podrán considerarse como mejoras futuras.

## Stack tecnológico

- **Backend:** Java con Spring Boot, organizado en controladores, servicios y repositorios.
- **Persistencia:** Spring Data JPA para el mapeo de las entidades y el acceso a los datos.
- **Base de datos:** MySQL.
- **Frontend:** React con TypeScript, HTML5 y CSS3.
- **Comunicación:** API REST con intercambio de información en formato JSON.
- **Control de versiones:** Git y GitHub.
- **Pruebas de la API:** Postman.
- **Despliegue:** plataforma cloud a definir para disponer de un entorno accesible durante la demostración.

## Plan de trabajo

### Etapa 1: análisis y organización

- Definir el problema, el objetivo y el alcance del proyecto.
- Identificar las entidades principales y sus relaciones.
- Organizar las tareas entre los tres integrantes.
- Preparar el repositorio y acordar la forma de trabajo con Git y GitHub.

### Etapa 2: diseño

- Diseñar el modelo de datos.
- Definir la estructura del backend y los endpoints de la API REST.
- Preparar bocetos de las pantallas principales.
- Establecer las reglas para asignar, cancelar y reprogramar turnos.

### Etapa 3: desarrollo del backend

- Configurar el proyecto con Java y Spring Boot.
- Implementar las entidades, repositorios, servicios y controladores.
- Conectar el sistema con MySQL.
- Incorporar validaciones y el control de superposición de turnos.
- Probar los endpoints con Postman.

### Etapa 4: desarrollo del frontend

- Crear las pantallas para pacientes, profesionales, especialidades y turnos.
- Implementar los formularios y las consultas de información.
- Conectar el frontend con la API REST.
- Mostrar mensajes claros de confirmación y error.

### Etapa 5: integración y pruebas

- Integrar el frontend, el backend y la base de datos.
- Probar el circuito completo de asignación de turnos.
- Verificar cancelaciones, reprogramaciones, estados y disponibilidad.
- Corregir errores y revisar que se cumpla el alcance definido.

### Etapa 6: documentación y presentación

- Actualizar el README y la documentación técnica.
- Preparar datos de prueba para la demostración.
- Desplegar la aplicación en una plataforma cloud.
- Realizar una prueba final y preparar la presentación del proyecto.

## Organización del trabajo en GitHub

El proyecto utilizará un único repositorio compartido. Cada integrante trabajará en ramas separadas para las tareas asignadas y, una vez revisados los cambios, estos se incorporarán a la rama principal. GitHub permitirá registrar los avances, mantener el historial de modificaciones y facilitar el trabajo colaborativo.

## Indicadores para evaluar la solución

La mejora producida por el sistema podrá evaluarse mediante:

- Tiempo necesario para asignar un turno.
- Cantidad de turnos superpuestos.
- Cantidad de errores en la carga de datos.
- Número de cancelaciones y ausencias.
- Tiempo utilizado para consultar las agendas.

## Repositorio

[Repositorio del proyecto en GitHub](https://github.com/CarlosAdrianJ/proyecto_clinica_medica)

## Estado del proyecto

Proyecto en etapa inicial de planificación y organización.
