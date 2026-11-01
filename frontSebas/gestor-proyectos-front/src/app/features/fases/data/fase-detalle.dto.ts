// JSON tal como lo manda el back en /equipo/proyectos/{id}/fases/{faseId} (GestionFaseDtos).

export interface DetalleFaseDto {
  info: InfoFaseDto;
  avance: number;
  ticketsTerminados: number;
  ticketsTotal: number;
  ticketsPorEstado: { nombre: string; cantidad: number }[];
  criterios: CriterioDto[];
  versionAnterior: string | null;
  bugReversion: { id: number; codigo: string; abierto: boolean } | null;
  rol: string | null;
  esResponsable: boolean;
  acciones: { accion: string; texto: string; impedimentos: string[] }[];
  puedeMarcarCriterios: boolean;
  puedeCrearHistoria: boolean;
  puedeEditar: boolean;
  puedeEliminar: boolean;
  historiasEliminables: number[];
}

export interface InfoFaseDto {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  proyectoCodigo: string;
  clienteNombre: string;
  numero: number;
  nombre: string;
  objetivo: string | null;
  estado: string;
  prioridad: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  responsableNombre: string;
  versionRelease: string | null;
  mantenimiento: boolean;
  figmaUrl: string | null;
  figmaParaTodas: boolean;
  motivoEstado: string | null;
  reanudarEl: string | null;
  enEstadoDesde: string | null;
  cambioEstadoPor: string | null;
  modulos: { tipo: string; nombre: string }[];
  historias: {
    id: number;
    numero: number;
    titulo: string;
    estado: string;
    terminados: number;
    total: number;
    analistaIniciales: string | null;
    incompleta: boolean;
    disenoPropio: boolean;
  }[];
  actividad: { descripcion: string; clase: string; fecha: string }[];
  aCancelar: { historias: number; tickets: number };
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
}

export interface CriterioDto {
  id: number;
  descripcion: string;
  manual: boolean;
  cumplido: boolean;
  detalle: string | null;
  cumplidoPor: string | null;
  cumplidoEn: string | null;
}

export interface AccionFaseRequestDto {
  accion: string;
  estadoEsperado: string;
  motivo: string | null;
  reanudarEl: string | null;
  versionConfirmada: string | null;
  asignarBugA: number | null;
}

export interface ResultadoAccionDto {
  accion: string;
  estadoNuevo: string;
  bugCreado: string | null;
}

export interface AccionDeListaDto {
  faseId: number;
  numero: number;
  nombre: string;
  estado: string;
  accion: string;
  texto: string;
  impedimento: string | null;
}

/** El release de la fase (PDF pág. 27). */
export interface ReleaseFaseDto {
  info: {
    version: string;
    estado: string | null;
    publicadoEn: string | null;
    publicadoPor: string | null;
    enProduccion: { version: string; fecha: string | null } | null;
    liderNombre: string;
    modulos: { tipo: string; nombre: string; tecnologia: string | null }[];
    enviarNotas: boolean;
    cambios: { tipo: string; codigo: string; titulo: string }[];
    demo: { estado: string; revisadaPor: string | null; revisadaEn: string | null } | null;
  };
  notas: string;
  puedeEditarNotas: boolean;
}
