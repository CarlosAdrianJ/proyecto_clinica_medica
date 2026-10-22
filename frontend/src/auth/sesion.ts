const CLAVE_TOKEN = 'clinica_medica_token';

export function guardarToken(token: string): void {
  sessionStorage.setItem(CLAVE_TOKEN, token);
}

export function obtenerToken(): string | null {
  return sessionStorage.getItem(CLAVE_TOKEN);
}

export function eliminarToken(): void {
  sessionStorage.removeItem(CLAVE_TOKEN);
}