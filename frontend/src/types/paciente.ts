export interface Paciente {
  id: number;
  nombre: string;
  apellido: string;
  dni: string;
  fechaNacimiento: string | null;
  telefono: string | null;
  email: string | null;
  direccion: string | null;
  activo: boolean;
  fechaAlta: string;
}

export interface PacienteRequest {
  nombre: string;
  apellido: string;
  dni: string;
  fechaNacimiento: string | null;
  telefono: string;
  email: string;
  direccion: string;
}