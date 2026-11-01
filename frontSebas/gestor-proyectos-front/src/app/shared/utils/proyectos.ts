// Textos, íconos y colores de proyectos, fases y módulos del lado del equipo.
// (El Portal de Clientes tiene los suyos, con palabras pensadas para el cliente.)

import { Etiqueta, Tono } from '../models/etiqueta.model';

export interface InfoModulo {
  /** "API", "Web", "Móvil" (chips) */
  corto: string;
  /** "Backend · API", "Frontend web" (formularios, filtros) */
  largo: string;
  icono: string;
  tono: Tono;
}

export const MODULOS: Record<string, InfoModulo> = {
  API: { corto: 'API', largo: 'Backend · API', icono: 'bi-hdd-stack', tono: 'morado' },
  WEB: { corto: 'Web', largo: 'Frontend web', icono: 'bi-display', tono: 'violeta' },
  MOVIL: { corto: 'Móvil', largo: 'App móvil', icono: 'bi-phone', tono: 'verde' },
  ESCRITORIO: { corto: 'Escritorio', largo: 'Escritorio', icono: 'bi-pc-display', tono: 'azul' },
  BASE_DATOS: { corto: 'Base de datos', largo: 'Base de datos', icono: 'bi-database', tono: 'ambar' },
  INFRAESTRUCTURA: { corto: 'Infra', largo: 'Infraestructura', icono: 'bi-cloud', tono: 'turquesa' },
};

export const TIPOS_MODULO = Object.keys(MODULOS);

export function infoModulo(tipo: string): InfoModulo {
  return MODULOS[tipo] ?? { corto: tipo, largo: tipo, icono: 'bi-box', tono: 'gris' };
}

const ESTADOS_PROYECTO: Record<string, Etiqueta> = {
  PLANEACION: { texto: 'Planeación', tono: 'gris' },
  EN_CURSO: { texto: 'En curso', tono: 'morado' },
  EN_PAUSA: { texto: 'En pausa', tono: 'naranja' },
  TERMINADO: { texto: 'Terminado', tono: 'verde' },
  CANCELADO: { texto: 'Cancelado', tono: 'rojo' },
};

export const ORDEN_ESTADOS_PROYECTO = Object.keys(ESTADOS_PROYECTO);

export function etiquetaProyecto(estado: string): Etiqueta {
  return ESTADOS_PROYECTO[estado] ?? { texto: estado, tono: 'gris' };
}

/** En el equipo una fase publicada se ve como "Cerrada" (PDF pág. 14). */
const ESTADOS_FASE: Record<string, Etiqueta> = {
  PLANEADA: { texto: 'Planeada', tono: 'gris' },
  EN_CURSO: { texto: 'En curso', tono: 'morado' },
  PAUSADA: { texto: 'Pausada', tono: 'naranja' },
  LISTA_PARA_RELEASE: { texto: 'Lista para release', tono: 'turquesa' },
  EN_PUBLICACION: { texto: 'En publicación', tono: 'turquesa' },
  PUBLICADA: { texto: 'Cerrada', tono: 'verde' },
  REVERTIDA: { texto: 'Revertida', tono: 'rojo' },
  CANCELADA: { texto: 'Cancelada', tono: 'gris' },
};

export function etiquetaFase(estado: string): Etiqueta {
  return ESTADOS_FASE[estado] ?? { texto: estado, tono: 'gris' };
}

/** Color de la barra de avance de cada proyecto, siempre el mismo para el mismo proyecto. */
export function tonoDeProyecto(id: number, estado: string): Tono {
  if (estado === 'EN_PAUSA') return 'naranja';
  const tonos: Tono[] = ['morado', 'verde', 'violeta', 'turquesa', 'azul'];
  return tonos[id % tonos.length];
}
