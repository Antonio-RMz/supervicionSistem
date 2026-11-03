import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';
import { FormaTrabajo } from '../../../shared/utils/flujos';
import { InfoModulo } from '../../../shared/utils/proyectos';

export interface Miembro {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  /** Códigos ya ordenados: ADMIN, LIDER... */
  roles: string[];
  tono: Tono;
}

export interface ModuloChip extends InfoModulo {
  tipo: string;
}

/** Una tarjeta de la lista (PDF pág. 13). */
export interface ProyectoTarjeta {
  id: number;
  codigo: string;
  nombre: string;
  estado: string;
  etiqueta: Etiqueta;
  prioridad: string;
  /** null = proyecto interno */
  clienteId: number | null;
  clienteNombre: string;
  modulos: ModuloChip[];
  avance: number;
  tono: Tono;
  fases: number;
  historias: number;
  tickets: number;
  equipo: Miembro[];
  liderNombre: string;
}

export interface ListadoProyectos {
  proyectos: ProyectoTarjeta[];
  puedeCrear: boolean;
}

export interface Reglas {
  permitirAutovalidacion: boolean;
  comentarioAlRechazar: boolean;
  ramaAlIniciar: boolean;
  notificarClienteFase: boolean;
  /** Se elige al crear el proyecto y ya no cambia */
  formaTrabajo: FormaTrabajo;
  /** En Normal: la Actividad pasa por PR o se termina; en Gitflow siempre true */
  requierePr: boolean;
}

export interface ModuloProyecto extends ModuloChip {
  id: number;
  nombre: string;
  tecnologia: string;
  repositorio: string;
  ramaBase: string;
  /** "CI pasó" en verde, "CI falló" en rojo, "Sin despliegues" en gris */
  ci: { texto: string; tono: Tono; fecha: Date | null };
}

export interface FaseResumen {
  id: number;
  numero: number;
  /** "Fase 2 · Desarrollo" (o "Mantenimiento") */
  titulo: string;
  nombre: string;
  estado: string;
  etiqueta: Etiqueta;
  fechaInicio: Date | null;
  fechaFin: Date | null;
  /** "24 feb – 25 abr" */
  fechas: string;
  versionRelease: string | null;
  mantenimiento: boolean;
  modulos: ModuloChip[];
  historias: number;
  tickets: number;
  avance: number;
  tonoAvance: Tono;
}

export interface Detenido {
  ticketId: number;
  codigo: string;
  titulo: string;
  bloqueado: boolean;
  /** "Bloqueada 2 días · esperando credenciales" o "En QA hace 3 días" */
  detalle: string;
}

/** Encabezado y pestaña Resumen (PDF pág. 14). */
export interface ResumenProyecto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  estado: string;
  etiqueta: Etiqueta;
  clienteNombre: string;
  prioridadTexto: string;
  fechaInicio: Date | null;
  fechaEntrega: Date | null;
  /** "Grupo Ferretero León · Desarrollo web · Prioridad alta · 3 feb – 30 may 2026" */
  subtitulo: string;
  liderId: number;
  liderNombre: string;
  reglas: Reglas;
  avance: number;
  tono: Tono;
  modulos: ModuloProyecto[];
  equipo: Miembro[];
  fases: FaseResumen[];
  faseActual: FaseResumen | null;
  ticketsAbiertos: number;
  bugsAbiertos: number;
  bugsEnProduccion: number;
  mantenimientoAbiertos: number;
  ticketsListos: number;
  ticketsFaseActual: number;
  ritmo: { texto: string; detalle: string; tono: Tono };
  salud: {
    rechazosQaMes: number;
    diasPromedioEnQa: string;
    horas: string;
    historiasSinTickets: number;
  };
  detenidos: Detenido[];
  puedeEditar: boolean;
  puedeCrearFase: boolean;
  puedeCrearHistoria: boolean;
  puedeCrearTicket: boolean;
}

export interface TicketArbol {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  estadoTexto: string;
  modulo: ModuloChip | null;
  responsableId: number | null;
  responsable: { iniciales: string; tono: Tono; nombre: string } | null;
  accion: string | null;
  quien: string | null;
}

export interface HistoriaArbol {
  id: number;
  codigo: string;
  titulo: string;
  estado: string;
  estadoTexto: string;
  analistaId: number;
  analista: { iniciales: string; tono: Tono };
  /** Lista, Lista para desarrollo o En desarrollo: ya se le pueden crear tickets */
  aceptaTickets: boolean;
  listos: number;
  total: number;
  tickets: TicketArbol[];
}

export interface FaseArbol {
  fase: FaseResumen;
  historias: HistoriaArbol[];
  sueltos: TicketArbol[];
}

// ─── Formulario (PDF pág. 16) ───────────────────────────────────────────────

export interface ModuloForm {
  id: number | null;
  tipo: string;
  nombre: string;
  tecnologia: string;
  repositorio: string;
  ramaBase: string;
}

export interface DatosProyecto {
  nombre: string;
  /** null = proyecto interno */
  clienteId: number | null;
  prioridad: string;
  /** Solo al editar */
  estado: string | null;
  fechaInicio: string;
  fechaEntrega: string;
  descripcion: string;
  liderId: number | null;
  miembroIds: number[];
  modulos: ModuloForm[];
  reglas: Reglas;
}

export interface Persona {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  roles: string[];
  activo: boolean;
  tono: Tono;
}
