export type Rol =
  | 'ADMINISTRADOR'
  | 'RECEPCIONISTA'
  | 'PROFESIONAL';

export interface UsuarioAutenticado {
  id: number;
  nombreUsuario: string;
  rol: Rol;
  profesionalId: number | null;
  activo: boolean;
}

export interface LoginRequest {
  nombreUsuario: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEn: number;
  usuario: UsuarioAutenticado;
}

export interface Sesion {
  token: string;
  usuario: UsuarioAutenticado;
}