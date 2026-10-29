import type {
  ProfesionalResumen,
  Usuario,
  UsuarioRequest,
} from '../types/usuario';

const USUARIOS_URL = `${import.meta.env?.VITE_API_URL ?? ''}/api/usuarios`;
const PROFESIONALES_URL = `${import.meta.env?.VITE_API_URL ?? ''}/api/profesionales`;

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'La sesión no es válida o ha vencido.';
    case 403:
      return 'No tiene permiso para realizar esta operación.';
    case 404:
      return 'El registro solicitado no existe.';
    case 409:
      return 'El nombre de usuario ya está en uso.';
    default:
      return 'No se pudo establecer comunicación con el servidor.';
  }
}

async function manejarRespuesta<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let mensaje = mensajePorEstado(response.status);

    try {
      const cuerpo = await response.json();

      const detalle = cuerpo?.message ?? cuerpo?.detail;
      if (typeof detalle === 'string' && detalle.trim()) {
        mensaje = detalle;
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

export async function listarUsuarios(token: string): Promise<Usuario[]> {
  const response = await fetch(USUARIOS_URL, {
    headers: encabezados(token),
  });

  return manejarRespuesta<Usuario[]>(response);
}

export async function crearUsuario(
  token: string,
  datos: UsuarioRequest,
): Promise<Usuario> {
  const response = await fetch(USUARIOS_URL, {
    method: 'POST',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<Usuario>(response);
}

export async function actualizarUsuario(
  token: string,
  id: number,
  datos: UsuarioRequest,
): Promise<Usuario> {
  const response = await fetch(`${USUARIOS_URL}/${id}`, {
    method: 'PUT',
    headers: encabezados(token),
    body: JSON.stringify(datos),
  });

  return manejarRespuesta<Usuario>(response);
}

export async function desactivarUsuario(
  token: string,
  id: number,
): Promise<void> {
  const response = await fetch(`${USUARIOS_URL}/${id}`, {
    method: 'DELETE',
    headers: encabezados(token),
  });

  return manejarRespuesta<void>(response);
}

export async function listarProfesionales(
  token: string,
): Promise<ProfesionalResumen[]> {
  const response = await fetch(PROFESIONALES_URL, {
    headers: encabezados(token),
  });

  return manejarRespuesta<ProfesionalResumen[]>(response);
}
