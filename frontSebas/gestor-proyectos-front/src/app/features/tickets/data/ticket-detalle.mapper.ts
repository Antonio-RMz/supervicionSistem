import { Tono } from '../../../shared/models/etiqueta.model';
import { aFecha, aFechaHora, diaYMes, fechaCorta, hora, haceCuanto } from '../../../shared/utils/fechas';
import { FLUJOS, ROL_DE_ACTOR } from '../../../shared/utils/flujos';
import { infoModulo } from '../../../shared/utils/proyectos';
import { codigoHistoria, etiquetaEstadoTicket, etiquetaPrioridad, textoEstado } from '../../../shared/utils/tickets';
import { TipoMovimiento } from '../models/movimientos.models';
import { DetalleTicket, PasoDelFlujo, SEVERIDADES } from '../models/ticket-detalle.models';
import { DetalleTicketDto, InfoTicketDto } from './ticket-detalle.dto';

const TONOS_PERSONA: Tono[] = ['violeta', 'verde', 'morado', 'azul', 'turquesa', 'naranja'];

const ESTADOS_PR: Record<string, string> = {
  ABIERTO: 'Abierto',
  APROBADO: 'Aprobado',
  INTEGRADO: 'Integrado',
  CERRADO: 'Cerrado',
};

/** Estados fuera del flujo normal: en qué paso se ven. */
const PASO_DE: Record<string, string> = {
  BLOQUEADA: 'EN_PROGRESO',
};

export function aDetalle(dto: DetalleTicketDto, hoy = new Date()): DetalleTicket {
  const i = dto.info;
  const codigo = `${i.tipo}-${String(i.numero).padStart(2, '0')}`;
  const ultimo = i.historial[0];
  return {
    id: i.id,
    proyectoId: i.proyectoId,
    proyectoNombre: i.proyectoNombre,
    faseId: i.faseId,
    // La fase 0 es Mantenimiento, la permanente: no se numera.
    faseTitulo: i.faseNumero === 0 ? i.faseNombre : `Fase ${i.faseNumero} · ${i.faseNombre}`,
    historia: i.historia && {
      id: i.historia.id,
      codigo: codigoHistoria(i.historia.numero),
      titulo: i.historia.titulo,
      estado: i.historia.estado,
      estadoTexto: textoEstado(i.historia.estado),
      como: i.historia.como,
      quiero: i.historia.quiero,
      para: i.historia.para,
    },
    tipo: i.tipo,
    codigo,
    numero: i.numero,
    titulo: i.titulo,
    descripcion: i.descripcion,
    severidad: SEVERIDADES.find((s) => s.codigo === dto.severidad) ?? null,
    hotfix: dto.hotfix,
    parche: dto.parche,
    estado: i.estado,
    etiqueta: etiquetaEstadoTicket(i.estado),
    prioridad: etiquetaPrioridad(i.prioridad),
    capas: i.capas,
    modulo: i.modulo && {
      ...infoModulo(i.modulo.tipo),
      nombre: i.modulo.nombre,
      tecnologia: i.modulo.tecnologia,
      ramaBase: i.modulo.ramaBase,
    },
    responsable: i.responsable && { ...i.responsable, iniciales: iniciales(i.responsable.nombre) },
    tester: i.tester && { ...i.tester, iniciales: iniciales(i.tester.nombre) },
    estimacion: [
      i.estimacionHoras !== null ? `${Number(i.estimacionHoras)} h` : null,
      i.horasRegistradas ? `${Number(i.horasRegistradas)} h registradas` : null,
    ].filter(Boolean).join(' · ') || '—',
    fechaLimite: fechaCorta(aFecha(i.fechaLimite)) || '—',
    rama: i.rama,
    bloqueadoPor: i.bloqueadoPor,
    rechazosEnQa: i.rechazosEnQa,
    dependencias: i.dependencias.map((d) => ({ ...d, etiqueta: etiquetaEstadoTicket(d.estado) })),
    subtareas: i.subtareas,
    subtareasHechas: i.subtareas.filter((s) => s.hecha).length,
    pullRequests: i.pullRequests.map((p) => ({
      numero: p.numero,
      titulo: `${p.ramaOrigen} → ${p.ramaDestino}`,
      estado: { texto: ESTADOS_PR[p.estado] ?? p.estado, tono: p.estado === 'ABIERTO' ? 'naranja' : 'verde' },
      ci: p.ciEstado === 'PASO' ? { texto: 'CI pasó', tono: 'verde' }
        : p.ciEstado === 'FALLO' ? { texto: 'CI falló', tono: 'rojo' }
        : { texto: 'CI pendiente', tono: 'gris' },
      url: p.url,
    })),
    criterios: i.criterios,
    flujo: flujoDe(i, dto.hotfix),
    historial: i.historial.map((h) => {
      const fecha = aFechaHora(h.fecha);
      return {
        texto: queHizo(h),
        detalle: `${diaYMes(fecha)} · ${hora(fecha)}`,
        motivo: h.automatico ? null : h.motivo,
        tono: etiquetaEstadoTicket(h.estado).tono,
      };
    }),
    motivoActual: ultimo?.estado === i.estado && ultimo.motivo
      ? { motivo: ultimo.motivo, usuario: ultimo.usuario, fecha: aFechaHora(ultimo.fecha) }
      : null,
    comentarios: i.comentarios.map((c) => ({
      usuario: c.usuario,
      iniciales: iniciales(c.usuario),
      tono: tonoDe(c.usuario),
      texto: c.texto,
      cuando: haceCuanto(aFechaHora(c.fecha), hoy),
    })),
    actor: dto.actor,
    meToca: dto.meToca,
    opciones: dto.opciones.map((o) => ({ ...o, tipo: o.tipo as TipoMovimiento })),
    puedeMarcarSubtareas: dto.puedeMarcarSubtareas,
    puedeEditarSubtareas: dto.puedeEditarSubtareas,
    puedeEditar: dto.puedeEditar,
    bloqueadores: dto.bloqueadores,
    equipo: dto.equipo,
  };
}

/** Los pasos de su tipo con quién actúa en cada uno y en cuál va (PDF pág. 29, "Flujo"). */
function flujoDe(i: InfoTicketDto, hotfix: boolean): PasoDelFlujo[] {
  const pasos = FLUJOS[hotfix ? 'BUGS_HOTFIX' : i.tipo] ?? [];
  const actual = pasos.findIndex((p) => p.codigo === (PASO_DE[i.estado] ?? i.estado));
  const primerNombre = (nombre: string | undefined) => nombre?.split(' ')[0] ?? 'Sin asignar';
  return pasos.map((p, n) => ({
    estado: p.estado,
    roles: p.actor ? [ROL_DE_ACTOR[p.actor]] : [],
    detalle:
      p.actor === 'responsable' ? `${primerNombre(i.responsable?.nombre)} · ${p.accion}`
      : p.actor === 'tester' ? `${primerNombre(i.tester?.nombre)} · ${p.accion}`
      : p.accion,
    avance: actual < 0 ? 'pendiente' : n < actual || n === pasos.length - 1 && n === actual ? 'hecho'
      : n === actual ? 'actual' : 'pendiente',
  }));
}

/** "Pedro inició el ticket · se creó la rama", "Desbloqueado: ACTV-07 se terminó" */
function queHizo(h: InfoTicketDto['historial'][number]): string {
  if (h.estadoAnterior === null) return `${h.usuario ?? 'Alguien'} creó el ticket`;
  if (h.automatico) return h.motivo ?? `Pasó a ${textoEstado(h.estado)}`;
  const quien = h.usuario ?? 'Alguien';
  const desdeQa = h.estadoAnterior === 'EN_QA';
  const desdePr = h.estadoAnterior === 'EN_REVISION';
  switch (h.estado) {
    case 'EN_PROGRESO':
    case 'EN_CORRECCION':
      return desdeQa ? `${quien} lo regresó a trabajo`
        : desdePr ? `${quien} pidió cambios al PR`
        : h.estadoAnterior === 'BLOQUEADA' ? `${quien} lo desbloqueó`
        : `${quien} inició el ticket`;
    case 'EN_QA':
      return `${quien} lo envió a QA`;
    case 'EN_REVISION':
      return `${quien} envió su PR`;
    case 'BLOQUEADA':
      return `${quien} lo marcó bloqueado`;
    case 'CANCELADA':
      return `${quien} lo canceló`;
    case 'APROBADA':
    case 'APROBADO':
    case 'APROBADA_QA':
      return desdeQa ? `${quien} lo aprobó en QA` : desdePr ? `${quien} aprobó el PR` : `${quien} lo aprobó`;
    default:
      return `${quien} lo pasó a ${textoEstado(h.estado)}`;
  }
}

function iniciales(nombre: string): string {
  return nombre.split(/\s+/).slice(0, 2).map((p) => p.charAt(0).toUpperCase()).join('');
}

function tonoDe(nombre: string): Tono {
  const suma = [...nombre].reduce((total, letra) => total + letra.charCodeAt(0), 0);
  return TONOS_PERSONA[suma % TONOS_PERSONA.length];
}
