/** JSON de GET /api/equipo/proyectos/{p}/historias/{h}/integracion (PDF pág. 28). */
export interface IntegracionDto {
  historiaId: number;
  proyectoId: number;
  proyectoNombre: string;
  faseId: number;
  faseNumero: number;
  version: string | null;
  codigo: string;
  titulo: string;
  estado: string;
  analistaNombre: string;
  /** "Líder" o "Admin" si puede integrarla; null si solo la ve */
  rol: string | null;
  impedimentos: string[];
  prefijosRama: string[];
  prsPorIntegrar: number;
  ticketsConCodigo: number;
  modulos: ModuloIntegrarDto[];
  revision: RevisionDto[];
  ambientes: AmbienteDto[];
  historiasDeFase: { id: number; numero: number; estado: string }[];
  pasos: string[];
}

export interface ModuloIntegrarDto {
  tipo: string;
  nombre: string;
  tecnologia: string | null;
  tickets: TicketIntegrarDto[];
}

export interface TicketIntegrarDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  rama: string | null;
  prNumero: number | null;
  /** ABIERTO, APROBADO, INTEGRADO, CERRADO */
  prEstado: string | null;
  /** PENDIENTE, PASO, FALLO */
  prCi: string | null;
  conConflictos: boolean;
  enDevelop: boolean;
}

export interface RevisionDto {
  texto: string;
  resultado: 'OK' | 'AVISO' | 'FALTA';
  detalle: string | null;
  fecha: string | null;
}

export interface AmbienteDto {
  nombre: string;
  version: string | null;
  detalle: string | null;
  fecha: string | null;
}
