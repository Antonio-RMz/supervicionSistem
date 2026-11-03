// JSON de /api/equipo/proyectos/{id}/historias (ver HistoriasDtos.java).

export interface OpcionDto {
  id: number;
  tipo: string | null;
  nombre: string;
}

export interface PreparacionDto {
  proyectoId: number;
  proyectoNombre: string;
  numero: number;
  fases: {
    id: number;
    numero: number;
    nombre: string;
    estado: string;
    modulos: OpcionDto[];
  }[];
  analistas: { id: number; nombreCompleto: string; roles: string[] }[];
  usuariosDelSistema: { nombre: string; descripcion: string | null }[];
  desarrolladores: { id: number; nombreCompleto: string; roles: string[] }[];
}

export interface NuevaHistoriaDto {
  borrador: boolean;
  faseId: number;
  titulo: string;
  como: string | null;
  quiero: string | null;
  para: string | null;
  criterios: string[];
  figmaUrl: string | null;
  disenoAprobado: boolean;
  analistaId: number;
  prioridad: string;
  puntos: number | null;
  moduloIds: number[];
  desarrolladores: { usuarioId: number; moduloId: number | null }[];
}

export interface EdicionDto {
  preparacion: PreparacionDto;
  historia: {
    id: number;
    numero: number;
    estado: string;
    faseId: number;
    titulo: string;
    como: string | null;
    quiero: string | null;
    para: string | null;
    criterios: string[];
    figmaUrl: string | null;
    disenoAprobado: boolean;
    analistaId: number;
    prioridad: string;
    puntos: number | null;
    moduloIds: number[];
    desarrolladores: { usuarioId: number; moduloId: number | null }[];
  };
}

/** GET /historias?fase=ID: las historias de la fase completas (ver ListadoHistoriasDtos.java). */
export interface HistoriasDeFaseDto {
  proyectoNombre: string;
  faseNumero: number;
  faseNombre: string;
  historias: {
    codigo: string;
    estado: string;
    titulo: string;
    como: string | null;
    quiero: string | null;
    para: string | null;
    prioridad: string;
    puntos: number | null;
    figmaUrl: string | null;
    criterios: string[];
  }[];
}
