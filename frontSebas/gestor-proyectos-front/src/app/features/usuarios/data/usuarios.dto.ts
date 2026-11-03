// JSON de /api/equipo/usuarios y /api/equipo/accesos (ver UsuariosDtos.java).

export interface UsuarioFilaDto {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  roles: string[];
  proyectos: number;
  activo: boolean;
}

export interface NuevoUsuarioDto {
  nombre: string;
  apellidos: string | null;
  correo: string;
  password: string;
  cambiarPassword: boolean;
  activo: boolean;
  roles: string[];
  proyectoIds: number[];
  /** opcional; null = sin capturar */
  usuarioGit: string | null;
}

export interface PermisoExtraDto {
  permisoCodigo: string;
  permisoNombre?: string | null;
  proyectoId: number | null;
  proyectoNombre?: string | null;
  otorgado: boolean;
}

export interface AccesosDto {
  roles: string[];
  permisosExtra: PermisoExtraDto[];
}

export interface RolDto {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  permisos: string[];
}

export interface PermisoDto {
  codigo: string;
  nombre: string;
  grupo: string;
}

export interface TareaDto {
  id: number;
  tipo: string;
  codigo: string;
  titulo: string;
  estado: string;
  accion: string;
  actorNombre: string | null;
  actorRol: string | null;
  rechazadaEnQa: boolean;
  actualizadoEn: string | null;
}

export interface PerfilDto {
  id: number;
  nombreCompleto: string;
  iniciales: string;
  correo: string;
  roles: string[];
  activo: boolean;
  ultimoAcceso: string | null;
  usuarioGit: string | null;
  metricas: {
    trabajandoAhora: number;
    leToca: number;
    esperandoAOtros: number;
    cerradosEsteMes: number;
    rechazadosEnQaEsteMes: number;
  };
  trabajandoAhora: TareaDto[];
  leToca: TareaDto[];
  esperandoAOtros: TareaDto[];
  actividad: { descripcion: string; clase: string; fecha: string }[];
  proyectos: { id: number; nombre: string; ticketsAbiertos: number }[];
}
