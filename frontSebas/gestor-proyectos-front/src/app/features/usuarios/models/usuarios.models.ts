import { Etiqueta, Tono } from '../../../shared/models/etiqueta.model';

/** Una fila de la tabla de Usuarios. */
export interface UsuarioFila {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  /** Códigos ya ordenados: ADMIN, LIDER, ANALISTA, DESARROLLADOR */
  roles: string[];
  etiquetasRol: Etiqueta[];
  proyectos: number;
  activo: boolean;
  tonoAvatar: Tono;
}

export interface Rol {
  codigo: string;
  nombre: string;
  descripcion: string;
  permisos: string[];
}

export interface Permiso {
  codigo: string;
  nombre: string;
  grupo: string;
}

/** Ajuste puntual sobre lo que dan los roles. proyectoId null = todos los proyectos. */
export interface PermisoExtra {
  permisoCodigo: string;
  permisoNombre: string;
  proyectoId: number | null;
  proyectoNombre: string;
  otorgado: boolean;
}

export interface Accesos {
  roles: string[];
  permisosExtra: PermisoExtra[];
}

export interface NuevoUsuario {
  nombre: string;
  apellidos: string;
  correo: string;
  password: string;
  cambiarPassword: boolean;
  activo: boolean;
  roles: string[];
  proyectoIds: number[];
  /** Su usuario de GitHub, GitLab…; vacío = sin capturar */
  usuarioGit: string;
}

// ─── Detalle de una persona ─────────────────────────────────────────────────

export interface Tarea {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estadoTexto: string;
  accion: string;
  /** "Ana · Analista" (solo en "Esperando a otros") */
  quien: string | null;
  rechazadaEnQa: boolean;
  actualizadoEn: Date | null;
}

export interface Hecho {
  descripcion: string;
  tono: Tono;
  fecha: Date | null;
}

/** Hechos del mismo día bajo un título: "Hoy", "Ayer", "15 oct". */
export interface DiaDeActividad {
  dia: string;
  hechos: Hecho[];
}

export interface PerfilUsuario {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  etiquetasRol: Etiqueta[];
  activo: boolean;
  tonoAvatar: Tono;
  ultimoAcceso: Date | null;
  /** null = sin capturar */
  usuarioGit: string | null;
  metricas: {
    trabajandoAhora: number;
    leToca: number;
    esperandoAOtros: number;
    cerradosEsteMes: number;
    rechazadosEnQaEsteMes: number;
  };
  trabajandoAhora: Tarea[];
  leToca: Tarea[];
  esperandoAOtros: Tarea[];
  actividad: DiaDeActividad[];
  proyectos: { id: number; nombre: string; ticketsAbiertos: number }[];
}
