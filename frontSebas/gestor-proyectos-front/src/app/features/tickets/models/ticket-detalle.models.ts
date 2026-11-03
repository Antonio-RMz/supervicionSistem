import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';
import { InfoModulo } from '../../../shared/utils/proyectos';
import { TipoMovimiento } from './movimientos.models';

export interface OpcionTicket {
  tipo: TipoMovimiento;
  texto: string;
  destino: string;
  /** Lo que falta para poder presionarlo ("Primero debe cerrarse ACTV-07"); vacío si ya se puede. */
  impedimentos: string[];
}

/** Las que abren una ventana antes de ejecutarse (PDF pág. 23 y 26). */
export type Confirmacion =
  | 'ENVIAR_QA'
  | 'BLOQUEAR'
  | 'RECHAZAR'
  | 'CANCELAR'
  | 'RETIRAR'
  | 'CONFIRMAR'
  | 'NO_REPRODUCIBLE'
  | 'PUBLICAR_PARCHE'
  | 'APROBAR_MEJORA';

/** Severidad de un bug: Crítica y Alta van por hotfix desde main (PDF pág. 23). */
export const SEVERIDADES: { codigo: string; texto: string; tono: Tono; detalle: string }[] = [
  { codigo: 'CRITICA', texto: 'Crítica', tono: 'rojo', detalle: 'Producción caída o datos en riesgo · va por hotfix' },
  { codigo: 'ALTA', texto: 'Alta', tono: 'naranja', detalle: 'Algo importante no funciona · va por hotfix' },
  { codigo: 'MEDIA', texto: 'Media', tono: 'ambar', detalle: 'Sigue el flujo normal y sale con la siguiente fase' },
  { codigo: 'BAJA', texto: 'Baja', tono: 'gris', detalle: 'Sigue el flujo normal y sale con la siguiente fase' },
];

export interface PasoDelFlujo {
  estado: string;
  /** "Desarrollador", "Líder", "Analista"; vacío si avanza solo */
  roles: string[];
  /** "Pedro · inicia", "Ana · lo prueba", "se integra con su historia" */
  detalle: string;
  /** hecho: ya pasó; actual: ahí está; pendiente: falta */
  avance: 'hecho' | 'actual' | 'pendiente';
}

/** El detalle del ticket (PDF pág. 29) ya listo para mostrarse. */
export interface DetalleTicket {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  faseId: number;
  faseTitulo: string;
  historia: {
    id: number;
    codigo: string;
    titulo: string;
    /** BORRADOR, LISTA, LISTA_DESARROLLO, EN_DESARROLLO... */
    estado: string;
    estadoTexto: string;
    como: string | null;
    quiero: string | null;
    para: string | null;
  } | null;
  tipo: string;
  codigo: string;
  /** El número del ticket (41 en ACTV-41) */
  numero: number;
  titulo: string;
  descripcion: string | null;
  /** De un bug: Crítica, Alta... con su color; null si no tiene */
  severidad: { codigo: string; texto: string; tono: Tono } | null;
  /** Bug de producción que se corrige desde main y sale en un parche (PDF pág. 23) */
  hotfix: boolean;
  /** La Actividad termina con su PR aprobado; false en un proyecto Normal sin PR (el Desarrollador la termina) */
  requierePr: boolean;
  /** Si es un hotfix aprobado: la versión y los bugs que saldrían con él */
  parche: { version: string; bugs: { codigo: string; titulo: string }[] } | null;
  estado: string;
  etiqueta: Etiqueta;
  prioridad: Etiqueta;
  capas: string[];
  modulo: (InfoModulo & { nombre: string; tecnologia: string | null; ramaBase: string }) | null;
  responsable: { id: number; nombre: string; iniciales: string } | null;
  tester: { id: number; nombre: string; iniciales: string } | null;
  /** "6 h · 4 h registradas" */
  estimacion: string;
  fechaLimite: string;
  rama: string | null;
  bloqueadoPor: string | null;
  rechazosEnQa: number;
  dependencias: { codigo: string; titulo: string; etiqueta: Etiqueta }[];
  subtareas: { id: number; descripcion: string; hecha: boolean }[];
  subtareasHechas: number;
  pullRequests: { numero: number; titulo: string; estado: Etiqueta; ci: Etiqueta; url: string | null }[];
  criterios: { descripcion: string; verificado: boolean; soloEste: boolean }[];
  flujo: PasoDelFlujo[];
  historial: { texto: string; detalle: string; motivo: string | null; tono: Tono }[];
  /** Por qué y quién lo dejó en su estado actual (bloqueada, cancelada...) */
  motivoActual: { motivo: string; usuario: string | null; fecha: Date | null } | null;
  comentarios: { usuario: string; iniciales: string; tono: Tono; texto: string; cuando: string }[];
  actor: string;
  meToca: boolean;
  opciones: OpcionTicket[];
  puedeMarcarSubtareas: boolean;
  /** Las agregan y quitan quien lo trabaja o el Líder, hasta que se termina. */
  puedeEditarSubtareas: boolean;
  /** Sigue sin empezar y es de quien lo creó o del Líder. */
  puedeEditar: boolean;
  bloqueadores: { id: number; codigo: string; titulo: string }[];
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
}

/**
 * En las listas, las acciones que piden datos llevan al detalle con ?accion= para hacerse ahí:
 * "Confirmar" y "Publicar parche" de un bug (severidad, el parche completo, PDF pág. 23).
 */
export function accionEnDetalle(tipo: string, accion: string | null): 'confirmar' | 'parche' | null {
  if (tipo === 'BUGS') return accion === 'Confirmar' ? 'confirmar' : accion === 'Publicar parche' ? 'parche' : null;
  return null;
}
