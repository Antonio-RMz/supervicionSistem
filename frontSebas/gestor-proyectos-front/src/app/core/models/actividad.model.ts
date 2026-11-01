export type EstadoActividad =
  | 'POR_HACER'
  | 'EN_DESARROLLO'
  | 'REVISION_QA'
  | 'LISTO_PARA_INTEGRAR'
  | 'PR_EN_REVISION'
  | 'INTEGRADO';

export interface Actividad {
  id: number;
  codigo: string;
  titulo: string;
  descripcion: string;
  estado: EstadoActividad;
  faseId: number;
  faseNombre: string;
  prUrl: string | null;
}

export interface UsuarioInvolucrado {
  id: number;
  nombre: string;
  apellido: string;
  iniciales: string;
  color: string;
  rolProyecto: string;
}

export interface BitacoraEntrada {
  id: number;
  usuarioNombre: string;
  usuarioIniciales: string;
  usuarioColor: string;
  estadoAnterior: EstadoActividad;
  estadoNuevo: EstadoActividad;
  fechaISO: string;
  comentario?: string;
}
