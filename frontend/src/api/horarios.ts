import type {
  HorarioAtencion,
  HorarioAtencionRequest,
  ProfesionalHorario,
} from '../types/horario';

const BASE_URL = '/api/horarios-atencion';
const PROFESIONALES_URL = '/api/profesionales';

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'La sesión no es válida o ha vencido.';
    case 403:
      return 'No tiene permiso para realizar esta operación.';
    case 404:
      return 'El horario o profesional solicitado no existe.';
    case 409:
      return 'El horario se superpone con otro horario activo.';
    default:
      return 'No se pudo establecer comunicación con el servidor.';
  }
}

async function manejarRespuesta<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let mensaje = mensajePorEstado(response.status);

    try {
      const cuerpo = await response.json();
      const mensajeServidor = cuerpo?.message ?? cuerpo?.detail;

      if (
        typeof mensajeServidor === 'string' &&
        mensajeServidor.trim()
      ) {
        mensaje = mensajeServidor;
      }
    } catch {
      mensaje = mensajePorEstado(response.status);
    }

    throw new Error(mensaje);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

function encabezados(token: string): HeadersInit {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

export async function listarHorarios(
  token: string,
): Promise<HorarioAtencion[]> {
  const response = await fetch(BASE_URL, {
    headers: encabezados(token),
  });

  return manejarRespuesta<HorarioAtencion[]>(response);
}

export async function listarHorariosPorProfesional(
  token: string,
  profesionalId: number,
): Promise<HorarioAtencion[]> {
  const response = await fetch(
    `${BASE_URL}/profesional/${profesionalId}`,
    {
      headers: encabezados(token),
    },
  );

  return manejarRespuesta<HorarioAtencion[]>(response);
}

export async function crearHorario(
  token: string,
  datos: HorarioAtencionRequest,
): Promise<HorarioAtencion> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<HorarioAtencion>(response);
}

export async function actualizarHorario(
  token: string,
  id: number,
  datos: HorarioAtencionRequest,
): Promise<HorarioAtencion> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<HorarioAtencion>(response);
}

export async function eliminarHorario(
  token: string,
  id: number,
): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
    headers: encabezados(token),
  });

  return manejarRespuesta<void>(response);
}

export async function listarProfesionalesParaHorarios(
  token: string,
): Promise<ProfesionalHorario[]> {
  const response = await fetch(PROFESIONALES_URL, {
    headers: encabezados(token),
  });

  return manejarRespuesta<ProfesionalHorario[]>(response);
}