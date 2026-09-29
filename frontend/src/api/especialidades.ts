import type { Especialidad, EspecialidadFormData } from '../types/especialidad';

const BASE_URL = '/api/especialidades';

function mensajePorDefecto(status: number): string {
  switch (status) {
    case 404:
      return 'No se encontró la especialidad.';
    case 409:
      return 'Ya existe una especialidad con ese nombre.';
    default:
      return 'Ocurrió un error al comunicarse con el servidor.';
  }
}

async function manejarRespuesta<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let mensaje = mensajePorDefecto(response.status);
    try {
      const cuerpo = await response.json();
      if (cuerpo?.message) {
        mensaje = cuerpo.message;
      }
    } catch {
      // el backend no devolvió un cuerpo JSON legible; nos quedamos con el mensaje por defecto
    }
    throw new Error(mensaje);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function listarEspecialidades(): Promise<Especialidad[]> {
  return fetch(BASE_URL).then((res) => manejarRespuesta<Especialidad[]>(res));
}

export function crearEspecialidad(datos: EspecialidadFormData): Promise<Especialidad> {
  return fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos),
  }).then((res) => manejarRespuesta<Especialidad>(res));
}

export function actualizarEspecialidad(id: number, datos: EspecialidadFormData): Promise<Especialidad> {
  return fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos),
  }).then((res) => manejarRespuesta<Especialidad>(res));
}

export function eliminarEspecialidad(id: number): Promise<void> {
  return fetch(`${BASE_URL}/${id}`, { method: 'DELETE' }).then((res) => manejarRespuesta<void>(res));
}
