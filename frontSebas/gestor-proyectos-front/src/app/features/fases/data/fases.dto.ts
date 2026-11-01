// JSON de /api/equipo/proyectos/{id}/fases (ver FasesDtos.java).

export interface PreparacionDto {
  proyectoId: number;
  proyectoNombre: string;
  numero: number;
  versionSugerida: string;
  inicioSugerido: string;
  liderId: number;
  hayFaseActiva: boolean;
  faseActiva: string | null;
  modulos: { id: number; tipo: string; nombre: string; tecnologia: string | null }[];
  equipo: { id: number; nombreCompleto: string; iniciales: string; roles: string[] }[];
  criteriosAutomaticos: string[];
}

export interface NuevaFaseDto {
  nombre: string;
  objetivo: string | null;
  fechaInicio: string;
  fechaFin: string;
  responsableId: number;
  versionRelease: string | null;
  prioridad: string;
  moduloIds: number[];
  criteriosManuales: string[];
}

export interface EdicionDto {
  preparacion: PreparacionDto;
  fase: {
    id: number;
    numero: number;
    estado: string;
    nombre: string;
    objetivo: string | null;
    fechaInicio: string;
    fechaFin: string;
    responsableId: number;
    versionRelease: string | null;
    prioridad: string;
    moduloIds: number[];
    criteriosManuales: string[];
    modulosEnUso: number[];
  };
}
