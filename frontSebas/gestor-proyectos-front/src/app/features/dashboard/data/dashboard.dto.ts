// JSON de GET /api/equipo/proyectos/{id}/dashboard (ver DashboardDtos.java).

export interface MetricasDto {
  ticketsAbiertos: number;
  abiertosEstaSemana: number;
  enQa: number;
  enQaMasDe3Dias: number;
  bugsAbiertos: number;
  bugsPrioridadAlta: number;
  historiasCerradasSemana: number;
  ticketsCerradosSemana: number;
}

export interface ItemPendienteDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  accion: string;
  actorNombre: string | null;
  actorRol: string | null;
  ejecutable: boolean;
}

export interface FaseDto {
  id: number;
  numero: number;
  nombre: string;
  estado: string;
  avance: number;
}

export interface MovimientoDto {
  descripcion: string;
  fecha: string;
}

export interface DashboardDto {
  proyectoId: number;
  proyectoNombre: string;
  metricas: MetricasDto;
  pendientesMios: number;
  teToca: ItemPendienteDto[];
  esperandoAOtros: ItemPendienteDto[];
  fases: FaseDto[];
  actividad: MovimientoDto[];
}
