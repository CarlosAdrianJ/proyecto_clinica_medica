import type { Rol } from './auth';

export interface Usuario {
  id: number;
  nombreUsuario: string;
  rol: Rol;
  profesionalId: number | null;
  activo: boolean;
}

export interface UsuarioRequest {
  nombreUsuario: string;
  contrasena: string;
  rol: Rol;
  profesionalId: number | null;
  activo: boolean;
}

export interface ProfesionalResumen {
  id: number;
  nombre: string;
  apellido: string;
  matricula: string;
  activo: boolean;
}