// JSON de /api/equipo/proyectos/... (ver ProyectosDtos.java).

export interface MiembroDto {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  roles: string[];
}

export interface TarjetaDto {
  id: number;
  codigo: string;
  nombre: string;
  estado: string;
  prioridad: string;
  /** null = proyecto interno */
  clienteId: number | null;
  clienteNombre: string;
  modulos: string[];
  avance: number;
  fases: number;
  historias: number;
  tickets: number;
  equipo: MiembroDto[];
  liderNombre: string;
}

export interface ListadoDto {
  proyectos: TarjetaDto[];
  puedeCrear: boolean;
}

export interface ReglasDto {
  permitirAutovalidacion: boolean;
  comentarioAlRechazar: boolean;
  ramaAlIniciar: boolean;
  notificarClienteFase: boolean;
}

export interface InfoDto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  estado: string;
  /** null = proyecto interno */
  clienteId: number | null;
  clienteNombre: string;
  prioridad: string;
  fechaInicio: string | null;
  fechaEntrega: string | null;
  liderId: number;
  liderNombre: string;
  reglas: ReglasDto;
}

export interface ModuloDto {
  id: number;
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  repositorio: string | null;
  ramaBase: string;
  ciEstado: string | null;
  ciFecha: string | null;
}

export interface FaseDto {
  id: number;
  numero: number;
  nombre: string;
  estado: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  versionRelease: string | null;
  mantenimiento: boolean;
  modulos: string[];
  historias: number;
  tickets: number;
  avance: number;
}

export interface ResumenDto {
  info: InfoDto;
  avance: number;
  modulos: ModuloDto[];
  equipo: MiembroDto[];
  fases: FaseDto[];
  faseActual: FaseDto | null;
  ticketsAbiertos: number;
  bugsAbiertos: number;
  bugsEnProduccion: number;
  mantenimientoAbiertos: number;
  ticketsListos: number;
  ticketsFaseActual: number;
  ritmo: 'EN_TIEMPO' | 'EN_RIESGO' | 'ATRASADO' | 'SIN_FECHAS';
  salud: {
    rechazosQaMes: number;
    diasPromedioEnQa: number | null;
    horasEstimadas: number;
    horasReales: number;
    historiasSinTickets: number;
  };
  detenidos: { ticketId: number; codigo: string; titulo: string; estado: string; desde: string | null; motivo: string | null }[];
  puedeEditar: boolean;
  puedeCrearFase: boolean;
  puedeCrearHistoria: boolean;
  puedeCrearTicket: boolean;
}

export interface TicketArbolDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  modulo: string | null;
  responsableId: number | null;
  responsableIniciales: string | null;
  responsableNombre: string | null;
  accion: string | null;
  quien: string | null;
}

export interface HistoriaArbolDto {
  id: number;
  codigo: string;
  titulo: string;
  estado: string;
  analistaId: number;
  analistaIniciales: string;
  listos: number;
  total: number;
  tickets: TicketArbolDto[];
}

export interface FaseArbolDto {
  fase: FaseDto;
  historias: HistoriaArbolDto[];
  sueltos: TicketArbolDto[];
}

export interface ModuloFormDto {
  id: number | null;
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  repositorio: string | null;
  ramaBase: string | null;
}

/** Lo que se manda al crear o editar. */
export interface ProyectoRequestDto {
  nombre: string;
  /** null = proyecto interno */
  clienteId: number | null;
  prioridad: string;
  estado: string | null;
  fechaInicio: string | null;
  fechaEntrega: string | null;
  descripcion: string | null;
  liderId: number;
  miembroIds: number[];
  modulos: ModuloFormDto[];
  reglas: ReglasDto;
}

export interface ConfiguracionDto extends ProyectoRequestDto {
  id: number;
  codigo: string;
}

export interface PersonaDto {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  roles: string[];
  proyectos: number;
  activo: boolean;
}
