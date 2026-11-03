import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';
import { FormaTrabajo } from '../../../shared/utils/flujos';
import { InfoModulo } from '../../../shared/utils/proyectos';

/** Lo que el Líder puede hacerle a una fase (PDF pág. 20). Las mismas del back (AccionFase). */
export type AccionFase =
  | 'INICIAR'
  | 'PAUSAR'
  | 'REANUDAR'
  | 'CANCELAR'
  | 'CERRAR'
  | 'SUBIR_A_MAIN'
  | 'DESPLEGAR'
  | 'REVERTIR'
  | 'PUBLICAR_DE_NUEVO'
  | 'REABRIR';

/** Las que abren una ventana de confirmación antes de ejecutarse (PDF pág. 25). */
export type AccionConConfirmacion = Extract<AccionFase, 'PAUSAR' | 'CANCELAR' | 'SUBIR_A_MAIN' | 'REVERTIR'>;

export const ACCIONES_CON_CONFIRMACION: readonly AccionFase[] = ['PAUSAR', 'CANCELAR', 'SUBIR_A_MAIN', 'REVERTIR'];

export interface AccionDisponible {
  accion: AccionFase;
  texto: string;
  /** Lo que falta para habilitarla ("Para cerrar faltan 2 criterios"); vacío si ya se puede. */
  impedimentos: string[];
}

export interface Criterio {
  id: number;
  descripcion: string;
  manual: boolean;
  cumplido: boolean;
  /** "1 de 3 aceptadas", "Automático"; en un manual cumplido, "Manual · Jorge, 29 jul" */
  detalle: string | null;
}

/** El detalle de la fase (PDF pág. 32) ya listo para mostrarse. */
export interface DetalleFase {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  proyectoCodigo: string;
  /** Gitflow o Normal; las dos suben develop a main al terminar la fase */
  formaTrabajo: FormaTrabajo;
  clienteNombre: string;
  numero: number;
  /** "Fase 4 · Portal móvil" */
  titulo: string;
  objetivo: string | null;
  estado: string;
  etiqueta: Etiqueta;
  prioridad: Etiqueta;
  fechaInicio: Date | null;
  fechaFin: Date | null;
  /** null si no hay fecha fin o ya pasó */
  diasRestantes: number | null;
  responsableNombre: string;
  versionRelease: string | null;
  versionAnterior: string | null;
  mantenimiento: boolean;
  motivoEstado: string | null;
  reanudarEl: Date | null;
  enEstadoDesde: Date | null;
  cambioEstadoPor: string | null;
  modulos: (InfoModulo & { nombre: string })[];
  /** "portal-api y portal-app" */
  repositorios: string;
  /** Un solo enlace de Figma para toda la fase. */
  diseno: DisenoFase;
  avance: number;
  ticketsTerminados: number;
  ticketsTotal: number;
  historias: HistoriaDeFase[];
  /** Historias integradas (o cerradas) de las que cuentan, para el panel de Lista para release. */
  historiasIntegradas: number;
  historiasQueCuentan: number;
  criterios: Criterio[];
  criteriosCumplidos: number;
  ticketsPorEstado: { nombre: string; cantidad: number; tono: Tono }[];
  actividad: { descripcion: string; cuando: string; tono: Tono }[];
  aCancelar: { historias: number; tickets: number };
  /** A quién se puede asignar el BUGS al revertir; los Desarrolladores primero. */
  equipo: { id: number; nombreCompleto: string; roles: string }[];
  bugReversion: { codigo: string; abierto: boolean } | null;
  /** "Líder" o "Admin" si tiene botones; null si solo consulta */
  rol: string | null;
  esResponsable: boolean;
  acciones: AccionDisponible[];
  puedeMarcarCriterios: boolean;
  puedeCrearHistoria: boolean;
  /** Líder o responsable, mientras la fase no llega al release. */
  puedeEditar: boolean;
  /** Planeada, nada iniciado y es el Líder o un Admin: se puede borrar con sus historias. */
  puedeEliminar: boolean;
}

export interface HistoriaDeFase {
  id: number;
  codigo: string;
  titulo: string;
  estado: string;
  estadoTexto: string;
  /** "3/3 tickets", "Sin tickets" */
  tickets: string;
  analistaIniciales: string | null;
  /** Le falta algo para marcarla Lista (punto ámbar). */
  incompleta: boolean;
  /** Aún no se inicia y quien la ve es el Líder o un Admin: se puede borrar desde la lista. */
  eliminable: boolean;
}

export interface DisenoFase {
  figmaUrl: string | null;
  /** Las historias sin diseño propio lo heredan. */
  paraTodas: boolean;
  /** Cuántas historias usan el de la fase, de {@link total}. */
  loUsan: number;
  total: number;
}

/** Lo que se captura en las confirmaciones (PDF pág. 25). */
export interface DatosAccion {
  motivo?: string;
  reanudarEl?: string;
  versionConfirmada?: string;
  asignarBugA?: number;
}

export interface ResultadoAccion {
  accion: string;
  estadoNuevo: string;
  bugCreado: string | null;
}

/** El botón de una fase en el árbol del proyecto (PDF pág. 20, "Qué ve cada uno en la lista"). */
export interface AccionDeLista {
  faseId: number;
  /** "Fase 4 · Portal móvil" */
  titulo: string;
  estado: string;
  etiqueta: Etiqueta;
  accion: AccionFase;
  texto: string;
  impedimento: string | null;
}

/** El release de la fase (PDF pág. 27) ya listo para mostrarse. */
export interface ReleaseFase {
  version: string;
  /** PREPARANDO, PUBLICADA, REVERTIDA; null si aún no se prepara */
  estado: string | null;
  /** "31 jul 2026 · Jorge Sebastián" si ya se publicó */
  publicado: string | null;
  /** "v1.2.0 (30 abr)" */
  enProduccion: string | null;
  liderNombre: string;
  modulos: (InfoModulo & { nombre: string; tecnologia: string })[];
  enviarNotas: boolean;
  /** Los tickets que entran en las notas, por sección: Nuevo, Mejorado, Corregido */
  secciones: { titulo: string; cambios: { tipo: string; titulo: string }[] }[];
  /** "Mariana Torres · visto bueno en demo"; null si el cliente aún no la revisa */
  vistoBuenoCliente: string | null;
  notas: string;
  /** Si el Líder ya escribió las suyas (si no, se ven las armadas con los tickets) */
  notasEditadas: boolean;
  puedeEditarNotas: boolean;
}
