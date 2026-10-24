import type { Profesional, ProfesionalRequest } from '../types/profesional';
import type { Especialidad } from '../types/especialidad';

const URL = '/api';

async function solicitar<T>(token: string, ruta: string, method = 'GET', datos?: ProfesionalRequest): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await fetch(`${URL}${ruta}`, {
      method,
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      ...(datos ? { body: JSON.stringify(datos) } : {}),
    });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Intentá nuevamente.');
  }
  if (!respuesta.ok) {
    const mensajes: Record<number, string> = {
      400: 'Revisá los datos ingresados.',
      401: 'La sesión venció o no es válida. Volvé a iniciar sesión.',
      403: 'No tenés permiso para realizar esta operación.',
      404: 'El profesional o la especialidad no existe.',
      409: 'La matrícula ya existe o la especialidad ya está asociada.',
    };
    let mensaje = mensajes[respuesta.status] ?? 'No se pudo completar la operación.';
    try {
      const cuerpo = await respuesta.json();
      for (const detalle of [cuerpo.message, cuerpo.detail]) {
        if (typeof detalle === 'string' && detalle.trim()) { mensaje = detalle; break; }
      }
    } catch { /* Se conserva el mensaje correspondiente al estado HTTP. */ }
    throw new Error(mensaje);
  }
  if (respuesta.status === 204) return undefined as T;
  return respuesta.json() as Promise<T>;
}

export const listarProfesionales = (token: string) => solicitar<Profesional[]>(token, '/profesionales');
export const consultarProfesional = (token: string, id: number) => solicitar<Profesional>(token, `/profesionales/${id}`);
export const crearProfesional = (token: string, datos: ProfesionalRequest) => solicitar<Profesional>(token, '/profesionales', 'POST', datos);
export const actualizarProfesional = (token: string, id: number, datos: ProfesionalRequest) => solicitar<Profesional>(token, `/profesionales/${id}`, 'PUT', datos);
export const desactivarProfesional = (token: string, id: number) => solicitar<void>(token, `/profesionales/${id}`, 'DELETE');
export const reactivarProfesional = (token: string, id: number) => solicitar<Profesional>(token, `/profesionales/${id}/reactivar`, 'PATCH');
export const asociarEspecialidad = (token: string, id: number, especialidadId: number) => solicitar<void>(token, `/profesionales/${id}/especialidades/${especialidadId}`, 'POST');
export const listarEspecialidadesParaProfesionales = (token: string) => solicitar<Especialidad[]>(token, '/especialidades');
