import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';

/** La vista Integrar historia (PDF pág. 28), lista para pintarse. */
export interface IntegracionHistoria {
  historiaId: number;
  proyectoId: number;
  proyectoNombre: string;
  faseId: number;
  faseTitulo: string;
  codigo: string;
  titulo: string;
  estado: string;
  etiqueta: Etiqueta;
  descripcion: string;
  /** "Líder" o "Admin" si puede integrarla; null si solo la ve */
  rol: string | null;
  impedimentos: string[];
  flujo: PasoFlujo[];
  /** "2 módulos · 3 tickets" */
  resumen: string;
  modulos: ModuloAIntegrar[];
  revision: PuntoRevision[];
  /** "Todo listo", "Falta 1", "Con avisos" */
  resumenRevision: string;
  ambientes: { nombre: string; tono: Tono; texto: string }[];
  historiasDeFase: { id: number; codigo: string; etiqueta: Etiqueta }[];
  notaRelease: string;
  pasos: string[];
}

export interface PasoFlujo {
  titulo: string;
  detalle: string;
  avance: 'hecho' | 'actual' | 'pendiente';
}

export interface ModuloAIntegrar {
  nombre: string;
  /** "Backend · API · Spring Boot" */
  descripcion: string;
  icono: string;
  tono: Tono;
  tickets: TicketAIntegrar[];
}

export interface TicketAIntegrar {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  rama: string | null;
  /** "PR #45"; null si no tiene PR registrado */
  pr: string | null;
  chips: Etiqueta[];
}

export interface PuntoRevision {
  texto: string;
  estado: 'ok' | 'aviso' | 'falta';
  detalle: string;
}
