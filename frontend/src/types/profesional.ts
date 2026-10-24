export interface Profesional {
  id: number;
  nombre: string;
  apellido: string;
  matricula: string;
  telefono: string | null;
  email: string | null;
  activo: boolean;
  fechaAlta: string;
  especialidades: { id: number; nombre: string; activo: boolean }[];
}

export interface ProfesionalRequest {
  nombre: string;
  apellido: string;
  matricula: string;
  telefono: string;
  email: string;
}
