// JSON de GET /api/equipo/proyectos/{id}/mis-tickets (ver MisTicketsDtos.java).

export interface TicketMioDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  prioridad: string;
  faseId: number;
  faseNumero: number;
  historiaId: number | null;
  historiaCodigo: string | null;
  responsableId: number | null;
  responsableNombre: string | null;
  responsableIniciales: string | null;
  accion: string | null;
  rechazadaEnQa: boolean;
  meToca: boolean;
  asignado: boolean;
  creadoPorMi: boolean;
  ejecutable: boolean;
  actualizadoEn: string | null;
}
