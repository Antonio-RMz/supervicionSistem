// ── Catálogos ─────────────────────────────────────────────────────────────────
export interface CatalogoProyectoResponse {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
}

export interface ClienteProyectoResponse {
  id: number;
  nombre: string;
  empresa: string | null;
  email: string | null;
  telefono: string | null;
}

export interface UsuarioProyectoResponse {
  id: number;
  nombre: string;
  apellido: string;
  nombreCompleto: string;
  email: string;
  iniciales: string;
}

// ── Métricas ──────────────────────────────────────────────────────────────────
export interface ProyectoMetricasResponse {
  totalTickets: number;
  ticketsPendientes: number;
  ticketsEnProgreso: number;
  ticketsEnRevision: number;
  ticketsEnQA: number;
  ticketsFinalizados: number;
  ticketsCancelados: number;
  ticketsBloqueados: number;
  totalFases: number;
  totalHistorias: number;
  totalActividades: number;
  totalBugs: number;
  totalMejoras: number;
  totalTareasTecnicas: number;
  totalInvestigaciones: number;
  totalDocumentaciones: number;
  ticketsVencidos: number;
  ticketsSinResponsable: number;
  porcentajeProgreso: number;
}

// ── Estructura ────────────────────────────────────────────────────────────────
export interface ActividadProyectoResumenResponse {
  id: number;
  folio: string;
  titulo: string;
  descripcion: string | null;
  estado: CatalogoProyectoResponse | null;
  prioridad: CatalogoProyectoResponse | null;
  responsable: UsuarioProyectoResponse | null;
  horasEstimadas: number | null;
  horasRegistradas: number | null;
  fechaInicio: string | null;
  fechaVencimiento: string | null;
  fechaFin: string | null;
}

export interface HistoriaProyectoResponse {
  id: number;
  folio: string;
  titulo: string;
  descripcion: string | null;
  estado: CatalogoProyectoResponse | null;
  prioridad: CatalogoProyectoResponse | null;
  responsable: UsuarioProyectoResponse | null;
  fechaInicio: string | null;
  fechaVencimiento: string | null;
  fechaFin: string | null;
  porcentajeProgreso: number;
  totalActividades: number;
  actividadesFinalizadas: number;
  actividades: ActividadProyectoResumenResponse[];
}

export interface FaseProyectoResponse {
  id: number;
  folio: string;
  titulo: string;
  descripcion: string | null;
  estado: CatalogoProyectoResponse | null;
  prioridad: CatalogoProyectoResponse | null;
  responsable: UsuarioProyectoResponse | null;
  fechaInicio: string | null;
  fechaVencimiento: string | null;
  fechaFin: string | null;
  porcentajeProgreso: number;
  totalHistorias: number;
  historiasFinalizadas: number;
  historias: HistoriaProyectoResponse[];
}

export interface ProyectoEstructuraResponse {
  fases: FaseProyectoResponse[];
  ticketsSinPadre: TicketProyectoResumenResponse[];
}

// ── Tickets ───────────────────────────────────────────────────────────────────
export interface TicketProyectoResumenResponse {
  id: number;
  folio: string;
  titulo: string;
  descripcionCorta: string | null;
  tipo: CatalogoProyectoResponse | null;
  estado: CatalogoProyectoResponse | null;
  prioridad: CatalogoProyectoResponse | null;
  responsable: UsuarioProyectoResponse | null;
  folioTicketPadre: string | null;
  tituloTicketPadre: string | null;
  fechaInicio: string | null;
  fechaVencimiento: string | null;
  fechaFin: string | null;
  fechaCreacion: string | null;
  fechaActualizacion: string | null;
}

export interface TicketProyectoFiltroRequest {
  idTipoTicket: number | null;
  idEstadoTicket: number | null;
  idPrioridadTicket: number | null;
  idResponsable: number | null;
  idTicketPadre: number | null;
  folio: string | null;
  texto: string | null;
  fechaDesde: string | null;
  fechaHasta: string | null;
  soloMisTickets: boolean | null;
  soloVencidos: boolean | null;
  pagina: number | null;
  tamanio: number | null;
  ordenarPor: string | null;
  direccion: string | null;
}

export interface PaginaResponse<T> {
  contenido: T[];
  paginaActual: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  primera: boolean;
  ultima: boolean;
}

// ── Equipo ────────────────────────────────────────────────────────────────────
export interface MiembroProyectoResponse {
  idUsuario: number;
  nombre: string;
  apellido: string;
  nombreCompleto: string;
  email: string;
  iniciales: string;
  idRol: number;
  nombreRol: string;
  activo: boolean;
  ticketsAsignados: number;
  ticketsEnProgreso: number;
  ticketsVencidos: number;
}

// ── Actividad reciente ────────────────────────────────────────────────────────
export interface ActividadProyectoResponse {
  id: number;
  tipoActividad: string;
  descripcion: string;
  idTicket: number | null;
  folioTicket: string | null;
  tituloTicket: string | null;
  usuario: UsuarioProyectoResponse | null;
  valorAnterior: string | null;
  valorNuevo: string | null;
  fechaActividad: string;
}

// ── Documentos ────────────────────────────────────────────────────────────────
export interface DocumentoProyectoResponse {
  id: number;
  idTicket: number | null;
  folioTicket: string | null;
  nombre: string;
  nombreOriginal: string;
  tipoMime: string;
  extension: string;
  tamanioBytes: number;
  rutaArchivo: string;
  usuarioCarga: UsuarioProyectoResponse | null;
  fechaCarga: string;
}

// ── Detalle completo ──────────────────────────────────────────────────────────
export interface ProyectoDetalleResponse {
  id: number;
  nombre: string;
  iniciales: string;
  descripcion: string | null;
  objetivo: string | null;
  alcance: string | null;
  categoria: string | null;
  imagenUrl: string | null;
  githubRepoUrl: string | null;
  produccionUrl: string | null;
  qaUrl: string | null;
  tipo: CatalogoProyectoResponse | null;
  estado: CatalogoProyectoResponse | null;
  prioridad: CatalogoProyectoResponse | null;
  cliente: ClienteProyectoResponse | null;
  fechaInicio: string | null;
  fechaFinEstimada: string | null;
  fechaFinReal: string | null;
  progresoPorcentaje: number;
  metricas: ProyectoMetricasResponse | null;
  estructura: ProyectoEstructuraResponse | null;
  equipo: MiembroProyectoResponse[];
  ticketsRecientes: TicketProyectoResumenResponse[];
  actividadReciente: ActividadProyectoResponse[];
  documentos: DocumentoProyectoResponse[];
  fechaCreacion: string | null;
  fechaActualizacion: string | null;
}

export interface ProyectoResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
  color: string;
  iniciales: string;
  progreso: number;
  usuarios: number;
  estado: string;
  categoria: string | null;
  urlQa?: string | null;
  urlProduccion?: string | null;
  // Campos extendidos (pueden venir null si el backend aún no los persiste)
  cliente?: string | null;
  tipoProyectoId?: number | null;
  estadoProyectoId?: number | null;
  prioridadProyectoId?: number | null;
  icono?: string | null;
  repoUrl?: string | null;
  appUrl?: string | null;
}

export interface ProyectoRequest {
  nombre: string;
  descripcion?: string | null;
  alcance?: string | null;
  categoria?: string | null;
  imagenUrl?: string | null;
  estado?: string;
  equipoId?: number | null;
  qaUrl?: string | null;
  produccionUrl?: string | null;
  // Solo para el modal de edición legacy (pro.component.ts)
  progreso?: number;
}

export interface ProyectoEditarRequest {
  nombre: string;
  descripcion: string | null;
  cliente: string | null;
  tipoProyectoId: number;
  estadoProyectoId: number;
  prioridadProyectoId: number;
  icono: string | null;
  repoUrl: string | null;
  appUrl: string | null;
}

export interface ProyectoNuevoRequest {
  nombre: string;
  cliente: string | null;
  descripcion: string | null;
  tipoProyectoId: number;
  estadoProyectoId: number;
  prioridadProyectoId: number;
}

export interface Fase {
  id: number;
  numero: number;
  nombre: string;
  descripcion: string | null;
  color: string;
  progreso: number;
  historiasCompletadas: number;
  historiasTotal: number;
  actividadesCompletadas: number;
  actividadesTotal: number;
  estado: string;
}

export type Prioridad = 'ALTA' | 'MEDIA' | 'BAJA';

export interface Historia {
  id: number;
  codigo: string;
  titulo: string;
  prioridad: Prioridad;
  estado: string;
  faseId: number;
}

export interface ResumenConteo {
  total: number;
  completadas: number;
  enProgreso: number;
  pendientes: number;
}

export interface ResumenProyectoStats {
  fases: number;
  historias: ResumenConteo;
  actividades: ResumenConteo;
  miembros: number;
  progresoGeneral: number;
}
