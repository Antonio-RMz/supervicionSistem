/** JSON de GET /api/equipo/proyectos/{id}/equipo (PDF pág. 34). */
export interface EquipoDto {
  proyectoId: number;
  miembros: MiembroDto[];
  cobertura: { rol: string; resultado: 'OK' | 'AVISO' | 'FALTA'; quien: string }[];
  alertas: string[];
  permisos: { permiso: string; usuarioId: number; usuario: string; otorgado: boolean }[];
  reglas: {
    permitirAutovalidacion: boolean;
    comentarioAlRechazar: boolean;
    ramaAlIniciar: boolean;
    notificarClienteFase: boolean;
  };
  puedeGestionar: boolean;
}

export interface MiembroDto {
  id: number;
  nombreCompleto: string;
  nombreCorto: string;
  iniciales: string;
  roles: string[];
  ultimoAcceso: string | null;
  esLider: boolean;
  /** API, WEB, MOVIL... */
  modulos: string[];
  ticketsAbiertos: number;
  carga: 'BAJA' | 'MEDIA' | 'ALTA';
  leToca: number;
}

export interface CandidatoDto {
  id: number;
  nombreCompleto: string;
  roles: string[];
}
