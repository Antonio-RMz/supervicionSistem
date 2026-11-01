import { InfoModulo } from '../../../shared/utils/proyectos';

export interface ModuloTicket extends InfoModulo {
  id: number;
  tipo: string;
  nombre: string;
  tecnologia: string;
  repositorio: string;
  ramaBase: string;
}

export interface HistoriaTicket {
  id: number;
  codigo: string;
  titulo: string;
  estado: string;
  /** "Como cliente con contrato activo, quiero ..., para ..." */
  enunciado: string;
  analistaNombre: string;
  moduloIds: number[];
  tickets: number;
  criterios: { id: number; descripcion: string; cubiertoPor: string | null }[];
}

/** Lo que el formulario de Nuevo ticket necesita (PDF pág. 19). */
export interface PreparacionTicket {
  proyectoId: number;
  proyectoNombre: string;
  liderId: number;
  fases: { id: number; numero: number; titulo: string; modulos: ModuloTicket[]; historias: HistoriaTicket[] }[];
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
  siguientes: Record<string, number>;
  abiertos: { id: number; codigo: string; titulo: string; estadoTexto: string }[];
  capas: string[];
}

export interface NuevoTicket {
  faseId: number | null;
  historiaId: number | null;
  tipo: string;
  titulo: string;
  descripcion: string;
  moduloId: number | null;
  capas: string[];
  criterioIds: number[];
  subtareas: string[];
  dependeDe: number[];
  responsableId: number | null;
  testerId: number | null;
  prioridad: string;
  estimacionHoras: number | null;
  fechaLimite: string;
}

/** Editar ticket: el formulario de Nuevo ticket con lo que ya tiene. Tipo, fase e historia no cambian. */
export interface EdicionTicket {
  preparacion: PreparacionTicket;
  codigo: string;
  datos: NuevoTicket;
}
