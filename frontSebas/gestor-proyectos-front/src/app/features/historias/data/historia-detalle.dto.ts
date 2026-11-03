import { FormaTrabajo } from '../../../shared/utils/flujos';

// JSON tal como lo manda el back en /equipo/proyectos/{id}/historias/{historiaId} (GestionHistoriaDtos).

export interface DetalleHistoriaDto {
  info: InfoHistoriaDto;
  actor: string;
  meToca: boolean;
  acciones: { accion: string; texto: string; impedimentos: string[] }[];
  puedeVerificarCriterios: boolean;
  puedeEditar: boolean;
  puedeAdjuntar: boolean;
  puedeCrearTicket: boolean;
  ticketsSinEmpezar: number;
  ticketsReabribles: { id: number; codigo: string; titulo: string }[];
  fasesDestino: { id: number; titulo: string }[];
  puedeEliminar: boolean;
  desarrollo: DesarrolloDto | null;
}

export interface InfoHistoriaDto {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  formaTrabajo: FormaTrabajo;
  faseId: number;
  faseNumero: number;
  faseNombre: string;
  faseEstado: string;
  faseInicio: string | null;
  faseFin: string | null;
  numero: number;
  titulo: string;
  como: string | null;
  quiero: string | null;
  para: string | null;
  estado: string;
  prioridad: string;
  puntos: number | null;
  analistaNombre: string;
  figmaUrl: string | null;
  disenoDeFase: boolean;
  disenoAprobado: boolean;
  disenoAprobadoPor: string | null;
  disenoAprobadoEn: string | null;
  modulos: string[];
  criterios: { id: number; descripcion: string; verificado: boolean; tickets: string[] }[];
  tickets: {
    id: number;
    tipo: string;
    numero: number;
    titulo: string;
    estado: string;
    moduloTipo: string | null;
    responsableNombre: string | null;
    dependeDe: string[];
  }[];
  historial: { estado: string; motivo: string | null; automatico: boolean; usuario: string | null; fecha: string }[];
  adjuntos: { id: number; nombre: string; tipoMime: string | null; tamanoBytes: number | null }[];
  comentarios: { usuario: string; texto: string; fecha: string }[];
}

export interface AccionHistoriaRequestDto {
  accion: string;
  estadoEsperado: string;
  motivo: string | null;
  ticketId: number | null;
  crearBug: boolean;
  faseDestinoId: number | null;
}

export interface ResultadoAccionHistoriaDto {
  accion: string;
  estadoNuevo: string;
  bugCreado: string | null;
}

export interface AccionDeListaHistoriaDto {
  historiaId: number;
  estado: string;
  accion: string;
  texto: string;
  impedimento: string | null;
}

export interface DesarrolloDto {
  desarrolladores: { usuarioId: number; nombre: string; moduloId: number | null; moduloNombre: string | null; terminado: boolean }[];
  trabajo: {
    id: number;
    descripcion: string;
    moduloId: number | null;
    moduloNombre: string | null;
    hecho: boolean;
    creadoPor: string;
    creadoEn: string;
  }[];
  repositorios: { id: number; tipo: string; nombre: string; tecnologia: string | null; ramaBase: string; url: string | null }[];
  miParteTerminada: boolean | null;
  puedeAnotar: boolean;
  puedeMarcar: boolean;
  requierePr: boolean;
}
