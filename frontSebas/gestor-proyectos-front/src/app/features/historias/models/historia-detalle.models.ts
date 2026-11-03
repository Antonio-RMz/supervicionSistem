import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';
import { FormaTrabajo } from '../../../shared/utils/flujos';
import { InfoModulo } from '../../../shared/utils/proyectos';

/** Las decisiones sobre una historia (PDF pág. 21). Las mismas del back (AccionHistoria). */
export type AccionHistoria =
  | 'INICIAR_PLANEACION'
  | 'MARCAR_LISTA'
  | 'REGRESAR_A_PLANEACION'
  | 'DESCARTAR'
  | 'MOVER_DE_FASE'
  | 'ACEPTAR'
  | 'DEVOLVER'
  | 'INTEGRAR'
  | 'REABRIR'
  // Historia con Desarrolladores (sin Actividades)
  | 'INICIAR_DESARROLLO'
  | 'TERMINAR_DESARROLLO'
  | 'APROBAR_REVISION'
  | 'PEDIR_CAMBIOS';

/** Las que abren una ventana de confirmación antes de ejecutarse (PDF pág. 33). */
export type AccionConConfirmacion = Extract<
  AccionHistoria,
  'DEVOLVER' | 'DESCARTAR' | 'MOVER_DE_FASE' | 'PEDIR_CAMBIOS'
>;

export const ACCIONES_CON_CONFIRMACION: readonly AccionHistoria[] = [
  'DEVOLVER', 'DESCARTAR', 'MOVER_DE_FASE', 'PEDIR_CAMBIOS',
];

/** Un Desarrollador de la historia y si ya terminó su parte. */
export interface DesarrolladorDeHistoria {
  usuarioId: number;
  nombre: string;
  iniciales: string;
  /** "sis-control-escolar-api"; null = en todos los repositorios */
  repositorio: string | null;
  terminado: boolean;
}

/** Un punto de "Lo que hice". */
export interface TrabajoDeHistoria {
  id: number;
  descripcion: string;
  repositorio: string | null;
  hecho: boolean;
  /** "Jorge · 2 nov" */
  detalle: string;
}

export interface RepositorioDeHistoria {
  id: number;
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  ramaBase: string;
  /** Para clonar: "https://github.com/.../repo.git"; null si el proyecto no lo tiene */
  url: string | null;
  /** Para abrirlo en el navegador (sin ".git") */
  enlace: string | null;
}

/** Pestaña Desarrollo: solo en historias con Desarrolladores (sin Actividades como ticket). */
export interface DesarrolloHistoria {
  desarrolladores: DesarrolladorDeHistoria[];
  trabajo: TrabajoDeHistoria[];
  repositorios: RepositorioDeHistoria[];
  /** Si quien la ve es uno de sus Desarrolladores: si ya terminó su parte; null si no lo es */
  miParteTerminada: boolean | null;
  puedeAnotar: boolean;
  puedeMarcar: boolean;
  /** El proyecto pide PR (o es Gitflow): al terminar la revisa el Líder */
  requierePr: boolean;
}

export interface AccionDisponible {
  accion: AccionHistoria;
  texto: string;
  impedimentos: string[];
}

export interface TicketDeHistoria {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  /** BORRADOR, POR_HACER... */
  estadoCodigo: string;
  estado: Etiqueta;
  modulo: InfoModulo | null;
  responsable: { nombre: string; iniciales: string } | null;
  dependeDe: string[];
}

/** El detalle de la historia (PDF pág. 24) ya listo para mostrarse. */
export interface DetalleHistoria {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  /** En la forma Normal no hay Integrar: aceptada ya queda integrada */
  formaTrabajo: FormaTrabajo;
  faseId: number;
  faseNumero: number;
  faseNombre: string;
  /** "Fase 4 · Portal móvil" */
  faseTitulo: string;
  faseEtiqueta: Etiqueta;
  /** "1 jun – 31 jul" */
  faseFechas: string;
  codigo: string;
  titulo: string;
  como: string | null;
  quiero: string | null;
  para: string | null;
  estado: string;
  etiqueta: Etiqueta;
  prioridad: Etiqueta;
  puntos: number | null;
  analistaNombre: string;
  figmaUrl: string | null;
  /** El enlace es el de su fase (no tiene diseño propio). */
  disenoDeFase: boolean;
  /** "Diseño aprobado por el cliente · Mariana Torres · 25 jun"; null si no se ha aprobado */
  disenoAprobado: string | null;
  modulos: InfoModulo[];
  criterios: { id: number; descripcion: string; verificado: boolean; tickets: string[] }[];
  criteriosVerificados: number;
  tickets: TicketDeHistoria[];
  ticketsAprobados: number;
  ticketsQueCuentan: number;
  historial: { texto: string; detalle: string; tono: Tono }[];
  /** Por qué y quién la dejó en su estado actual (Descartada, devuelta...); null si no hay motivo */
  motivoActual: { motivo: string; usuario: string | null } | null;
  /** Capturas o bocetos; el archivo se pide aparte */
  imagenes: ImagenDeHistoria[];
  comentarios: { usuario: string; iniciales: string; tono: Tono; texto: string; cuando: string }[];
  actor: string;
  meToca: boolean;
  acciones: AccionDisponible[];
  puedeVerificarCriterios: boolean;
  /** Su Analista o el Líder, mientras no tenga tickets. */
  puedeEditar: boolean;
  /** Su Analista o el Líder, hasta que se integra. */
  puedeAdjuntar: boolean;
  puedeCrearTicket: boolean;
  ticketsSinEmpezar: number;
  ticketsReabribles: { id: number; codigo: string; titulo: string }[];
  fasesDestino: { id: number; titulo: string }[];
  /** Aún no se inicia y es su Analista, el Líder o el Admin: se puede borrar del todo. */
  puedeEliminar: boolean;
  /** Con Desarrolladores (sin Actividades): sus Desarrolladores, "Lo que hice" y repositorios; null si no */
  desarrollo: DesarrolloHistoria | null;
}

export interface ImagenDeHistoria {
  id: number;
  nombre: string;
  /** "240 KB" */
  tamano: string;
}

/** Lo que se captura en las confirmaciones (PDF pág. 33). */
export interface DatosAccionHistoria {
  motivo?: string;
  ticketId?: number;
  crearBug?: boolean;
  faseDestinoId?: number;
}

export interface ResultadoAccionHistoria {
  accion: string;
  estadoNuevo: string;
  bugCreado: string | null;
}

/** El botón de una historia en las listas (PDF pág. 21, "Qué ve cada uno en la lista"). */
export interface AccionDeListaHistoria {
  historiaId: number;
  estado: string;
  accion: AccionHistoria;
  texto: string;
  impedimento: string | null;
}

/** El botón de una historia en el Dashboard y Mis tickets (FlujoTrabajo del back) -> su acción. */
const ACCIONES_DE_FLUJO: Record<string, AccionHistoria> = {
  'Iniciar planeación': 'INICIAR_PLANEACION',
  'Marcar como Lista': 'MARCAR_LISTA',
  Aceptar: 'ACEPTAR',
  Integrar: 'INTEGRAR',
};

export function accionDeFlujo(texto: string): AccionHistoria | null {
  return ACCIONES_DE_FLUJO[texto] ?? null;
}
