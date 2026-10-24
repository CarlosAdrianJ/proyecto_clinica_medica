import type { Especialidad, EspecialidadFormData } from '../types/especialidad';

const BASE_URL = '/api/especialidades';

function mensajePorDefecto(status: number): string {
  switch (status) {
    case 401:
      return 'La sesión venció o no es válida. Volvé a iniciar sesión.';
    case 403:
      return 'No tenés permiso para realizar esta operación.';
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
      const detalle = cuerpo?.message || cuerpo?.detail;
      if (typeof detalle === 'string' && detalle.trim()) {
        mensaje = detalle;
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

export function listarEspecialidades(token: string): Promise<Especialidad[]> {
  return fetch(BASE_URL, { headers: { Authorization: `Bearer ${token}` } }).then((res) => manejarRespuesta<Especialidad[]>(res));
}

export function crearEspecialidad(token: string, datos: EspecialidadFormData): Promise<Especialidad> {
  return fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(datos),
  }).then((res) => manejarRespuesta<Especialidad>(res));
}

export function actualizarEspecialidad(token: string, id: number, datos: EspecialidadFormData): Promise<Especialidad> {
  return fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(datos),
  }).then((res) => manejarRespuesta<Especialidad>(res));
}

export function eliminarEspecialidad(token: string, id: number): Promise<void> {
  return fetch(`${BASE_URL}/${id}`, { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } }).then((res) => manejarRespuesta<void>(res));
}
