import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
import { test } from 'node:test';
import ts from 'typescript';

const fuente = await readFile(new URL('../src/api/especialidades.ts', import.meta.url), 'utf8');
const { outputText } = ts.transpileModule(fuente, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
});
const api = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

const TOKEN = 'token-de-prueba';
const DATOS = { nombre: 'Cardiología', descripcion: 'Corazón y sistema circulatorio' };

test('todas las operaciones envían JWT y respetan endpoints, métodos y cuerpos', async t => {
  const casos = [
    [() => api.listarEspecialidades(TOKEN), '/api/especialidades', 'GET', undefined, 200],
    [() => api.crearEspecialidad(TOKEN, DATOS), '/api/especialidades', 'POST', DATOS, 201],
    [() => api.actualizarEspecialidad(TOKEN, 4, DATOS), '/api/especialidades/4', 'PUT', DATOS, 200],
    [() => api.eliminarEspecialidad(TOKEN, 4), '/api/especialidades/4', 'DELETE', undefined, 204],
  ];

  for (const [operacion, ruta, metodo, cuerpo, status] of casos) {
    t.mock.method(globalThis, 'fetch', async (url, opciones = {}) => {
      assert.equal(url, ruta);
      assert.equal(opciones.method ?? 'GET', metodo);
      assert.equal(opciones.headers.Authorization, `Bearer ${TOKEN}`);
      assert.equal(opciones.body, cuerpo ? JSON.stringify(cuerpo) : undefined);
      if (cuerpo) {
        assert.equal(opciones.headers['Content-Type'], 'application/json');
      }
      return status === 204
        ? new Response(null, { status })
        : Response.json(metodo === 'GET' ? [] : { id: 4, ...DATOS, activo: true }, { status });
    });
    await operacion();
    t.mock.restoreAll();
  }
});

test('devuelve los datos del backend', async t => {
  const lista = [{ id: 1, nombre: 'Clínica médica', descripcion: null, activo: true }];
  t.mock.method(globalThis, 'fetch', async () => Response.json(lista));
  assert.deepEqual(await api.listarEspecialidades(TOKEN), lista);
  t.mock.restoreAll();

  t.mock.method(globalThis, 'fetch', async () => Response.json({ id: 9, ...DATOS, activo: true }, { status: 201 }));
  const creada = await api.crearEspecialidad(TOKEN, DATOS);
  assert.equal(creada.id, 9);
  assert.equal(creada.nombre, DATOS.nombre);
});

test('la baja con 204 se resuelve sin intentar leer JSON', async t => {
  t.mock.method(globalThis, 'fetch', async () => new Response(null, { status: 204 }));
  assert.equal(await api.eliminarEspecialidad(TOKEN, 4), undefined);
});

test('errores sin cuerpo muestran un mensaje según el estado HTTP', async t => {
  const casos = [
    [401, /sesión venció/],
    [403, /No tenés permiso/],
    [404, /No se encontró la especialidad/],
    [409, /Ya existe una especialidad con ese nombre/],
    [500, /error al comunicarse con el servidor/],
  ];
  for (const [status, mensaje] of casos) {
    t.mock.method(globalThis, 'fetch', async () => new Response('', { status }));
    await assert.rejects(api.crearEspecialidad(TOKEN, DATOS), mensaje);
    t.mock.restoreAll();
  }
});

test('prioriza el mensaje del backend, ya venga en message o en detail', async t => {
  for (const campo of ['message', 'detail']) {
    t.mock.method(globalThis, 'fetch', async () =>
      Response.json({ [campo]: 'Ya existe otra especialidad con ese nombre' }, { status: 409 }));
    await assert.rejects(api.actualizarEspecialidad(TOKEN, 4, DATOS), /Ya existe otra especialidad/);
    t.mock.restoreAll();
  }
});

test('un mensaje vacío del backend no reemplaza al mensaje por defecto', async t => {
  t.mock.method(globalThis, 'fetch', async () => Response.json({ message: '   ' }, { status: 404 }));
  await assert.rejects(api.eliminarEspecialidad(TOKEN, 99), /No se encontró la especialidad/);
});

test('un error de red rechaza la operación', async t => {
  t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('Failed to fetch'); });
  await assert.rejects(api.listarEspecialidades(TOKEN));
});

test('un error de red muestra un mensaje comprensible', { todo: 'pendiente: hoy se muestra "Failed to fetch" tal cual' }, async t => {
  t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('Failed to fetch'); });
  await assert.rejects(api.listarEspecialidades(TOKEN), /No se pudo conectar/);
});
