// JSON de GET /api/equipo/proyectos/{id}/tablero (ver TableroDtos.java).

export interface MovimientoDto {
  tipo: 'AVANZAR' | 'RECHAZAR';
  accion: string;
  columnaDestino: string;
}

export interface TarjetaDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  prioridad: string;
  columna: string;
  historiaId: number | null;
  historiaCodigo: string | null;
  responsableId: number | null;
  responsableNombre: string | null;
  responsableIniciales: string | null;
  avance: MovimientoDto | null;
  rechazo: MovimientoDto | null;
  bloqueadaPor: string | null;
  mia: boolean;
}

export interface TableroDto {
  fases: { id: number; numero: number; nombre: string; estado: string; mantenimiento: boolean }[];
  faseId: number | null;
  tarjetas: TarjetaDto[];
}
