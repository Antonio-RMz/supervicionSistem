/** JSON de GET /api/equipo/proyectos/{id}/historias (menú Historias). */
export interface ListadoHistoriasDto {
  proyectoId: number;
  proyectoNombre: string;
  historias: HistoriaDeListaDto[];
  puedeCrear: boolean;
}

export interface HistoriaDeListaDto {
  id: number;
  /** "HU-0016" */
  codigo: string;
  titulo: string;
  estado: string;
  prioridad: string;
  puntos: number | null;
  faseId: number;
  faseNumero: number;
  faseNombre: string;
  analistaNombre: string;
  ticketsTerminados: number;
  ticketsTotal: number;
  actualizadoEn: string | null;
}
