import { Tono } from '../../../shared/models/etiqueta.model';

/** Una de las cuatro tarjetas de arriba. */
export interface Metrica {
  titulo: string;
  valor: number;
  detalle: string;
  /** Rojo cuando hay algo que atender (ej. bugs). */
  alerta: boolean;
}

export interface ItemPendiente {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  estadoTexto: string;
  accion: string;
  /** El botón hace la acción ahí mismo (si no, tiene su propio proceso) */
  ejecutable: boolean;
  /** "Ana · Analista" (solo en "Esperando a otros") */
  quien: string | null;
}

export interface FaseAvance {
  nombre: string;
  avance: number;
  tono: Tono;
}

export interface Movimiento {
  descripcion: string;
  fecha: Date | null;
}

export interface Dashboard {
  proyectoNombre: string;
  metricas: Metrica[];
  pendientesMios: number;
  teToca: ItemPendiente[];
  esperandoAOtros: ItemPendiente[];
  fases: FaseAvance[];
  actividad: Movimiento[];
}
