export interface EjecutarAccionRequest {
  idUsuarioResponsable?: number;
  comentario?: string;
}

export interface CrearFaseRequest {
  proyectoId: number;
  titulo: string;
  descripcion: string;
  prioridadId: number;
  fechaInicioEstimada: string;
  fechaFinEstimada: string;
}

export interface CrearHistoriaRequest {
  titulo: string;
  descripcion: string;
  idPrioridadTicket: number;
  idUsuarioResponsable?: number | null;
  fechaInicio?: string | null;
  fechaVencimiento?: string | null;
}

export interface UsuarioComentarioResponse {
  id:             number;
  nombre:         string;
  apellido:       string;
  nombreCompleto: string;
  avatarUrl?:     string | null;
}

export interface TicketComentarioResponse {
  id:            number;
  comentario:    string;
  fechaCreacion: string;
  usuario:       UsuarioComentarioResponse;
}

export interface AccionDisponible {
  id:                  number;
  codigo:              string;
  nombre:              string;
  idEstadoDestino:     number;
  codigoEstadoDestino: string;
  nombreEstadoDestino: string;
  requiereResponsable: boolean;
}

export interface TicketRelacionadoResponse {
  id:         number;
  folio:      string;
  titulo:     string;
  tipoTicket: string;
  estado:     string;
}

export interface ResponsableDisponibleResponse {
  id:          number;
  nombreCompleto: string;
  correo:      string;
  codigoRol:   string;
  nombreRol:   string;
}

export interface TicketFaseRequerimientoResponse {
  id: number;
  codigo: string;
  titulo: string;
  prioridad: string | null;
  estado: string | null;
}

export interface TicketFaseReglaResponse {
  id: number;
  codigo: string;
  descripcion: string;
}

export interface TicketFaseCriterioResponse {
  id: number;
  descripcion: string;
}

export interface TicketFaseDefinicionResponse {
  id: number | null;
  idTicket: number;
  objetivo: string | null;
  incluye: string | null;
  fueraAlcance: string | null;
  observaciones: string | null;
  requerimientos: TicketFaseRequerimientoResponse[];
  reglas: TicketFaseReglaResponse[];
  criterios: TicketFaseCriterioResponse[];
}

export interface ActualizarObjetivoFaseRequest {
  objetivo: string;
}

export interface ActualizarAlcanceFaseRequest {
  incluye: string | null;
  fueraAlcance: string | null;
}

export interface CrearRequerimientoFaseRequest {
  codigo: string;
  titulo: string;
  prioridad: string | null;
  estado: string | null;
}

export interface CrearReglaFaseRequest {
  codigo: string;
  descripcion: string;
}

export interface CrearCriterioFaseRequest {
  descripcion: string;
}

export interface TicketDetalleApi {
  id:                  number;
  folio:               string;
  titulo:              string;
  descripcion:         string | null;
  tipo:                { id: number; codigo: string; nombre: string };
  estado:              { id: number; codigo: string; nombre: string };
  prioridad:           { id: number; codigo: string; nombre: string } | null;
  usuarioCreador:      { id: number; nombre: string } | null;
  usuarioResponsable:  { id: number; nombre: string } | null;
  fechaInicio:         string | null;
  fechaFin:            string | null;
  fechaVencimiento:    string | null;
  fechaCreacion?:      string | null;
  fechaActualizacion?: string | null;
  accionesDisponibles: AccionDisponible[];
  ticketPadre:         TicketRelacionadoResponse | null;
  ticketsHijos:        TicketRelacionadoResponse[];
  comentarios?:        TicketComentarioResponse[];
  // Posibles estructuras que devuelve el backend para identificar el proyecto
  proyectoId?: number;
  proyecto?:   { id: number; nombre?: string };
}
