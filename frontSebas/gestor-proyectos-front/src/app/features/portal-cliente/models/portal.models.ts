// Modelos que usan las pantallas del portal. Ya vienen listos para pintar
// (fechas como Date, etiquetas con texto y color), así el HTML queda simple.

import { Etiqueta } from '../../../shared/models/etiqueta.model';

export type EstadoFase = 'PROXIMA' | 'EN_CURSO' | 'EN_PAUSA' | 'ENTREGADA' | 'CANCELADA';

export interface Modulo {
  tipo: string;
  /** "API", "Web", "Móvil" (tarjetas) */
  nombreCorto: string;
  /** "Backend · API", "Web", "App móvil" (detalle) */
  nombreLargo: string;
  /** Clase de bootstrap-icons, ej. "bi-phone" */
  icono: string;
}

export interface Fase {
  id: number;
  numero: number;
  nombre: string;
  objetivo: string | null;
  estado: EstadoFase;
  etiqueta: Etiqueta;
  fechaInicio: Date | null;
  fechaFin: Date | null;
  version: string | null;
  avance: number;
}

export interface MiProyecto {
  id: number;
  codigo: string;
  nombre: string;
  etiqueta: Etiqueta;
  modulos: Modulo[];
  avance: number;
  faseActual: Fase | null;
  demosPendientes: number;
}

export interface AvanceModulo {
  modulo: Modulo;
  avance: number;
}

export interface Historia {
  titulo: string;
  descripcion: string | null;
  etiqueta: Etiqueta;
}

export interface VersionEntregada {
  version: string;
  fecha: Date | null;
  resumen: string | null;
}

export interface Contacto {
  nombre: string;
  rol: string;
  correo: string;
}

export interface Demo {
  id: number;
  titulo: string;
  url: string | null;
}

export interface AvanceProyecto {
  id: number;
  codigo: string;
  nombre: string;
  etiqueta: Etiqueta;
  avance: number;
  modulos: Modulo[];
  fases: Fase[];
  faseActual: Fase | null;
  modulosFaseActual: AvanceModulo[];
  historias: Historia[];
  versiones: VersionEntregada[];
  contacto: Contacto | null;
  demoPendiente: Demo | null;
}
