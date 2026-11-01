// JSON tal como lo manda el back en /equipo/proyectos/{id}/tickets/{ticketId} (DetalleTicketDtos).

export interface DetalleTicketDto {
  info: InfoTicketDto;
  severidad: string | null;
  hotfix: boolean;
  parche: { version: string; bugs: { id: number; codigo: string; titulo: string }[] } | null;
  actor: string;
  meToca: boolean;
  opciones: { tipo: string; texto: string; destino: string; impedimentos: string[] }[];
  puedeMarcarSubtareas: boolean;
  puedeEditarSubtareas: boolean;
  puedeEditar: boolean;
  bloqueadores: { id: number; codigo: string; titulo: string; estado: string }[];
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
}

export interface InfoTicketDto {
  id: number;
  proyectoId: number;
  proyectoNombre: string;
  faseId: number;
  faseNumero: number;
  faseNombre: string;
  historia: {
    id: number;
    numero: number;
    titulo: string;
    estado: string;
    como: string | null;
    quiero: string | null;
    para: string | null;
  } | null;
  tipo: string;
  numero: number;
  titulo: string;
  descripcion: string | null;
  estado: string;
  prioridad: string;
  capas: string[];
  modulo: { tipo: string; nombre: string; tecnologia: string | null; repositorio: string | null; ramaBase: string } | null;
  responsable: { id: number; nombre: string } | null;
  tester: { id: number; nombre: string } | null;
  creadoPor: { id: number; nombre: string };
  estimacionHoras: number | null;
  horasRegistradas: number | null;
  fechaLimite: string | null;
  rama: string | null;
  bloqueadoPor: string | null;
  rechazosEnQa: number;
  dependencias: { codigo: string; titulo: string; estado: string }[];
  subtareas: { id: number; descripcion: string; hecha: boolean }[];
  pullRequests: {
    numero: number;
    ramaOrigen: string;
    ramaDestino: string;
    estado: string;
    ciEstado: string;
    url: string | null;
  }[];
  criterios: { descripcion: string; verificado: boolean; soloEste: boolean }[];
  historial: {
    estadoAnterior: string | null;
    estado: string;
    motivo: string | null;
    automatico: boolean;
    usuario: string | null;
    fecha: string;
  }[];
  comentarios: { usuario: string; texto: string; fecha: string }[];
}
