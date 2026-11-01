import { Tono } from '../../../shared/models/etiqueta.model';
import { TipoMovimiento } from '../../tickets/models/movimientos.models';

export type Columna = 'POR_HACER' | 'EN_PROGRESO' | 'EN_QA' | 'APROBADA' | 'CERRADA';

export const COLUMNAS: { id: Columna; titulo: string }[] = [
  { id: 'POR_HACER', titulo: 'Por hacer' },
  { id: 'EN_PROGRESO', titulo: 'En progreso' },
  { id: 'EN_QA', titulo: 'En revisión' },
  { id: 'APROBADA', titulo: 'Aprobada' },
  { id: 'CERRADA', titulo: 'Cerrada' },
];

export interface MovimientoTarjeta {
  tipo: TipoMovimiento;
  accion: string;
  columnaDestino: Columna;
}

export interface Tarjeta {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  /** Color del punto: alta roja, media naranja, baja gris */
  tonoPrioridad: Tono;
  textoPrioridad: string;
  columna: Columna;
  historia: string | null;
  responsable: { id: number; nombre: string; iniciales: string; tono: Tono } | null;
  /** Lo que yo puedo hacer con ella; null si no me toca */
  avance: MovimientoTarjeta | null;
  rechazo: MovimientoTarjeta | null;
  /** "Analista", "Líder"... cuando le toca a otra persona: candado */
  bloqueadaPor: string | null;
  mia: boolean;
}

export interface FaseOpcion {
  id: number;
  /** "Fase 2 · Desarrollo" o "Mantenimiento" */
  nombre: string;
}

export interface Tablero {
  fases: FaseOpcion[];
  faseId: number | null;
  tarjetas: Tarjeta[];
}

// ─── Por historia: su Analista la prueba completa, así que el tablero sigue su flujo ───

export type ColumnaHistoria = 'POR_HACER' | 'EN_DESARROLLO' | 'EN_VALIDACION' | 'ACEPTADA' | 'INTEGRADA';

export const COLUMNAS_HISTORIA: { id: ColumnaHistoria; titulo: string; estados: string[] }[] = [
  { id: 'POR_HACER', titulo: 'Por hacer', estados: ['BORRADOR', 'EN_PLANEACION', 'LISTA', 'LISTA_DESARROLLO'] },
  { id: 'EN_DESARROLLO', titulo: 'En desarrollo', estados: ['EN_DESARROLLO'] },
  { id: 'EN_VALIDACION', titulo: 'En validación', estados: ['EN_VALIDACION'] },
  { id: 'ACEPTADA', titulo: 'Aceptada', estados: ['ACEPTADA'] },
  { id: 'INTEGRADA', titulo: 'Integrada', estados: ['INTEGRADA', 'CERRADA'] },
];
