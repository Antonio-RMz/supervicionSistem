// DTO del back -> modelo de la pantalla. Aquí viven los textos y colores de cada estado.

import { Etiqueta } from '../../../shared/models/etiqueta.model';
import { aFecha } from '../../../shared/utils/fechas';
import {
  AvanceProyecto,
  EstadoFase,
  Fase,
  Historia,
  MiProyecto,
  Modulo,
  VersionEntregada,
} from '../models/portal.models';
import { AvanceDto, FaseDto, HistoriaDto, MiProyectoDto, ModuloDto, VersionDto } from './portal-cliente.dto';

const ESTADOS_PROYECTO: Record<string, Etiqueta> = {
  PLANEACION: { texto: 'Próximo', tono: 'gris' },
  EN_CURSO: { texto: 'En curso', tono: 'morado' },
  EN_PAUSA: { texto: 'En pausa', tono: 'naranja' },
  TERMINADO: { texto: 'Terminado', tono: 'verde' },
  CANCELADO: { texto: 'Cancelado', tono: 'gris' },
};

const ESTADOS_FASE: Record<EstadoFase, Etiqueta> = {
  PROXIMA: { texto: 'Próxima', tono: 'gris' },
  EN_CURSO: { texto: 'En curso', tono: 'morado' },
  EN_PAUSA: { texto: 'En pausa', tono: 'naranja' },
  ENTREGADA: { texto: 'Entregada', tono: 'verde' },
  CANCELADA: { texto: 'Cancelada', tono: 'gris' },
};

const ESTADOS_HISTORIA: Record<string, Etiqueta> = {
  PLANEADO: { texto: 'Planeado', tono: 'gris' },
  EN_DESARROLLO: { texto: 'En desarrollo', tono: 'morado' },
  EN_PRUEBAS: { texto: 'En pruebas', tono: 'naranja' },
  LISTO: { texto: 'Lista para entrega', tono: 'verde' },
  ENTREGADO: { texto: 'Entregado', tono: 'verde' },
};

const MODULOS: Record<string, Omit<Modulo, 'tipo'>> = {
  API: { nombreCorto: 'API', nombreLargo: 'Backend · API', icono: 'bi-hdd-stack' },
  WEB: { nombreCorto: 'Web', nombreLargo: 'Web', icono: 'bi-display' },
  MOVIL: { nombreCorto: 'Móvil', nombreLargo: 'App móvil', icono: 'bi-phone' },
  ESCRITORIO: { nombreCorto: 'Escritorio', nombreLargo: 'Escritorio', icono: 'bi-pc-display' },
  BASE_DATOS: { nombreCorto: 'Base de datos', nombreLargo: 'Base de datos', icono: 'bi-database' },
  INFRAESTRUCTURA: { nombreCorto: 'Infraestructura', nombreLargo: 'Infraestructura', icono: 'bi-cloud' },
};

const SIN_ETIQUETA: Etiqueta = { texto: '—', tono: 'gris' };

export function aMiProyecto(dto: MiProyectoDto): MiProyecto {
  return {
    id: dto.id,
    codigo: dto.codigo,
    nombre: dto.nombre,
    etiqueta: ESTADOS_PROYECTO[dto.estado] ?? SIN_ETIQUETA,
    modulos: dto.modulos.map(aModulo),
    avance: dto.avance,
    faseActual: dto.faseActual ? aFase(dto.faseActual) : null,
    demosPendientes: dto.demosPendientes,
  };
}

export function aAvance(dto: AvanceDto): AvanceProyecto {
  return {
    id: dto.id,
    codigo: dto.codigo,
    nombre: dto.nombre,
    etiqueta: ESTADOS_PROYECTO[dto.estado] ?? SIN_ETIQUETA,
    avance: dto.avance,
    modulos: dto.modulos.map(aModulo),
    fases: dto.fases.map(aFase),
    faseActual: dto.faseActual ? aFase(dto.faseActual) : null,
    modulosFaseActual: dto.modulosFaseActual.map((m) => ({ modulo: aModulo(m), avance: m.avance })),
    historias: dto.historias.map(aHistoria),
    versiones: dto.versiones.map(aVersion),
    contacto: dto.contacto,
    demoPendiente: dto.demoPendiente,
  };
}

function aModulo(dto: ModuloDto): Modulo {
  const conocido = MODULOS[dto.tipo] ?? { nombreCorto: dto.nombre, nombreLargo: dto.nombre, icono: 'bi-box' };
  return { tipo: dto.tipo, ...conocido };
}

function aFase(dto: FaseDto): Fase {
  const estado = dto.estado as EstadoFase;
  return {
    id: dto.id,
    numero: dto.numero,
    nombre: dto.nombre,
    objetivo: dto.objetivo,
    estado,
    etiqueta: ESTADOS_FASE[estado] ?? SIN_ETIQUETA,
    fechaInicio: aFecha(dto.fechaInicio),
    fechaFin: aFecha(dto.fechaFin),
    version: dto.version,
    avance: dto.avance,
  };
}

function aHistoria(dto: HistoriaDto): Historia {
  return {
    titulo: dto.titulo,
    descripcion: dto.descripcion,
    etiqueta: ESTADOS_HISTORIA[dto.estado] ?? SIN_ETIQUETA,
  };
}

function aVersion(dto: VersionDto): VersionEntregada {
  return { version: dto.version, fecha: aFecha(dto.fecha), resumen: dto.resumen };
}
