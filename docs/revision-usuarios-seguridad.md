# Revision de Usuarios y Seguridad

Fecha: 18/10/2026. Alcance: cambios locales del backend y su documentacion, antes del commit.

## Resultado

| Verificacion | Resultado |
| --- | --- |
| Suite Maven completa | 85 pruebas; 0 fallos, 0 errores, 0 omisiones; BUILD SUCCESS |
| Seguridad | 30 pruebas: 25 casos MockMvc, 1 HTTP real, 4 del administrador inicial |
| Pruebas previas | 55 aprobadas, incluyendo 18 de Profesionales |
| Coleccion Postman mediante Newman 6.2.1 | 41 solicitudes y 63 verificaciones; 0 fallos |
| Backend para Newman | http://localhost:8080, configurado con MySQL por la usuaria |
| Formato de cambios | git diff --check sin errores |

## Correcciones realizadas durante la revision

- Evitar trazas de Java en respuestas de error, incluso al ejecutar con DevTools. Propiedades spring.web.error.* segun la [documentacion de Spring Boot](https://docs.spring.io/spring-boot/appendix/application-properties/index.html).
- Responder a validaciones de Usuarios y Login sin devolver ni registrar los valores rechazados de las contrasenas.
- Agregar pruebas del primer administrador y del limite en bytes de BCrypt.
- Ampliar Postman con cambio de contrasena, reactivacion y comprobacion de que los errores no incluyen trazas ni valores rechazados.

La coleccion crea registros de prueba con nombres unicos y deja esas cuentas, profesionales y pacientes desactivados al finalizar. No modifica las cuentas originales de la usuaria.

## Limites de la verificacion

La API se verifico contra el servidor que la usuaria inicio con MySQL. No se accedio directamente a MySQL con credenciales de base de datos desde las herramientas de revision. Para comprobar los hashes y las asociaciones directamente en Workbench, ejecutar verificacion-usuarios-mysql.sql. Para confirmar persistencia tras un reinicio completo del proceso, guardar el listado, reiniciar spring-boot:run y comparar los IDs y nombres.

Los tokens vencen segun su TTL. La cuenta y su rol se consultan en cada solicitud; la baja impide el acceso mientras la cuenta siga inactiva. Cambiar solo la contrasena no invalida tokens previos. No hay refresh tokens ni cierre de sesion en servidor. El frontend aun no integra login ni envio del JWT; esta entrega corresponde al backend solicitado.

## Archivos para el futuro commit

Incluir los cambios revisados de:

- README.md.
- backend/pom.xml y backend/mvnw.cmd.
- backend/src/main/java/com/clinica/medica/config/SecurityConfig.java.
- Los tres controladores modificados: Profesionales, Horarios de Atencion y Disponibilidad.
- backend/src/main/java/com/clinica/medica/usuario/.
- backend/src/main/java/com/clinica/medica/security/.
- backend/src/main/resources/application.properties.
- backend/src/test/java/com/clinica/medica/security/ y backend/src/test/resources/.
- database/datos_pruebas.sql.
- docs/usuarios-seguridad.md, docs/usuarios-seguridad.postman_collection.json, docs/verificacion-usuarios-mysql.sql y este resumen.

No incluir .vscode/: ya existia y es configuracion local ajena al cambio. Tampoco incluir .postman/ ni backend/target/: contienen ejecuciones locales, reportes o archivos generados y ya estan ignorados por Git. Los reportes JSON locales pueden contener tokens y no forman parte de la entrega.

Las credenciales administradoras quedan vacias en la coleccion publicada. JWT_SECRET y DB_PASSWORD se toman del entorno; la clave JWT fija de src/test/resources es exclusiva de la base de pruebas H2.

Estado: codigo revisado y pruebas aprobadas para preparar el commit. No se hizo git add, commit ni push.
