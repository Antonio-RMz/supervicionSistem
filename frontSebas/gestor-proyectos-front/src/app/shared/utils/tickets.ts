// Textos y colores de tickets e historias que se repiten en varias pantallas del Gestor.

import { Etiqueta, Tono } from '../models/etiqueta.model';

/** Color de la etiqueta de cada tipo: BUGS en rojo, MEJR en verde... */
export const TONO_TIPO: Record<string, Tono> = {
  ACTV: 'morado',
  BUGS: 'rojo',
  MEJR: 'verde',
  HIST: 'violeta',
  FASE: 'azul',
};

/**
 * Cómo se nombra cada item, igual que en el back (CodigoItem): historias "HU-0016", tickets "ACTV-41".
 */
export function codigoHistoria(numero: number): string {
  return `HU-${String(numero).padStart(4, '0')}`;
}

export function codigoItem(tipo: string, numero: number): string {
  return tipo === 'HIST' ? codigoHistoria(numero) : `${tipo}-${String(numero).padStart(2, '0')}`;
}

/** Lo que dice la etiqueta del tipo: las historias de usuario se ven como "HU". */
export function textoTipo(tipo: string): string {
  return tipo === 'HIST' ? 'HU' : tipo;
}

const ESTADOS: Record<string, string> = {
  // Historia
  BORRADOR: 'Borrador',
  EN_PLANEACION: 'En planeación',
  LISTA: 'Lista',
  LISTA_DESARROLLO: 'Lista para desarrollo',
  EN_DESARROLLO: 'En desarrollo',
  EN_VALIDACION: 'En validación',
  ACEPTADA: 'Aceptada',
  DESCARTADA: 'Descartada',
  // Tickets
  POR_HACER: 'Por hacer',
  EN_PROGRESO: 'En progreso',
  BLOQUEADA: 'Bloqueada',
  EN_QA: 'En QA',
  APROBADA: 'Aprobada',
  APROBADO: 'Aprobado',
  APROBADA_QA: 'PR aprobado',
  INTEGRADA: 'Integrada',
  INTEGRADO: 'Integrado',
  CERRADA: 'Cerrada',
  CERRADO: 'Cerrado',
  CANCELADA: 'Cancelada',
  REPORTADO: 'Reportado',
  CONFIRMADO: 'Confirmado',
  EN_CORRECCION: 'En corrección',
  PUBLICADO: 'Publicado',
  NO_REPRODUCIBLE: 'No reproducible',
  PROPUESTA: 'Propuesta',
  RECHAZADA: 'Rechazada',
  EN_REVISION: 'PR en revisión',
  HECHA: 'Hecha',
  INVESTIGANDO: 'Investigando',
  CONCLUSIONES: 'Conclusiones',
  REVISADA: 'Revisada',
  REDACTANDO: 'Redactando',
  PUBLICADA: 'Publicada',
};

/** "EN_QA" -> "En QA" */
export function textoEstado(estado: string): string {
  return ESTADOS[estado] ?? estado.replace(/_/g, ' ').toLowerCase();
}

/** Orden en que avanza el trabajo; lo usan las columnas de un tablero. */
export const ORDEN_ESTADOS = Object.keys(ESTADOS);

const PRIORIDADES: Record<string, Etiqueta> = {
  ALTA: { texto: 'Alta', tono: 'rojo' },
  MEDIA: { texto: 'Media', tono: 'ambar' },
  BAJA: { texto: 'Baja', tono: 'gris' },
};

/** "ALTA" -> Alta en rojo */
export function etiquetaPrioridad(prioridad: string): Etiqueta {
  return PRIORIDADES[prioridad] ?? { texto: textoEstado(prioridad), tono: 'gris' };
}

/** Color de cada estado de historia (PDF pág. 21). */
const TONOS_HISTORIA: Record<string, Tono> = {
  BORRADOR: 'gris',
  EN_PLANEACION: 'azul',
  LISTA: 'violeta',
  LISTA_DESARROLLO: 'turquesa',
  EN_DESARROLLO: 'morado',
  EN_QA: 'ambar',
  EN_VALIDACION: 'naranja',
  ACEPTADA: 'turquesa',
  INTEGRADA: 'verde',
  CERRADA: 'gris',
  DESCARTADA: 'rojo',
};

/** "EN_VALIDACION" -> En validación en naranja */
export function etiquetaHistoria(estado: string): Etiqueta {
  return { texto: textoEstado(estado), tono: TONOS_HISTORIA[estado] ?? 'gris' };
}

/** Color del estado de un ticket en una lista: en trabajo, en revisión, aprobado o cerrado. */
export function etiquetaEstadoTicket(estado: string): Etiqueta {
  const tono: Tono = ['CERRADA', 'CERRADO', 'INTEGRADA', 'INTEGRADO', 'PUBLICADA', 'PUBLICADO', 'REVISADA'].includes(estado)
    ? 'verde'
    : ['APROBADA', 'APROBADO', 'APROBADA_QA', 'HECHA'].includes(estado)
      ? 'violeta'
      : ['EN_QA', 'EN_REVISION', 'CONCLUSIONES'].includes(estado)
        ? 'naranja'
        : ['EN_PROGRESO', 'EN_CORRECCION', 'INVESTIGANDO', 'REDACTANDO'].includes(estado)
          ? 'morado'
          : ['CANCELADA', 'BLOQUEADA', 'RECHAZADA'].includes(estado)
            ? 'rojo'
            : 'gris';
  return { texto: textoEstado(estado), tono };
}

/** Estados en que nadie ha empezado a trabajar el ticket (igual que TicketDeHistoria en el back). */
const SIN_EMPEZAR = ['BORRADOR', 'POR_HACER', 'REPORTADO', 'CONFIRMADO', 'PROPUESTA'];
const FUERA = ['CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA'];

/** El Desarrollador aún no lo inicia: no tiene rama ni trabajo. En una mejora "Aprobada" es la propuesta aceptada. */
export function ticketSinEmpezar(tipo: string, estado: string): boolean {
  return SIN_EMPEZAR.includes(estado) || FUERA.includes(estado) || (tipo === 'MEJR' && estado === 'APROBADA');
}
