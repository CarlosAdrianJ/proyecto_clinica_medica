export interface FranjaDisponible {
  horaInicio: string;
  horaFin: string;
}

export interface Disponibilidad {
  profesionalId: number;
  fecha: string;
  franjas: FranjaDisponible[];
}