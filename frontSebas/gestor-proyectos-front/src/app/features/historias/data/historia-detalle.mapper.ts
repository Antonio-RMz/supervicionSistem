import { Tono } from '../../../shared/models/etiqueta.model';
import { aFecha, aFechaHora, diaYMes, haceCuanto } from '../../../shared/utils/fechas';
import { etiquetaFase, infoModulo } from '../../../shared/utils/proyectos';
import { codigoHistoria, etiquetaEstadoTicket, etiquetaHistoria, etiquetaPrioridad, textoEstado } from '../../../shared/utils/tickets';
import {
  AccionDeListaHistoria,
  AccionHistoria,
  DatosAccionHistoria,
  DesarrolloHistoria,
  DetalleHistoria,
} from '../models/historia-detalle.models';
import { AccionDeListaHistoriaDto, AccionHistoriaRequestDto, DesarrolloDto, DetalleHistoriaDto } from './historia-detalle.dto';

const TERMINADOS = ['APROBADA', 'APROBADO', 'APROBADA_QA', 'HECHA', 'INTEGRADA', 'INTEGRADO', 'PUBLICADA',
  'PUBLICADO', 'CERRADA', 'CERRADO', 'REVISADA'];
const FUERA = ['CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA'];

/** Tonos para los avatares de los comentarios: siempre el mismo para la misma persona. */
const TONOS_PERSONA: Tono[] = ['violeta', 'verde', 'morado', 'azul', 'turquesa', 'naranja'];

export function aDetalle(dto: DetalleHistoriaDto, hoy = new Date()): DetalleHistoria {
  const i = dto.info;
  const cuentan = i.tickets.filter((t) => !FUERA.includes(t.estado));
  const aprobadoEn = aFechaHora(i.disenoAprobadoEn);
  return {
    id: i.id,
    proyectoId: i.proyectoId,
    proyectoNombre: i.proyectoNombre,
    formaTrabajo: i.formaTrabajo,
    faseId: i.faseId,
    faseNumero: i.faseNumero,
    faseNombre: i.faseNombre,
    faseTitulo: `Fase ${i.faseNumero} · ${i.faseNombre}`,
    faseEtiqueta: etiquetaFase(i.faseEstado),
    faseFechas: i.faseInicio || i.faseFin
      ? `${diaYMes(aFecha(i.faseInicio)) || '…'} – ${diaYMes(aFecha(i.faseFin)) || '…'}`
      : '',
    codigo: codigoHistoria(i.numero),
    titulo: i.titulo,
    como: i.como,
    quiero: i.quiero,
    para: i.para,
    estado: i.estado,
    etiqueta: etiquetaHistoria(i.estado),
    prioridad: etiquetaPrioridad(i.prioridad),
    puntos: i.puntos,
    analistaNombre: i.analistaNombre,
    figmaUrl: i.figmaUrl,
    disenoDeFase: i.disenoDeFase,
    disenoAprobado: i.disenoAprobado
      ? ['Diseño aprobado por el cliente', i.disenoAprobadoPor, diaYMes(aprobadoEn)].filter(Boolean).join(' · ')
      : null,
    modulos: i.modulos.map(infoModulo),
    criterios: i.criterios,
    criteriosVerificados: i.criterios.filter((c) => c.verificado).length,
    tickets: i.tickets.map((t) => ({
      id: t.id,
      tipo: t.tipo,
      codigo: `${t.tipo}-${String(t.numero).padStart(2, '0')}`,
      titulo: t.titulo,
      estadoCodigo: t.estado,
      estado: etiquetaEstadoTicket(t.estado),
      modulo: t.moduloTipo ? infoModulo(t.moduloTipo) : null,
      responsable: t.responsableNombre ? { nombre: t.responsableNombre, iniciales: iniciales(t.responsableNombre) } : null,
      dependeDe: t.dependeDe,
    })),
    ticketsAprobados: cuentan.filter((t) => TERMINADOS.includes(t.estado)).length,
    ticketsQueCuentan: cuentan.length,
    historial: i.historial.map((h) => ({
      texto: textoEstado(h.estado) + (h.automatico ? ' · automático' : ''),
      detalle: [diaYMes(aFechaHora(h.fecha)), h.motivo ?? h.usuario].filter(Boolean).join(' · '),
      tono: etiquetaHistoria(h.estado).tono,
    })),
    motivoActual: i.historial[0]?.estado === i.estado && i.historial[0].motivo
      ? { motivo: i.historial[0].motivo, usuario: i.historial[0].usuario }
      : null,
    imagenes: i.adjuntos.map((a) => ({ id: a.id, nombre: a.nombre, tamano: tamano(a.tamanoBytes) })),
    comentarios: i.comentarios.map((c) => ({
      usuario: c.usuario,
      iniciales: iniciales(c.usuario),
      tono: tonoDe(c.usuario),
      texto: c.texto,
      cuando: haceCuanto(aFechaHora(c.fecha), hoy),
    })),
    actor: dto.actor,
    meToca: dto.meToca,
    acciones: dto.acciones.map((a) => ({ ...a, accion: a.accion as AccionHistoria })),
    puedeVerificarCriterios: dto.puedeVerificarCriterios,
    puedeEditar: dto.puedeEditar,
    puedeAdjuntar: dto.puedeAdjuntar,
    puedeCrearTicket: dto.puedeCrearTicket,
    ticketsSinEmpezar: dto.ticketsSinEmpezar,
    ticketsReabribles: dto.ticketsReabribles,
    fasesDestino: dto.fasesDestino,
    puedeEliminar: dto.puedeEliminar,
    desarrollo: dto.desarrollo ? aDesarrollo(dto.desarrollo) : null,
  };
}

function aDesarrollo(d: DesarrolloDto): DesarrolloHistoria {
  return {
    desarrolladores: d.desarrolladores.map((x) => ({
      usuarioId: x.usuarioId,
      nombre: x.nombre,
      iniciales: iniciales(x.nombre),
      repositorio: x.moduloNombre,
      terminado: x.terminado,
    })),
    trabajo: d.trabajo.map((t) => ({
      id: t.id,
      descripcion: t.descripcion,
      repositorio: t.moduloNombre,
      hecho: t.hecho,
      detalle: `${t.creadoPor.split(' ')[0]} · ${diaYMes(aFecha(t.creadoEn.slice(0, 10)))}`,
    })),
    repositorios: d.repositorios.map((r) => ({ ...r, enlace: r.url ? r.url.replace(/\.git$/, '') : null })),
    miParteTerminada: d.miParteTerminada,
    puedeAnotar: d.puedeAnotar,
    puedeMarcar: d.puedeMarcar,
    requierePr: d.requierePr,
  };
}

export function aAccionesDeLista(dtos: AccionDeListaHistoriaDto[]): AccionDeListaHistoria[] {
  return dtos.map((d) => ({ ...d, accion: d.accion as AccionHistoria }));
}

export function aRequestAccion(
  accion: AccionHistoria,
  estadoEsperado: string,
  datos: DatosAccionHistoria,
): AccionHistoriaRequestDto {
  return {
    accion,
    estadoEsperado,
    motivo: datos.motivo?.trim() || null,
    ticketId: datos.ticketId ?? null,
    crearBug: datos.crearBug ?? false,
    faseDestinoId: datos.faseDestinoId ?? null,
  };
}

/** "Pedro Ramírez" -> "PR" */
function iniciales(nombre: string): string {
  return nombre.split(/\s+/).slice(0, 2).map((p) => p.charAt(0).toUpperCase()).join('');
}

function tonoDe(nombre: string): Tono {
  const suma = [...nombre].reduce((total, letra) => total + letra.charCodeAt(0), 0);
  return TONOS_PERSONA[suma % TONOS_PERSONA.length];
}

/** 245760 -> "240 KB", 1153434 -> "1.1 MB" */
function tamano(bytes: number | null): string {
  if (bytes === null) return '';
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
