import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';
import { InfoModulo } from '../../../shared/utils/proyectos';

/** La pestaña Equipo del proyecto (PDF pág. 34), lista para pintarse. */
export interface EquipoProyecto {
  miembros: MiembroEquipo[];
  cobertura: { rol: string; estado: 'ok' | 'aviso' | 'falta'; quien: string }[];
  alertas: string[];
  permisos: { permiso: string; usuarioId: number; usuario: string; etiqueta: Etiqueta }[];
  /** "Autovalidación · Permitida", ... */
  reglas: { nombre: string; valor: string }[];
  puedeGestionar: boolean;
}

export interface MiembroEquipo {
  id: number;
  nombreCompleto: string;
  /** "Pedro" para los avisos */
  nombre: string;
  iniciales: string;
  tono: Tono;
  roles: Etiqueta[];
  /** "Activo hace 5 min", "Sin accesos" */
  actividad: string;
  esLider: boolean;
  modulos: InfoModulo[];
  ticketsAbiertos: number;
  /** "9 tickets abiertos" */
  cargaTexto: string;
  /** Ancho de la barra, de 0 a 100 */
  cargaPorcentaje: number;
  cargaTono: Tono;
  leToca: number;
}

export interface Candidato {
  id: number;
  nombreCompleto: string;
  roles: string;
}
