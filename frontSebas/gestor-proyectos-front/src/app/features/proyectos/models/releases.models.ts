/** La pestaña Releases del proyecto (PDF pág. 36), lista para pintarse. */
export interface ReleasesProyecto {
  listasParaIntegrar: { id: number; codigo: string; titulo: string }[];
  puedeIntegrar: boolean;
  proximaFaseId: number | null;
  parche: { version: string; resumen: string; mantenimientoFaseId: number | null } | null;
  publicados: ReleasePublicado[];
}

export interface ReleasePublicado {
  id: number;
  version: string;
  esFase: boolean;
  faseId: number | null;
  /** "Fase 1 · Análisis", "Parche · Mantenimiento" */
  origen: string;
  revertida: boolean;
  /** "21 feb 2026" */
  fecha: string;
  /** "4 historias · 9 tickets", "2 bugs" */
  contenido: string;
  bugs: { id: number; codigo: string; titulo: string }[];
  publicadoPor: string;
  enProduccion: boolean;
}
