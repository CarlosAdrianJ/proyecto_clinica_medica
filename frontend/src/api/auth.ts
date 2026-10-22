import type {
  LoginRequest,
  LoginResponse,
  UsuarioAutenticado,
} from '../types/auth';

const BASE_URL = '/api/auth';

function mensajePorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Los datos ingresados no son válidos.';
    case 401:
      return 'El usuario o la contraseña son incorrectos.';
    case 403:
      return 'La cuenta no tiene permiso para ingresar.';
    default:
      return 'No se pudo establecer comunicación con el servidor.';
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
      mensaje = mensajePorEstado(response.status);
    }

    throw new Error(mensaje);
  }

  return response.json() as Promise<T>;
}

export async function iniciarSesion(
  credenciales: LoginRequest,
): Promise<LoginResponse> {
  const response = await fetch(`${BASE_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credenciales),
  });

  return manejarRespuesta<LoginResponse>(response);
}

export async function consultarUsuarioActual(
  token: string,
): Promise<UsuarioAutenticado> {
  const response = await fetch(`${BASE_URL}/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return manejarRespuesta<UsuarioAutenticado>(response);
}