import type {
  EstadoTurno,
  FiltrosTurno,
  ReprogramarTurnoRequest,
  Turno,
  TurnoRequest,
} from '../types/turno';

const BASE_URL = '/api/turnos';

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'La sesión no es válida o ha vencido.';
    case 403:
      return 'No tiene permiso para realizar esta operación.';
    case 404:
      return 'El turno solicitado no existe.';
    case 409:
      return 'La operación no puede realizarse por un conflicto de disponibilidad.';
    default:
      return 'Ocurrió un error al comunicarse con el servidor.';
  }
}

async function manejarRespuesta<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let mensaje = mensajePorEstado(response.status);

    try {
      const cuerpo = await response.json();
      if (typeof cuerpo?.message === 'string' && cuerpo.message.trim()) {
        mensaje = cuerpo.message;
      }
    } catch {
      // Se conserva el mensaje correspondiente al estado HTTP.
    }

    throw new Error(mensaje);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

function cabeceras(token: string, conContenido = false): HeadersInit {
  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`,
  };

  if (conContenido) {
    headers['Content-Type'] = 'application/json';
  }

  return headers;
}

function crearConsulta(filtros: FiltrosTurno): string {
  const parametros = new URLSearchParams();

  if (filtros.pacienteId !== undefined) {
    parametros.set('pacienteId', filtros.pacienteId.toString());
  }

  if (filtros.profesionalId !== undefined) {
    parametros.set('profesionalId', filtros.profesionalId.toString());
  }

  if (filtros.fecha) {
    parametros.set('fecha', filtros.fecha);
  }

  if (filtros.estado) {
    parametros.set('estado', filtros.estado);
  }

  const consulta = parametros.toString();
  return consulta ? `?${consulta}` : '';
}

export function listarTurnos(
  token: string,
  filtros: FiltrosTurno = {},
): Promise<Turno[]> {
  return fetch(`${BASE_URL}${crearConsulta(filtros)}`, {
    headers: cabeceras(token),
  }).then((response) => manejarRespuesta<Turno[]>(response));
}

export function consultarMiAgenda(
  token: string,
  filtros: Partial<Pick<FiltrosTurno, 'fecha' | 'estado'>> = {},
): Promise<Turno[]> {
  return fetch(`${BASE_URL}/mi-agenda${crearConsulta(filtros)}`, {
    headers: cabeceras(token),
  }).then((response) => manejarRespuesta<Turno[]>(response));
}

export function buscarTurno(token: string, id: number): Promise<Turno> {
  return fetch(`${BASE_URL}/${id}`, {
    headers: cabeceras(token),
  }).then((response) => manejarRespuesta<Turno>(response));
}

export function crearTurno(
  token: string,
  datos: TurnoRequest,
): Promise<Turno> {
  return fetch(BASE_URL, {
    method: 'POST',
    headers: cabeceras(token, true),
    body: JSON.stringify(datos),
  }).then((response) => manejarRespuesta<Turno>(response));
}

export function reprogramarTurno(
  token: string,
  id: number,
  datos: ReprogramarTurnoRequest,
): Promise<Turno> {
  return fetch(`${BASE_URL}/${id}/reprogramar`, {
    method: 'PUT',
    headers: cabeceras(token, true),
    body: JSON.stringify(datos),
  }).then((response) => manejarRespuesta<Turno>(response));
}

export function cambiarEstadoTurno(
  token: string,
  id: number,
  estado: EstadoTurno,
): Promise<Turno> {
  return fetch(`${BASE_URL}/${id}/estado`, {
    method: 'PATCH',
    headers: cabeceras(token, true),
    body: JSON.stringify({ estado }),
  }).then((response) => manejarRespuesta<Turno>(response));
}

export function cancelarTurno(
  token: string,
  id: number,
): Promise<Turno> {
  return fetch(`${BASE_URL}/${id}/cancelar`, {
    method: 'PATCH',
    headers: cabeceras(token),
  }).then((response) => manejarRespuesta<Turno>(response));
}