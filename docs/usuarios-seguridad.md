# Usuarios y seguridad

Implementado sobre Spring Security y JWT HS256. Las contrasenas se guardan con BCrypt (coste 12). El token dura 3600 segundos por defecto. No hay registro publico: solo ADMINISTRADOR administra cuentas.

## Configuracion y primer administrador

Usar el esquema existente de `database/schema.sql`; no se requieren cambios de tablas. La aplicacion mantiene `ddl-auto=validate` para MySQL. `datos_pruebas.sql` ya no inserta hashes ficticios.

En PowerShell, configurar antes de iniciar el backend:

```powershell
$env:DB_PASSWORD = '<clave de MySQL>'
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:BOOTSTRAP_ADMIN_USERNAME = 'admin'
$env:BOOTSTRAP_ADMIN_PASSWORD = '<clave inicial de al menos 8 caracteres>'
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Conservar JWT_SECRET en la configuracion privada del entorno para que los tokens sobrevivan a reinicios; cambiarlo invalida los tokens emitidos. Requiere al menos 32 bytes aleatorios codificados en Base64. `JWT_TTL_SECONDS` permite cambiar la vigencia.

El aprovisionamiento inicial solo crea un administrador cuando la tabla usuarios esta vacia y se proporcionan ambas credenciales. Quitar las variables BOOTSTRAP_ADMIN_* despues de crearlo. Si una base anterior contiene los hashes ficticios del antiguo archivo de ejemplos, corregir esas cuentas de prueba antes de usar este aprovisionamiento; no elimina ni sobrescribe usuarios existentes.

## API

- `POST /api/auth/login`: publico. Cuerpo `{"nombreUsuario":"admin","contrasena":"..."}`. Devuelve `token`, `tipo: "Bearer"`, `expiraEn` (segundos) y `usuario`.
- `GET /api/auth/me`: identidad de la cuenta autenticada, incluido profesionalId cuando corresponde.
- `GET /api/usuarios` y `GET /api/usuarios/{id}`: listado y consulta, solo administrador.
- `POST /api/usuarios`: alta, solo administrador; devuelve 201.
- `PUT /api/usuarios/{id}`: reemplaza nombre, contrasena, rol, asociacion y estado; solo administrador. Requiere todos los campos, incluida una nueva contrasena. Permite reactivar una cuenta con activo=true.
- `DELETE /api/usuarios/{id}`: baja logica, solo administrador; devuelve 204.

Alta o reemplazo:

```json
{
  "nombreUsuario": "laura.gomez",
  "contrasena": "CambiarEstaClave123!",
  "rol": "PROFESIONAL",
  "profesionalId": 1,
  "activo": true
}
```

El nombre admite 3 a 50 caracteres ASCII (letras, numeros, punto, guion y guion bajo) y se convierte a minusculas. La contrasena requiere 8 caracteres como minimo y como maximo 72 bytes UTF-8, limite de BCrypt. PROFESIONAL requiere un profesional existente y activo; los otros roles requieren profesionalId=null. La relacion permite varias cuentas por profesional, conforme al esquema existente. Las respuestas omiten contrasenas y hashes.

Enviar `Authorization: Bearer <token>` a los endpoints protegidos. Sin credenciales validas: 401. Cuenta autenticada sin permiso: 403. Datos invalidos: 400. Usuario inexistente: 404. Nombre duplicado: 409.

## Matriz de permisos

| Recurso | ADMINISTRADOR | RECEPCIONISTA | PROFESIONAL |
| --- | --- | --- | --- |
| Usuarios | Alta, consulta, reemplazo, baja | Denegado | Denegado |
| Pacientes | Gestion completa | Gestion completa | Denegado |
| Profesionales | Gestion completa | Consultas | Solo GET /api/profesionales/{id} propio |
| Especialidades | Gestion completa | Consultas | Consultas |
| Horarios de atencion | Gestion completa | Consultas | Solo GET /api/horarios-atencion/profesional/{profesionalId} propio |
| Disponibilidad | Consulta | Consulta | Consulta de su profesional |
| Identidad /api/auth/me | Propia | Propia | Propia |

Los endpoints no contemplados se rechazan por defecto. Turnos y agenda todavia no tienen endpoints implementados. Las restricciones de propiedad se verifican en el servidor.

Se valida firma, emisor y vigencia del JWT mediante el soporte oficial de [Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html). Cada solicitud consulta la cuenta en la base: bajas, cambios de rol y cambios de asociacion tienen efecto inmediato. Un profesional inactivo no puede iniciar sesion ni seguir usando su token. Cambiar solamente la contrasena no revoca tokens ya emitidos: vencen con su TTL; para revocar el acceso de inmediato, desactivar la cuenta. No se implementaron refresh tokens ni cierre de sesion en servidor.

## Pruebas automatizadas

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

Las pruebas usan H2 en memoria, sin MySQL ni DB_PASSWORD ni JWT_SECRET externos. Incluyen login real con BCrypt, JWT firmado, firma alterada, vencimiento, emisor incorrecto, credenciales incorrectas, matriz de roles, propiedad de recursos, duplicados, validaciones, gestion de cuentas y revocacion por baja. Las pruebas existentes de Profesionales se conservan.

## Postman

Importar `docs/usuarios-seguridad.postman_collection.json`. Configurar las variables `baseUrl`, `adminUsername` y `adminPassword`. Ejecutar la coleccion completa con Collection Runner sobre una base de pruebas. La coleccion guarda los tokens y los IDs automaticamente, crea cuentas y profesionales con nombres unicos y realiza bajas logicas al finalizar. Los registros permanecen como inactivos.

Tambien se puede ejecutar con Newman:

```powershell
npx --yes newman@6.2.1 run docs/usuarios-seguridad.postman_collection.json --env-var "baseUrl=http://localhost:8080" --env-var "adminUsername=admin" --env-var "adminPassword=<clave>"
```

Para un servidor desechable con H2 y dependencias de prueba:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:test-run "-Dspring-boot.run.arguments=--server.port=18080 --security.bootstrap.username=postman-admin --security.bootstrap.password=PostmanAdmin123!"
```

Estas credenciales solo corresponden a la base H2 desechable. Al cerrar ese proceso se pierde la base. Usar baseUrl=http://localhost:18080 en la coleccion.

## Resultado de verificacion (17/10/2026)

- 80 pruebas automatizadas aprobadas, sin errores ni omisiones: 55 pruebas previas y 25 nuevas (24 casos de integracion con MockMvc y una prueba HTTP real).
- Coleccion Postman ejecutada con Newman 6.2.1 contra http://localhost:18080: 34 solicitudes y 37 verificaciones aprobadas; cero fallos.
- Se verificaron respuestas 200/201/204 autorizadas y rechazos 400/401/403/404/409. La prueba HTTP cubre la conservacion de errores de negocio durante el despacho interno ERROR.
- Base usada para las pruebas: H2 en memoria, modo MySQL. No se ejecuto una validacion contra una instancia MySQL real.
- El servidor desechable se cerro al finalizar. No se guardan tokens ni contrasenas de la ejecucion en la coleccion versionada.
- Se corrigio el wrapper Maven de Windows para directorios .m2 sin destino de enlace, que causaban Cannot index into a null array al iniciar.

## Revision final (18/10/2026)

- Suite completa: 85 pruebas aprobadas, cero fallos, errores u omisiones. Incluye 30 pruebas del modulo de seguridad y 55 pruebas previas.
- Postman/Newman 6.2.1 sobre el backend local configurado con MySQL, puerto 8080: 41 solicitudes y 63 verificaciones aprobadas, cero fallos.
- La coleccion incluye ahora actualizacion de contrasena, rechazo de la anterior, reactivacion, usuario inexistente y validaciones sin exposicion de valores rechazados.
- Se fijaron las propiedades spring.web.error.* para no exponer trazas, mensajes internos ni errores con valores rechazados. Se agrego un manejador de validacion para Usuarios y Autenticacion.
- Se probaron el aprovisionamiento del primer administrador, la conservacion de cuentas existentes y el limite de 72 bytes UTF-8 de BCrypt.
- Consultas para verificar directamente hashes y asociaciones en Workbench: verificacion-usuarios-mysql.sql. Esa consulta y una comprobacion manual tras reiniciar el proceso quedan como verificaciones de base de datos a realizar por la usuaria; no se ejecutaron desde esta revision.
- Alcance: backend. El frontend existente todavia no tiene pantalla de login ni envio de tokens JWT.
- No se hizo staging, commit ni push. Ver el resumen en revision-usuarios-seguridad.md.
