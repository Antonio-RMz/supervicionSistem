/** JSON de GET /api/equipo/proyectos/{id}/modulos (PDF pág. 35). */
export interface ModulosDto {
  modulos: ModuloDto[];
  /** Tipos que el proyecto aún no tiene */
  faltan: string[];
  puedeGestionar: boolean;
}

export interface ModuloDto {
  id: number;
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  tecnologias: string[];
  repositorio: string | null;
  ramaBase: string;
  responsable: { id: number; nombre: string } | null;
  ambientes: { nombre: string; version: string | null; fecha: string | null }[];
  ultimoBuild: { estado: 'PASO' | 'FALLO' | 'PENDIENTE'; fecha: string | null } | null;
  porHacer: number;
  enProgreso: number;
  enQa: number;
  bugsProduccion: number;
  conTrabajo: boolean;
}

export interface ModuloRequestDto {
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  repositorio: string | null;
  ramaBase: string | null;
  responsableId: number | null;
}
