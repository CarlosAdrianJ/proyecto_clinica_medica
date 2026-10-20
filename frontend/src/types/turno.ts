export type EstadoTurno =
  | 'PENDIENTE'
  | 'ATENDIDO'
  | 'CANCELADO'
  | 'AUSENTE';

export interface Turno {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  profesionalId: number;
  profesionalNombre: string;
  especialidadId: number;
  especialidadNombre: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  observaciones: string | null;
  fechaCreacion: string;
}

export interface TurnoRequest {
  pacienteId: number;
  profesionalId: number;
  especialidadId: number;
  fecha: string;
  horaInicio: string;
  observaciones: string;
}

export interface ReprogramarTurnoRequest {
  fecha: string;
  horaInicio: string;
}

export interface FiltrosTurno {
  pacienteId?: number;
  profesionalId?: number;
  fecha?: string;
  estado?: EstadoTurno;
}