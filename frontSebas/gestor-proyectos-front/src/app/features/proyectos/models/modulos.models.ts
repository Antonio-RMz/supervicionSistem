import { Tono } from '../../../shared/models/etiqueta.model';
import { InfoModulo } from '../../../shared/utils/proyectos';

/** La pestaña Módulos del proyecto (PDF pág. 35), lista para pintarse. */
export interface ModulosProyecto {
  modulos: TarjetaModulo[];
  faltan: { tipo: string; info: InfoModulo }[];
  puedeGestionar: boolean;
}

export interface TarjetaModulo {
  id: number;
  tipo: string;
  info: InfoModulo;
  nombre: string;
  tecnologia: string | null;
  tecnologias: string[];
  repositorio: string | null;
  /** "https://github.com/equipo/portal-api" para abrirlo */
  repositorioUrl: string | null;
  ramaBase: string;
  responsable: { id: number; nombre: string } | null;
  /** "1.3.0-rc3 · hace 2 h"; "Sin integraciones de la fase" */
  ambientes: { nombre: string; tono: Tono; version: string | null; texto: string }[];
  /** "Último build · pasó · hace 2 h" */
  build: { texto: string; tono: 'verde' | 'rojo' | 'ambar' } | null;
  porHacer: number;
  enProgreso: number;
  enQa: number;
  bugsProduccion: number;
  conTrabajo: boolean;
  /** "staging · hace 2 h" */
  ultimoDespliegue: string;
}

/** Lo que se captura al agregar o editar un módulo. */
export interface DatosModulo {
  tipo: string;
  nombre: string;
  tecnologia: string;
  repositorio: string;
  ramaBase: string;
  responsableId: number | null;
}
