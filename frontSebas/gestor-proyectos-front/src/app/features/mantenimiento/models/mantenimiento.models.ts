import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';

/** La vista de Mantenimiento (PDF pág. 37), lista para pintarse. */
export interface Mantenimiento {
  faseId: number;
  enProduccion: string | null;
  siguienteParche: string;
  metricas: { titulo: string; valor: string; detalle: string; alerta: boolean }[];
  urgentes: TicketMantenimiento[];
  listos: TicketMantenimiento[];
  normales: TicketMantenimiento[];
  /** "va con la Fase 5"; "va con la siguiente fase" */
  conLaFase: string;
  parches: { version: string; fecha: string; bugs: string }[];
  puedePublicar: boolean;
}

export interface TicketMantenimiento {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  etiqueta: Etiqueta;
  severidad: Etiqueta | null;
  rama: string | null;
  responsable: { nombre: string; iniciales: string; tono: Tono } | null;
}

/** Lo que piden los modales Nuevo ticket (PDF pág. 38) y Reportar falla (pág. 39). */
export interface PreparacionMantenimiento {
  enProduccion: string | null;
  /** "la Fase 5"; "la siguiente fase" */
  conLaFase: string;
  /** nombre "portal-api · Backend · API"; corto "API" para los botones de Reportar falla */
  modulos: { id: number; nombre: string; corto: string; icono: string }[];
  /** "Pedro Ramírez · Desarrollador" */
  equipo: { id: number; nombre: string; desarrollador: boolean }[];
  siguientes: Record<string, number>;
  historias: { id: number; nombre: string }[];
  puedeCrear: boolean;
  puedeReportar: boolean;
}

/** Los dos tipos que se crean en Mantenimiento. */
export const TIPOS_MANTENIMIENTO = [
  { tipo: 'MEJR', nombre: 'Mejora pequeña', detalle: 'Ajuste a algo que ya existe' },
  { tipo: 'BUGS', nombre: 'Bug menor', detalle: 'Media o Baja, ya en producción' },
];
