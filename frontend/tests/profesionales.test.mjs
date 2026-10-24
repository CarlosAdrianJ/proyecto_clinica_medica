import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
import { test } from 'node:test';
import ts from 'typescript';

// Ejecuta el cliente TypeScript real, sin agregar un framework de pruebas.
const fuente = await readFile(new URL('../src/api/profesionales.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(fuente, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
});
const api = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
const fuenteUsuarios = await readFile(new URL('../src/api/usuarios.ts', import.meta.url), 'utf8');
const usuariosCompilados = ts.transpileModule(fuenteUsuarios, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
});
const usuarios = await import(`data:text/javascript;base64,${Buffer.from(usuariosCompilados.outputText).toString('base64')}`);

test('todas las operaciones envían JWT y respetan los endpoints y métodos del backend', async t => {
  const datos = { nombre: 'Nombre', apellido: 'Apellido', matricula: 'M-1', telefono: '', email: '' };
  const casos = [
    [() => api.listarProfesionales('token-de-prueba'), '/api/profesionales', 'GET'],
    [() => api.consultarProfesional('token-de-prueba', 7), '/api/profesionales/7', 'GET'],
    [() => api.crearProfesional('token-de-prueba', datos), '/api/profesionales', 'POST', datos],
    [() => api.actualizarProfesional('token-de-prueba', 7, datos), '/api/profesionales/7', 'PUT', datos],
    [() => api.desactivarProfesional('token-de-prueba', 7), '/api/profesionales/7', 'DELETE'],
    [() => api.asociarEspecialidad('token-de-prueba', 7, 3), '/api/profesionales/7/especialidades/3', 'POST'],
    [() => api.listarEspecialidadesParaProfesionales('token-de-prueba'), '/api/especialidades', 'GET'],
  ];
  for (const [operacion, ruta, method, cuerpo] of casos) {
    t.mock.method(globalThis, 'fetch', async (url, opciones) => {
      assert.equal(url, ruta);
      assert.equal(opciones.method, method);
      assert.equal(opciones.headers.Authorization, 'Bearer token-de-prueba');
      assert.equal(opciones.body, cuerpo ? JSON.stringify(cuerpo) : undefined);
      return method === 'DELETE' || ruta.includes('/especialidades/')
        ? new Response(null, { status: 204 })
        : Response.json([]);
    });
    await operacion();
    t.mock.restoreAll();
  }
});

test('conserva mensajes del backend tanto en detail como en message', async t => {
  for (const campo of ['message', 'detail']) {
    t.mock.method(globalThis, 'fetch', async () => Response.json({ [campo]: 'La especialidad ya está asociada.' }, { status: 409 }));
    await assert.rejects(api.asociarEspecialidad('token-de-prueba', 7, 3), /La especialidad ya está asociada/);
    t.mock.restoreAll();
  }
});

test('errores sin JSON y errores de red muestran mensajes comprensibles', async t => {
  t.mock.method(globalThis, 'fetch', async () => new Response('', { status: 403 }));
  await assert.rejects(api.listarProfesionales('token-de-prueba'), /No tenés permiso/);
  t.mock.restoreAll();
  t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('fetch failed'); });
  await assert.rejects(api.listarProfesionales('token-de-prueba'), /No se pudo conectar/);
});

test('Usuarios muestra el detalle de validación de Spring', async t => {
  t.mock.method(globalThis, 'fetch', async () => Response.json({ detail: 'Datos de usuario inválidos.' }, { status: 400 }));
  await assert.rejects(usuarios.listarUsuarios('token-de-prueba'), /Datos de usuario inválidos/);
});
