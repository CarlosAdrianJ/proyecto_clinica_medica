# Integración y verificación del frontend

Actualización: 24/10/2026. Cambios locales, sin commit ni push.

## Navegación y permisos

| Rol | Pantallas |
| --- | --- |
| Administrador | Turnos, Horarios, Disponibilidad, Pacientes, Profesionales, Especialidades y Usuarios. |
| Recepcionista | Turnos, Horarios, Disponibilidad, Pacientes, Profesionales y Especialidades. |
| Profesional | Únicamente Mi agenda. |

Profesionales y Especialidades están conectadas en `App.tsx`. El renderizado también restringe al Profesional a su agenda, aunque el estado de navegación apunte a otro módulo. Esto no modifica los permisos existentes del backend.

Sólo el Administrador crea, edita o desactiva Profesionales, Especialidades y Horarios. Recepcionista puede consultar estos catálogos, gestionar Pacientes y operar Turnos. Usuarios queda reservado al Administrador.

Especialidades envía el JWT en sus cuatro operaciones. Los formularios respetan los límites del backend y muestran los mensajes `message` o `detail`.

Profesionales muestra las especialidades asociadas, identificando las inactivas. `ProfesionalResponse` agrega el campo `especialidades` con id, nombre y estado; se conservan sus campos anteriores y los endpoints existentes. Después de asociar, la tabla se actualiza y el selector excluye las especialidades ya asociadas. Es necesario reiniciar el backend para recibir el nuevo campo.

Los profesionales inactivos muestran la acción **Reactivar** únicamente al Administrador, con confirmación y actualización del estado en la tabla. Usa `PATCH /api/profesionales/{id}/reactivar`, protegido para Administrador; conserva el mismo registro, datos, especialidades y horarios. No reactiva por separado usuarios, horarios o especialidades que también hayan sido desactivados. Repetir la operación sobre un profesional activo conserva su estado; un id inexistente devuelve 404.

## Verificación automatizada

La prueba `SeguridadIntegrationTest.circuitoCompletoDeTurnosConJwtYAgendaPropia` usa Spring, persistencia H2 temporal, MockMvc y JWT obtenidos mediante login. Comprueba:

1. Crear Especialidad y Profesional como Administrador.
2. Asociar la especialidad y consultarla como Recepcionista.
3. Configurar horario y registrar un Paciente.
4. Consultar disponibilidad y reservar un Turno como Recepcionista.
5. Rechazar una reserva superpuesta y descontar la franja ocupada.
6. Mostrar el turno en la agenda del profesional asociado y excluirlo de otra agenda.
7. Reprogramar y cancelar el turno.
8. Recuperar la disponibilidad y reservar nuevamente la franja liberada.

Resultados del 24/10/2026: 103 pruebas del backend aprobadas, 4 pruebas del cliente aprobadas, compilación TypeScript/Vite y ESLint aprobados. Estas verificaciones no modifican MySQL y no equivalen a una prueba visual.

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
npm.cmd --prefix .\frontend run build
npm.cmd --prefix .\frontend run lint
node --test frontend/tests/profesionales.test.mjs
git diff --check
```

## Comprobación visual pendiente

Esta sesión no dispone de navegador conectado. No se verificaron clics, distribución visual ni sesiones reales en MySQL. Para completar esa comprobación en un entorno de desarrollo con datos descartables:

1. Iniciar MySQL y ejecutar `powershell -ExecutionPolicy Bypass -File .\iniciar-desarrollo.ps1` desde la raíz del proyecto.
2. Como Administrador, cargar una Especialidad y un Profesional; asociarlos y comprobar la columna Especialidades, incluso después de recargar.
3. Crear un horario futuro de ese Profesional y verificar Disponibilidad.
4. Como Recepcionista, comprobar que no aparecen acciones de modificación en Especialidades y Profesionales; registrar un Paciente y reservar un Turno.
5. Intentar repetir el horario ocupado; verificar el rechazo. Reprogramar y cancelar, comprobando la disponibilidad liberada.
6. Como Profesional vinculado, comprobar que el menú sólo ofrece Mi agenda y muestra únicamente sus turnos.
7. Cerrar sesión y volver a ingresar con cada rol, verificando que no persista la pantalla del usuario anterior.

El alcance de esta integración no incluye despliegue ni configuración definitiva de CORS. La consulta de especialidades asociadas se ofrece desde Profesionales.
