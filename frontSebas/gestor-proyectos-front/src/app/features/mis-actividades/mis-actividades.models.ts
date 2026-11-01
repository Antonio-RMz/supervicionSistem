export interface ActividadTicketCodificado {
  id:     number;
  codigo: string;
  nombre: string;
}

export interface ActividadTicketResponsable {
  id:              number;
  nombre:          string;
  apellido?:       string | null;
  nombreCompleto?: string | null;
}

export interface ActividadTicket {
  id:                 number;
  folio:              string;
  titulo:             string;
  descripcion:        string | null;
  tipo:               ActividadTicketCodificado;
  estado:             ActividadTicketCodificado;
  prioridad:          ActividadTicketCodificado | null;
  usuarioResponsable: ActividadTicketResponsable | null;
  fechaVencimiento:   string | null;
  fechaCreacion:      string | null;
}

export interface ProyectoActividades {
  proyectoId:     number;
  nombreProyecto: string;
  totalTickets:   number;
  tickets:        ActividadTicket[];
}

export interface MisActividadesResponse {
  totalActividades: number;
  proyectos:        ProyectoActividades[];
}
