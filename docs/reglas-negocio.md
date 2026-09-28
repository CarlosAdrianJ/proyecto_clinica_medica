# Reglas de negocio

Las siguientes reglas definen las condiciones que debe respetar el Sistema de Gestión de Turnos para una Clínica Médica.

## Pacientes

1. **RN01:** Cada paciente debe identificarse mediante un DNI único.
2. **RN02:** No se debe permitir registrar dos pacientes con el mismo DNI.
3. **RN03:** La baja de un paciente debe ser lógica, modificando el campo `activo` a falso.
4. **RN04:** Un paciente inactivo debe conservarse para mantener el historial de sus turnos.
5. **RN05:** No se deben asignar nuevos turnos a pacientes inactivos.

## Profesionales

6. **RN06:** Cada profesional debe tener una matrícula única.
7. **RN07:** No se debe permitir registrar dos profesionales con la misma matrícula.
8. **RN08:** La baja de un profesional debe ser lógica.
9. **RN09:** Un profesional inactivo debe conservarse para mantener el historial de atención.
10. **RN10:** No se deben asignar nuevos horarios ni turnos a profesionales inactivos.

## Especialidades

11. **RN11:** El nombre de cada especialidad debe ser único.
12. **RN12:** La baja de una especialidad debe ser lógica.
13. **RN13:** Un profesional puede estar asociado con una o más especialidades.
14. **RN14:** Una especialidad puede estar asociada con varios profesionales.
15. **RN15:** No se debe repetir la misma asociación entre un profesional y una especialidad.
16. **RN16:** Un turno solo puede asignarse a un profesional en una especialidad que tenga habilitada.

## Horarios de atención

17. **RN17:** Todo horario de atención debe pertenecer a un profesional existente y activo.
18. **RN18:** La hora de finalización debe ser posterior a la hora de inicio.
19. **RN19:** La duración establecida para los turnos debe ser mayor que cero.
20. **RN20:** Los turnos deben programarse dentro de los días y horarios de atención del profesional.
21. **RN21:** Los horarios desactivados no deben utilizarse para calcular disponibilidad.

## Turnos y disponibilidad

22. **RN22:** Todo turno debe relacionarse con un paciente, un profesional y una especialidad.
23. **RN23:** El paciente y el profesional deben estar activos al momento de crear el turno.
24. **RN24:** No se deben permitir turnos superpuestos para un mismo profesional.
25. **RN25:** La hora de finalización del turno debe ser posterior a su hora de inicio.
26. **RN26:** La duración del turno debe respetar la duración definida en el horario de atención.
27. **RN27:** Un horario ocupado por un turno pendiente no debe mostrarse como disponible.
28. **RN28:** Un horario correspondiente a un turno cancelado puede volver a estar disponible.
29. **RN29:** La reprogramación debe validar nuevamente la disponibilidad del profesional.
30. **RN30:** La base de datos impide que existan dos turnos con el mismo profesional, fecha y hora de inicio.
31. **RN31:** La validación de intervalos parcialmente superpuestos debe realizarse en la lógica de negocio.

## Estados de los turnos

32. **RN32:** Los estados permitidos son `PENDIENTE`, `ATENDIDO`, `CANCELADO` y `AUSENTE`.
33. **RN33:** Todo turno nuevo debe crearse con estado `PENDIENTE`.
34. **RN34:** Un turno pendiente puede pasar a atendido, cancelado o ausente.
35. **RN35:** Los estados atendido, cancelado y ausente se consideran estados finales.
36. **RN36:** Un turno finalizado no debe volver directamente al estado pendiente.
37. **RN37:** Si se necesita recuperar un turno cancelado, se debe generar o reprogramar mediante una operación que vuelva a validar la disponibilidad.

| Estado actual | Estados permitidos |
|---|---|
| `PENDIENTE` | `ATENDIDO`, `CANCELADO`, `AUSENTE` |
| `ATENDIDO` | Sin transición |
| `CANCELADO` | Sin transición directa |
| `AUSENTE` | Sin transición |

## Usuarios y seguridad

38. **RN38:** Los nombres de usuario deben ser únicos.
39. **RN39:** Las contraseñas nunca deben almacenarse en texto plano.
40. **RN40:** Los usuarios deben acceder únicamente a las funciones autorizadas para su rol.
41. **RN41:** Los roles previstos son administrador, recepcionista y profesional.
42. **RN42:** Un usuario con rol profesional puede estar vinculado con un profesional.
43. **RN43:** Los usuarios desactivados no deben poder iniciar sesión.
44. **RN44:** La baja de usuarios debe ser lógica para conservar la trazabilidad de las operaciones.

## Integridad y conservación de datos

45. **RN45:** Los pacientes, profesionales y especialidades utilizados en operaciones anteriores no deben eliminarse físicamente.
46. **RN46:** Las relaciones deben respetar las claves foráneas definidas en la base de datos.
47. **RN47:** El historial de turnos debe conservarse aunque un paciente, profesional o especialidad se encuentre inactivo.
48. **RN48:** Las validaciones realizadas por la base de datos deben complementarse con validaciones en la capa de servicios.