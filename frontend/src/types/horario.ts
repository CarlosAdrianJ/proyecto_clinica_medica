export type DiaSemana =
  | 'LUNES'
  | 'MARTES'
  | 'MIERCOLES'
  | 'JUEVES'
  | 'VIERNES'
  | 'SABADO'
  | 'DOMINGO';

export interface HorarioAtencion {
  id: number;
  profesionalId: number;
  profesionalNombre: string;
  profesionalApellido: string;
  diaSemana: DiaSemana;
  horaDesde: string;
  horaHasta: string;
  duracionTurno: number;
  activo: boolean;
}

export interface HorarioAtencionRequest {
  profesionalId: number;
  diaSemana: DiaSemana;
  horaDesde: string;
  horaHasta: string;
  duracionTurno: number;
}

export interface ProfesionalHorario {
  id: number;
  nombre: string;
  apellido: string;
  matricula: string;
  activo: boolean;
}