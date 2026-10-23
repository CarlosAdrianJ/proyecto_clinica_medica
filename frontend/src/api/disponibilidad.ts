import type { Disponibilidad } from '../types/disponibilidad';

const BASE_URL = '/api/disponibilidad';

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'La sesión no es válida o ha vencido.';
    case 403:
      return 'No tiene permiso para consultar esta disponibilidad.';
    case 404:
      return 'El profesional solicitado no existe.';
    case 409:
      return 'El profesional se encuentra inactivo.';
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
      } else if (
        typeof cuerpo?.detail === 'string' &&
        cuerpo.detail.trim()
      ) {
        mensaje = cuerpo.detail;
      }
    } catch {
      mensaje = mensajePorEstado(response.status);
    }

    throw new Error(mensaje);
  }

  return response.json() as Promise<T>;
}

export async function consultarDisponibilidad(
  token: string,
  profesionalId: number,
  fecha: string,
): Promise<Disponibilidad> {
  const parametros = new URLSearchParams({
    profesionalId: String(profesionalId),
    fecha,
  });

  const response = await fetch(
    `${BASE_URL}?${parametros.toString()}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  return manejarRespuesta<Disponibilidad>(response);
}