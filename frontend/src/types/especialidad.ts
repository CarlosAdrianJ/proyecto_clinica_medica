export interface Especialidad {
  id: number;
  nombre: string;
  descripcion: string | null;
  activo: boolean;
}

export interface EspecialidadFormData {
  nombre: string;
  descripcion: string;
}
