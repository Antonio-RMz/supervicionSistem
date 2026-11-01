import { Tono } from '../../../shared/models/etiqueta.model';

export interface Cliente {
  id: number;
  razonSocial: string;
  nombreCorto: string;
  iniciales: string;
  rfc: string;
  /** Código (COMERCIO...) o '' si no se capturó */
  giro: string;
  ciudad: string;
  contactoNombre: string;
  contactoCorreo: string;
  contactoTelefono: string;
  notas: string;
  activo: boolean;
  proyectos: { id: number; nombre: string }[];
  tono: Tono;
}

export interface ListadoClientes {
  clientes: Cliente[];
  /** Si no, la pantalla es de solo lectura. */
  puedeGestionar: boolean;
}

/** Lo que se captura en el modal. Los textos vacíos viajan como null. */
export type DatosCliente = Omit<Cliente, 'id' | 'iniciales' | 'proyectos' | 'tono'>;

export type FiltroEstado = 'todos' | 'activos' | 'inactivos';

export const GIROS: { codigo: string; nombre: string }[] = [
  { codigo: 'COMERCIO', nombre: 'Comercio' },
  { codigo: 'MANUFACTURA', nombre: 'Manufactura' },
  { codigo: 'SERVICIOS', nombre: 'Servicios' },
  { codigo: 'TECNOLOGIA', nombre: 'Tecnología' },
  { codigo: 'SALUD', nombre: 'Salud' },
  { codigo: 'EDUCACION', nombre: 'Educación' },
  { codigo: 'GOBIERNO', nombre: 'Gobierno' },
  { codigo: 'OTRO', nombre: 'Otro' },
];
