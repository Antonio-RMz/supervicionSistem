import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';

export interface TicketMio {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  estadoTexto: string;
  prioridad: string;
  etiquetaPrioridad: Etiqueta;
  /** "Fase 2" */
  fase: string;
  /** "HU-0012" o, si no cuelga de una historia, la fase */
  historiaOFase: string;
  /** null si no cuelga de una historia */
  historia: string | null;
  responsable: { nombre: string; iniciales: string; tono: Tono } | null;
  /** Solo si la siguiente acción es mía */
  accion: string | null;
  rechazadaEnQa: boolean;
  meToca: boolean;
  asignado: boolean;
  creadoPorMi: boolean;
  /** El botón hace la acción ahí mismo. "Corregir" y lo que tiene su propio proceso solo indican qué sigue. */
  ejecutable: boolean;
}

/** Filtros de arriba de la tabla. '' = todos. */
export interface Filtros {
  texto: string;
  tipo: string;
  estado: string;
  prioridad: string;
  fase: string;
  historia: string;
}

export const SIN_FILTROS: Filtros = { texto: '', tipo: '', estado: '', prioridad: '', fase: '', historia: '' };

