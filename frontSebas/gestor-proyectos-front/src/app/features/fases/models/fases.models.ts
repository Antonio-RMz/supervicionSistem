import { InfoModulo } from '../../../shared/utils/proyectos';

export interface ModuloFase extends InfoModulo {
  id: number;
  tipo: string;
  nombre: string;
  tecnologia: string;
}

/** Lo que el formulario de Nueva fase necesita del proyecto (PDF pág. 17). */
export interface PreparacionFase {
  proyectoId: number;
  proyectoNombre: string;
  numero: number;
  versionSugerida: string;
  /** "2026-06-01", listo para un <input type="date"> */
  inicioSugerido: string;
  liderId: number;
  /** Si ya hay una en curso, la nueva espera su turno para iniciar. */
  faseActiva: string | null;
  modulos: ModuloFase[];
  equipo: { id: number; nombreCompleto: string; roles: string }[];
  criteriosAutomaticos: string[];
}

export interface NuevaFase {
  nombre: string;
  objetivo: string;
  fechaInicio: string;
  fechaFin: string;
  responsableId: number | null;
  versionRelease: string;
  prioridad: string;
  moduloIds: number[];
  criteriosManuales: string[];
}

/** Editar fase: cómo está guardada hoy. */
export interface FaseGuardada {
  id: number;
  numero: number;
  estado: string;
  datos: NuevaFase;
  /** Módulos que ya usan sus historias o tickets: no se pueden quitar. */
  modulosEnUso: number[];
}

export interface EdicionFase {
  preparacion: PreparacionFase;
  fase: FaseGuardada;
}
