/** JSON de GET /api/equipo/proyectos/{id}/releases (PDF pág. 36). */
export interface ReleasesDto {
  listasParaIntegrar: { id: number; numero: number; titulo: string }[];
  puedeIntegrar: boolean;
  proximaFaseId: number | null;
  parche: { version: string; aprobados: number; enCamino: number; mantenimientoFaseId: number | null } | null;
  publicados: PublicadoDto[];
}

export interface PublicadoDto {
  id: number;
  version: string;
  /** FASE o PARCHE */
  tipo: string;
  /** PUBLICADA o REVERTIDA */
  estado: string;
  faseId: number | null;
  faseNumero: number | null;
  faseNombre: string | null;
  fecha: string | null;
  historias: number;
  tickets: number;
  bugs: { id: number; codigo: string; titulo: string }[];
  publicadoPor: string | null;
  enProduccion: boolean;
}
