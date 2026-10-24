# Frontend de Profesionales y revisión de Seguridad

Rama: `feature/profesionales-frontend-fiorella`, creada desde `main` actualizada.

## Integración para Carlos

Se exporta por defecto `ProfesionalesPage` desde
`frontend/src/components/profesionales/ProfesionalesPage.tsx`.
Recibe `token: string` y `usuario: UsuarioAutenticado` (el usuario de la sesión).
Al integrarla, usar `<ProfesionalesPage token={token} usuario={usuario} />`.
`App.tsx` ya integra la pantalla para Administrador y Recepcionista. El Profesional accede únicamente a Mi agenda. Ver [integración y verificación del frontend](integracion-frontend.md).

- Administrador: lista, crea, edita, desactiva y asocia especialidades activas.
- Recepcionista: consulta la lista sin acciones de modificación.
- Profesional: consulta exclusivamente `/api/profesionales/{usuario.profesionalId}`;
  no se solicita la lista general ni se muestran acciones administrativas.
- Los formularios respetan los límites del DTO del backend, validan campos
  obligatorios y correo, y recortan espacios antes de guardar.
- Todas las consultas y modificaciones del módulo envían el JWT, incluido el
  catálogo de especialidades. Se utilizan los endpoints existentes.
- Hay indicadores de carga, bloqueo durante el guardado, confirmación de baja,
  mensajes de éxito, errores del backend y reintento de la lista.
- El backend devuelve las especialidades asociadas en `ProfesionalResponse`, con id, nombre y estado. La tabla las muestra, identifica las inactivas y actualiza la asociación al completar el POST. El selector excluye las ya asociadas.

## Revisión de Usuarios y Seguridad

Se revisaron `App.tsx`, Usuarios, el cliente API, autenticación y permisos del backend.
Usuarios sólo aparece para administrador y Turnos utiliza la agenda propia para
el rol profesional. Crear/editar permite elegir estado y asociar un profesional
activo cuando corresponde. El contrato actual exige ingresar contraseña al editar;
por lo tanto, la reactivación desde el formulario también requiere contraseña.

Se corrigió `api/usuarios.ts` para mostrar también `detail` de las respuestas de
Spring, además de `message`. No se modificó el contrato del backend.

La suite de integración existente verifica permisos, duplicados, asociaciones,
rechazo de cuentas inactivas, revocación de JWT y agenda propia. Se añadió un caso
que comprueba reactivación, cambio de contraseña, rechazo de la contraseña anterior
y acceso con la nueva.

**Integración completada:** Profesionales y Especialidades aparecen para Administrador y Recepcionista; sólo el Administrador puede modificar estos catálogos. El Profesional ve únicamente Mi agenda y el renderizado impide abrir los otros módulos desde el estado de navegación. El backend mantiene sus controles de acceso existentes.

## Verificación

- Backend: 102 pruebas, sin fallos, errores ni omisiones.
- `npm.cmd --prefix .\frontend run build`: aprobado (TypeScript y Vite).
- `npm.cmd --prefix .\frontend run lint`: aprobado.
- Pruebas del cliente: `node --test frontend/tests/profesionales.test.mjs`.
  Cuatro pruebas aprobadas. Comprueban JWT en las siete operaciones, métodos,
  rutas, cuerpos, respuestas sin contenido, errores de red, errores del backend
  y lectura de `detail` en Usuarios.
- `git diff --check`: sin errores.
- Se reparó la instalación local con `npm ci --ignore-scripts --no-audit --no-fund`;
  no se modificaron `package.json` ni `package-lock.json`.
- La navegación ya está integrada. La comprobación visual con sesiones reales sigue pendiente porque esta sesión no dispone de un navegador conectado. El circuito de integración se comprueba con JWT y H2; ver [detalle y pasos manuales](integracion-frontend.md).

La carpeta `.vscode/` era preexistente y se deja fuera de esta entrega.
No se añadieron credenciales reales ni datos privados; las credenciales de las
pruebas automatizadas corresponden exclusivamente a la base H2 de pruebas.

## Archivos de la entrega

- `frontend/src/types/profesional.ts` (nuevo).
- `frontend/src/api/profesionales.ts` (nuevo).
- `frontend/src/components/profesionales/ProfesionalesPage.tsx` (nuevo).
- `frontend/src/api/usuarios.ts` (corrección de mensajes).
- `frontend/tests/profesionales.test.mjs` (nuevo).
- `backend/src/test/java/com/clinica/medica/security/SeguridadIntegrationTest.java`
  (prueba adicional).
- `docs/profesionales-frontend-fiorella.md` (esta guía).
