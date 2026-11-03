import { InfoModulo } from '../../../shared/utils/proyectos';

export interface FaseOpcion {
  id: number;
  numero: number;
  /** "Fase 4 · Portal móvil" */
  titulo: string;
  modulos: (InfoModulo & { id: number; tipo: string; nombre: string })[];
}

/** Lo que el formulario de Nueva historia necesita (PDF pág. 18). */
export interface PreparacionHistoria {
  proyectoId: number;
  proyectoNombre: string;
  /** "HU-0019" */
  codigo: string;
  fases: FaseOpcion[];
  /** Quienes pueden ser responsables: Analistas (primero) y Líderes */
  analistas: { id: number; nombreCompleto: string; esAnalista: boolean }[];
  /** Quiénes usan el sistema que se construye: las opciones del "Como" (pestaña Usuarios del sistema). */
  usuariosDelSistema: { nombre: string; descripcion: string | null }[];
  /** Los del equipo con rol Desarrollador: a quienes les llega la historia (sin Actividades como ticket). */
  desarrolladores: { id: number; nombreCompleto: string }[];
}

/** Un Desarrollador de la historia; moduloId null = en todos sus repositorios. */
export interface DesarrolladorAsignado {
  usuarioId: number;
  moduloId: number | null;
}

export interface NuevaHistoria {
  borrador: boolean;
  faseId: number | null;
  titulo: string;
  como: string;
  quiero: string;
  para: string;
  criterios: string[];
  figmaUrl: string;
  disenoAprobado: boolean;
  analistaId: number | null;
  prioridad: string;
  puntos: number | null;
  moduloIds: number[];
  desarrolladores: DesarrolladorAsignado[];
}

/** Editar historia: cómo está guardada hoy. */
export interface HistoriaGuardada {
  id: number;
  codigo: string;
  estado: string;
  datos: NuevaHistoria;
}

export interface EdicionHistoria {
  preparacion: PreparacionHistoria;
  historia: HistoriaGuardada;
}
