import type {
  Paciente,
  PacienteRequest,
} from '../types/paciente';

const BASE_URL = '/api/pacientes';

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'La sesión no es válida o ha vencido.';
    case 403:
      return 'No tiene permiso para realizar esta operación.';
    case 404:
      return 'El paciente solicitado no existe.';
    case 409:
      return 'Ya existe un paciente con ese DNI.';
    default:
      return 'No se pudo establecer comunicación con el servidor.';
  }
}

async function manejarRespuesta<T>(
  response: Response,
): Promise<T> {
  if (!response.ok) {
    let mensaje = mensajePorEstado(response.status);

    try {
      const cuerpo = await response.json();

      if (
        typeof cuerpo?.message === 'string' &&
        cuerpo.message.trim()
      ) {
        mensaje = cuerpo.message;
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

export async function listarPacientes(
  token: string,
): Promise<Paciente[]> {
  const response = await fetch(BASE_URL, {
    headers: encabezados(token),
  });

  return manejarRespuesta<Paciente[]>(response);
}

export async function buscarPaciente(
  token: string,
  id: number,
): Promise<Paciente> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    headers: encabezados(token),
  });

  return manejarRespuesta<Paciente>(response);
}

export async function crearPaciente(
  token: string,
  datos: PacienteRequest,
): Promise<Paciente> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<Paciente>(response);
}

export async function actualizarPaciente(
  token: string,
  id: number,
  datos: PacienteRequest,
): Promise<Paciente> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<Paciente>(response);
}

export async function eliminarPaciente(
  token: string,
  id: number,
): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
    headers: encabezados(token),
  });

  return manejarRespuesta<void>(response);
}