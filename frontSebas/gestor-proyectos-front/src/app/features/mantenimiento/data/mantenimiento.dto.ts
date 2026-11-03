/** JSON de GET /api/equipo/proyectos/{id}/mantenimiento (PDF pág. 37). */
export interface MantenimientoDto {
  faseId: number;
  enProduccion: string | null;
  siguienteParche: string;
  urgentesConfirmados: number;
  criticos: number;
  urgentes: TicketMantenimientoDto[];
  listos: TicketMantenimientoDto[];
  normales: TicketMantenimientoDto[];
  proximaFaseNumero: number | null;
  horasMedioHotfix: number | null;
  parches: { version: string; fecha: string | null; bugs: number }[];
  puedePublicar: boolean;
}

export interface TicketMantenimientoDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  /** CRITICA, ALTA, MEDIA o BAJA; null si aún no se confirma */
  severidad: string | null;
  rama: string | null;
  responsableId: number | null;
  responsableNombre: string | null;
  porConfirmar: boolean;
  /** null si se reportó solo con imagen */
  descripcion: string | null;
  /** la primera imagen, para la miniatura; null si no tiene */
  imagenId: number | null;
  imagenes: number;
}

/** JSON de GET /api/equipo/proyectos/{id}/mantenimiento/nuevo: lo que piden los modales (PDF págs. 38 y 39). */
export interface PreparacionMantenimientoDto {
  proyectoId: number;
  faseId: number;
  liderId: number;
  enProduccion: string | null;
  proximaFaseNumero: number | null;
  modulos: { id: number; tipo: string; nombre: string; tecnologia: string | null; repositorio: string | null; ramaBase: string }[];
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
  /** número que tendría un ticket de cada tipo: { BUGS: 53, ... } */
  siguientes: Record<string, number>;
  /** para "Viene de"; codigo "HU-0017" */
  historias: { id: number; codigo: string; titulo: string }[];
  puedeCrear: boolean;
  puedeReportar: boolean;
}

/** POST /api/equipo/proyectos/{id}/mantenimiento/tickets (PDF pág. 38). */
export interface NuevoTicketMantenimientoRequest {
  tipo: string;
  titulo: string;
  descripcion: string;
  moduloId: number;
  prioridad: string;
  /** solo de un bug menor: MEDIA o BAJA */
  severidad: string | null;
  responsableId: number | null;
}

/** POST /api/equipo/proyectos/{id}/mantenimiento/fallas (PDF pág. 39). */
export interface ReporteFallaRequest {
  titulo: string;
  moduloId: number;
  /** provisional: CRITICA, ALTA, MEDIA o BAJA */
  severidad: string;
  /** opcional si el reporte lleva imagen */
  descripcion: string | null;
  historiaOrigenId: number | null;
}
