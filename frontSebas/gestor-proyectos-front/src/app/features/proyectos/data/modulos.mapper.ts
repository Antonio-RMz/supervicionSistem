import { aFechaHora, diaYMes, haceCuanto } from '../../../shared/utils/fechas';
import { infoModulo } from '../../../shared/utils/proyectos';
import { DatosModulo, ModulosProyecto, TarjetaModulo } from '../models/modulos.models';
import { ModuloDto, ModuloRequestDto, ModulosDto } from './modulos.dto';

const TONO_AMBIENTE = ['morado', 'naranja', 'verde'] as const;

const BUILD: Record<string, { palabra: string; tono: 'verde' | 'rojo' | 'ambar' }> = {
  PASO: { palabra: 'pasó', tono: 'verde' },
  FALLO: { palabra: 'falló', tono: 'rojo' },
  PENDIENTE: { palabra: 'corriendo', tono: 'ambar' },
};

export function aModulos(dto: ModulosDto, ahora = new Date()): ModulosProyecto {
  return {
    modulos: dto.modulos.map((m) => aTarjeta(m, ahora)),
    faltan: dto.faltan.map((tipo) => ({ tipo, info: infoModulo(tipo) })),
    puedeGestionar: dto.puedeGestionar,
  };
}

function aTarjeta(m: ModuloDto, ahora: Date): TarjetaModulo {
  const build = m.ultimoBuild ? BUILD[m.ultimoBuild.estado] : null;
  const staging = m.ambientes.find((a) => a.nombre === 'staging');
  const produccion = m.ambientes.find((a) => a.nombre === 'producción');
  return {
    id: m.id,
    tipo: m.tipo,
    info: infoModulo(m.tipo),
    nombre: m.nombre,
    tecnologia: m.tecnologia,
    tecnologias: m.tecnologias,
    repositorio: m.repositorio,
    repositorioUrl: m.repositorio ? (m.repositorio.startsWith('http') ? m.repositorio : `https://${m.repositorio}`) : null,
    ramaBase: m.ramaBase,
    responsable: m.responsable,
    ambientes: m.ambientes.map((a, i) => {
      const fecha = aFechaHora(a.fecha);
      // develop y staging dicen hace cuánto; producción, el día que se publicó
      const cuando = i < 2 ? haceCuanto(fecha, ahora) : diaYMes(fecha);
      return {
        nombre: a.nombre,
        tono: TONO_AMBIENTE[i] ?? 'gris',
        version: a.version,
        texto: a.version ? [a.version, cuando].filter(Boolean).join(' · ')
          : i < 2 ? 'Sin integraciones de la fase' : 'Sin publicar',
      };
    }),
    build: build && m.ultimoBuild
      ? { texto: `Último build · ${build.palabra} · ${haceCuanto(aFechaHora(m.ultimoBuild.fecha), ahora)}`, tono: build.tono }
      : null,
    porHacer: m.porHacer,
    enProgreso: m.enProgreso,
    enQa: m.enQa,
    bugsProduccion: m.bugsProduccion,
    conTrabajo: m.conTrabajo,
    ultimoDespliegue: staging?.version
      ? `staging · ${haceCuanto(aFechaHora(staging.fecha), ahora)}`
      : produccion?.version ? `producción · ${diaYMes(aFechaHora(produccion.fecha))}` : '—',
  };
}

export function aRequestModulo(d: DatosModulo): ModuloRequestDto {
  const texto = (v: string) => v.trim() || null;
  return {
    tipo: d.tipo,
    nombre: d.nombre.trim(),
    tecnologia: texto(d.tecnologia),
    repositorio: texto(d.repositorio),
    ramaBase: texto(d.ramaBase),
    responsableId: d.responsableId,
  };
}
