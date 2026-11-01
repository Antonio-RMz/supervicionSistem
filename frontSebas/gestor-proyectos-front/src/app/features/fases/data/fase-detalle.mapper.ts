import { Tono } from '../../../shared/models/etiqueta.model';
import { aFecha, aFechaHora, diaYMes, fechaCorta, haceCuanto } from '../../../shared/utils/fechas';
import { etiquetaFase, infoModulo } from '../../../shared/utils/proyectos';
import { nombresDeRoles } from '../../../shared/utils/roles';
import { codigoHistoria, etiquetaPrioridad, textoEstado } from '../../../shared/utils/tickets';
import { AccionDeLista, AccionFase, DatosAccion, DetalleFase, ReleaseFase } from '../models/fase-detalle.models';
import { AccionDeListaDto, AccionFaseRequestDto, DetalleFaseDto, ReleaseFaseDto } from './fase-detalle.dto';

/** Colores de "Tickets por estado", en el orden en que los manda el back. */
const TONOS_GRUPO: Record<string, Tono> = {
  'Por hacer': 'gris',
  'En progreso': 'morado',
  'En QA': 'naranja',
  Aprobados: 'violeta',
  Cerrados: 'verde',
};

/** Color del punto en "Actividad reciente" según el tipo de paso (Movimiento.Clase del back). */
const TONOS_ACTIVIDAD: Record<string, Tono> = {
  INICIO: 'morado',
  ENVIO: 'ambar',
  RECHAZO: 'rojo',
  CIERRE: 'verde',
  OTRO: 'violeta',
};

const HISTORIAS_INTEGRADAS = ['INTEGRADA', 'CERRADA'];

export function aDetalle(dto: DetalleFaseDto, hoy = new Date()): DetalleFase {
  const i = dto.info;
  const fechaFin = aFecha(i.fechaFin);
  const historiasQueCuentan = i.historias.filter((h) => h.estado !== 'DESCARTADA');
  return {
    id: i.id,
    proyectoId: i.proyectoId,
    proyectoNombre: i.proyectoNombre,
    proyectoCodigo: i.proyectoCodigo,
    clienteNombre: i.clienteNombre,
    numero: i.numero,
    titulo: i.mantenimiento ? i.nombre : `Fase ${i.numero} · ${i.nombre}`,
    objetivo: i.objetivo,
    estado: i.estado,
    etiqueta: etiquetaFase(i.estado),
    prioridad: etiquetaPrioridad(i.prioridad),
    fechaInicio: aFecha(i.fechaInicio),
    fechaFin,
    diasRestantes: diasHasta(fechaFin, hoy),
    responsableNombre: i.responsableNombre,
    versionRelease: i.versionRelease,
    versionAnterior: dto.versionAnterior,
    mantenimiento: i.mantenimiento,
    motivoEstado: i.motivoEstado,
    reanudarEl: aFecha(i.reanudarEl),
    enEstadoDesde: aFechaHora(i.enEstadoDesde),
    cambioEstadoPor: i.cambioEstadoPor,
    modulos: i.modulos.map((m) => ({ ...infoModulo(m.tipo), nombre: m.nombre })),
    repositorios: enLista(i.modulos.map((m) => m.nombre)),
    diseno: {
      figmaUrl: i.figmaUrl,
      paraTodas: i.figmaParaTodas,
      loUsan: i.figmaUrl && i.figmaParaTodas ? i.historias.filter((h) => !h.disenoPropio).length : 0,
      total: i.historias.length,
    },
    avance: dto.avance,
    ticketsTerminados: dto.ticketsTerminados,
    ticketsTotal: dto.ticketsTotal,
    historias: i.historias.map((h) => ({
      id: h.id,
      codigo: codigoHistoria(h.numero),
      titulo: h.titulo,
      estado: h.estado,
      estadoTexto: textoEstado(h.estado),
      tickets: h.total ? `${h.terminados}/${h.total} tickets` : 'Sin tickets',
      analistaIniciales: h.analistaIniciales,
      incompleta: h.incompleta,
      eliminable: dto.historiasEliminables.includes(h.id),
    })),
    historiasIntegradas: historiasQueCuentan.filter((h) => HISTORIAS_INTEGRADAS.includes(h.estado)).length,
    historiasQueCuentan: historiasQueCuentan.length,
    criterios: dto.criterios.map((c) => ({
      id: c.id,
      descripcion: c.descripcion,
      manual: c.manual,
      cumplido: c.cumplido,
      detalle: c.manual && c.cumplido && c.cumplidoPor
        ? `Manual · ${c.cumplidoPor}${c.cumplidoEn ? ', ' + diaYMes(aFechaHora(c.cumplidoEn)) : ''}`
        : c.detalle,
    })),
    criteriosCumplidos: dto.criterios.filter((c) => c.cumplido).length,
    ticketsPorEstado: dto.ticketsPorEstado.map((g) => ({ ...g, tono: TONOS_GRUPO[g.nombre] ?? 'gris' })),
    actividad: i.actividad.map((a) => ({
      descripcion: a.descripcion,
      cuando: haceCuanto(aFechaHora(a.fecha), hoy),
      tono: TONOS_ACTIVIDAD[a.clase] ?? 'gris',
    })),
    aCancelar: i.aCancelar,
    equipo: [...i.equipo]
      .sort((a, b) => Number(!a.roles.includes('DESARROLLADOR')) - Number(!b.roles.includes('DESARROLLADOR')))
      .map((p) => ({ id: p.id, nombreCompleto: p.nombreCompleto, roles: nombresDeRoles(p.roles) })),
    bugReversion: dto.bugReversion && { codigo: dto.bugReversion.codigo, abierto: dto.bugReversion.abierto },
    rol: dto.rol,
    esResponsable: dto.esResponsable,
    acciones: dto.acciones.map((a) => ({ ...a, accion: a.accion as AccionFase })),
    puedeMarcarCriterios: dto.puedeMarcarCriterios,
    puedeCrearHistoria: dto.puedeCrearHistoria,
    puedeEditar: dto.puedeEditar,
    puedeEliminar: dto.puedeEliminar,
  };
}

const SECCIONES_NOTAS: { tipo: string; titulo: string }[] = [
  { tipo: 'ACTV', titulo: 'Nuevo' },
  { tipo: 'MEJR', titulo: 'Mejorado' },
  { tipo: 'BUGS', titulo: 'Corregido' },
];

export function aRelease(dto: ReleaseFaseDto): ReleaseFase {
  const i = dto.info;
  const publicadoEn = aFechaHora(i.publicadoEn);
  const demoAprobada = i.demo?.estado === 'APROBADA';
  return {
    version: i.version,
    estado: i.estado,
    publicado: i.estado === 'PUBLICADA' && publicadoEn
      ? [fechaCorta(publicadoEn), i.publicadoPor].filter(Boolean).join(' · ')
      : null,
    enProduccion: i.enProduccion
      ? `${i.enProduccion.version}${i.enProduccion.fecha ? ` (${diaYMes(aFechaHora(i.enProduccion.fecha))})` : ''}`
      : null,
    liderNombre: i.liderNombre,
    modulos: i.modulos.map((m) => ({ ...infoModulo(m.tipo), nombre: m.nombre, tecnologia: m.tecnologia ?? '' })),
    enviarNotas: i.enviarNotas,
    secciones: SECCIONES_NOTAS
      .map((s) => ({ titulo: s.titulo, cambios: i.cambios.filter((c) => c.tipo === s.tipo) }))
      .filter((s) => s.cambios.length),
    vistoBuenoCliente: demoAprobada
      ? [i.demo!.revisadaPor ?? 'Cliente', 'visto bueno en demo', diaYMes(aFechaHora(i.demo!.revisadaEn))]
          .filter(Boolean).join(' · ')
      : null,
    notas: dto.notas,
    notasEditadas: dto.notas !== sugeridas(i.cambios),
    puedeEditarNotas: dto.puedeEditarNotas,
  };
}

/** Igual que NotasRelease.sugeridas del back, para saber si el Líder ya las editó. */
function sugeridas(cambios: { tipo: string; titulo: string }[]): string {
  return SECCIONES_NOTAS
    .filter((s) => cambios.some((c) => c.tipo === s.tipo))
    .map((s) => [s.titulo, ...cambios.filter((c) => c.tipo === s.tipo).map((c) => `- ${c.titulo}`)].join('\n'))
    .join('\n\n');
}

export function aAccionesDeLista(dtos: AccionDeListaDto[]): AccionDeLista[] {
  return dtos.map((d) => ({
    faseId: d.faseId,
    titulo: `Fase ${d.numero} · ${d.nombre}`,
    estado: d.estado,
    etiqueta: etiquetaFase(d.estado),
    accion: d.accion as AccionFase,
    texto: d.texto,
    impedimento: d.impedimento,
  }));
}

export function aRequestAccion(accion: AccionFase, estadoEsperado: string, datos: DatosAccion): AccionFaseRequestDto {
  return {
    accion,
    estadoEsperado,
    motivo: datos.motivo?.trim() || null,
    reanudarEl: datos.reanudarEl || null,
    versionConfirmada: datos.versionConfirmada?.trim() || null,
    asignarBugA: datos.asignarBugA ?? null,
  };
}

/** Días que faltan para la fecha fin; null si no hay o ya pasó. */
function diasHasta(fin: Date | null, hoy: Date): number | null {
  if (!fin) return null;
  const inicioHoy = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  const dias = Math.round((fin.getTime() - inicioHoy.getTime()) / 86_400_000);
  return dias >= 0 ? dias : null;
}

/** ["portal-api", "portal-app", "portal-web"] -> "portal-api, portal-app y portal-web" */
function enLista(textos: string[]): string {
  return textos.length <= 1 ? (textos[0] ?? '') : `${textos.slice(0, -1).join(', ')} y ${textos.at(-1)}`;
}
