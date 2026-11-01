/** Una historia del JSON que devuelve la IA (ver el prompt de Importar historias). */
export interface HistoriaImportada {
  titulo: string;
  como: string;
  quiero: string | null;
  para: string | null;
  prioridad: string | null;
  puntos: number | null;
  criterios: string[];
}

/** Cómo quedó el JSON pegado: las historias que trae o por qué no se puede ejecutar. */
export type LecturaJson =
  | { valido: true; historias: HistoriaImportada[]; incompletas: number }
  | { valido: false; error: string };

export interface ResultadoImportacion {
  creadas: number;
  /** Les falta Quiero, Para o criterios: quedan con punto ámbar. */
  incompletas: number;
}

/** Las historias de una fase completas, para copiarlas como JSON. */
export interface HistoriasDeFase {
  proyectoNombre: string;
  /** "Fase 1 · Primera versión" */
  faseTitulo: string;
  historias: HistoriaCompleta[];
}

export interface HistoriaCompleta {
  /** "HU-0001" */
  codigo: string;
  estado: string;
  titulo: string;
  como: string | null;
  quiero: string | null;
  para: string | null;
  prioridad: string;
  puntos: number | null;
  /** El suyo o, si no tiene, el de la fase. */
  figmaUrl: string | null;
  criterios: string[];
}

/** Qué campos lleva el JSON que se copia. Título, Como, Quiero y Para siempre van. */
export interface CamposJson {
  ids: boolean;
  estado: boolean;
  criterios: boolean;
  prioridad: boolean;
  figma: boolean;
}
