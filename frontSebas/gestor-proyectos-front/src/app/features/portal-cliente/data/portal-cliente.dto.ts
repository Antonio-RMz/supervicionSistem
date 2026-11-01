// DTOs: la forma exacta del JSON de /api/portal (ver PortalClienteDtos.java en el back).

export interface ModuloDto {
  nombre: string;
  tipo: string;
}

export interface FaseDto {
  id: number;
  numero: number;
  nombre: string;
  objetivo: string | null;
  estado: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  version: string | null;
  avance: number;
}

export interface MiProyectoDto {
  id: number;
  codigo: string;
  nombre: string;
  estado: string;
  modulos: ModuloDto[];
  avance: number;
  faseActual: FaseDto | null;
  demosPendientes: number;
}

export interface ModuloAvanceDto {
  nombre: string;
  tipo: string;
  avance: number;
}

export interface HistoriaDto {
  titulo: string;
  descripcion: string | null;
  estado: string;
}

export interface VersionDto {
  version: string;
  fecha: string | null;
  resumen: string | null;
}

export interface ContactoDto {
  nombre: string;
  rol: string;
  correo: string;
}

export interface DemoDto {
  id: number;
  titulo: string;
  url: string | null;
}

export interface AvanceDto {
  id: number;
  codigo: string;
  nombre: string;
  estado: string;
  avance: number;
  modulos: ModuloDto[];
  fases: FaseDto[];
  faseActual: FaseDto | null;
  modulosFaseActual: ModuloAvanceDto[];
  historias: HistoriaDto[];
  versiones: VersionDto[];
  contacto: ContactoDto | null;
  demoPendiente: DemoDto | null;
}
