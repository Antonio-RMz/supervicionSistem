import { Etiqueta } from '../../../shared/models/etiqueta.model';

/** Menú Historias: todas las historias del proyecto elegido arriba. */
export interface ListadoHistorias {
  proyectoId: number;
  proyectoNombre: string;
  historias: HistoriaDeLista[];
  puedeCrear: boolean;
}

export interface HistoriaDeLista {
  id: number;
  /** "HU-0016" */
  codigo: string;
  titulo: string;
  estado: string;
  etiqueta: Etiqueta;
  prioridad: Etiqueta;
  puntos: number | null;
  faseId: number;
  /** "Fase 4 · Portal móvil" */
  fase: string;
  analista: string;
  ticketsTerminados: number;
  ticketsTotal: number;
  /** De 0 a 100 */
  avance: number;
  /** "hace 2 h" */
  actualizada: string;
}
