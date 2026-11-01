// JSON de /api/equipo/proyectos/{id}/tickets (ver NuevoTicketDtos.java).

export interface PreparacionTicketDto {
  proyectoId: number;
  proyectoNombre: string;
  liderId: number;
  fases: {
    id: number;
    numero: number;
    nombre: string;
    modulos: { id: number; tipo: string; nombre: string; tecnologia: string | null; repositorio: string | null; ramaBase: string }[];
    historias: {
      id: number;
      numero: number;
      titulo: string;
      estado: string;
      como: string | null;
      quiero: string | null;
      para: string | null;
      analistaNombre: string;
      moduloIds: number[];
      tickets: number;
      criterios: { id: number; descripcion: string; cubiertoPor: string | null }[];
    }[];
  }[];
  equipo: { id: number; nombreCompleto: string; roles: string[] }[];
  siguientes: Record<string, number>;
  abiertos: { id: number; codigo: string; titulo: string; estado: string }[];
  capas: string[];
}

export interface NuevoTicketDto {
  faseId: number;
  historiaId: number;
  tipo: string;
  titulo: string;
  descripcion: string | null;
  moduloId: number;
  capas: string[];
  criterioIds: number[];
  subtareas: string[];
  dependeDe: number[];
  responsableId: number;
  testerId: number | null;
  prioridad: string;
  estimacionHoras: number | null;
  fechaLimite: string | null;
}

/** Editar ticket: lo mismo que uno nuevo, más lo que ya tiene (sin subtareas). */
export interface EdicionTicketDto {
  preparacion: PreparacionTicketDto;
  ticket: { id: number; codigo: string; datos: NuevoTicketDto };
}
