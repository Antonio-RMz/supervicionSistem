import { Tono } from '../../../shared/models/etiqueta.model';
import { aFecha, aFechaHora, diaYMes, fechaCorta } from '../../../shared/utils/fechas';
import {
  etiquetaFase,
  etiquetaProyecto,
  infoModulo,
  tonoDeProyecto,
} from '../../../shared/utils/proyectos';
import { ordenarRoles, tonoDeAvatar } from '../../../shared/utils/roles';
import { textoEstado } from '../../../shared/utils/tickets';
import {
  DatosProyecto,
  FaseArbol,
  FaseResumen,
  HistoriaArbol,
  ListadoProyectos,
  Miembro,
  ModuloChip,
  Persona,
  ResumenProyecto,
  TicketArbol,
} from '../models/proyectos.models';
import {
  ConfiguracionDto,
  FaseArbolDto,
  FaseDto,
  ListadoDto,
  MiembroDto,
  PersonaDto,
  ProyectoRequestDto,
  ResumenDto,
  TicketArbolDto,
} from './proyectos.dto';

const PRIORIDADES: Record<string, string> = { ALTA: 'alta', MEDIA: 'media', BAJA: 'baja' };

const RITMOS: Record<ResumenDto['ritmo'], { texto: string; detalle: string; tono: Tono }> = {
  EN_TIEMPO: { texto: 'En tiempo', detalle: 'al día con las fechas', tono: 'verde' },
  EN_RIESGO: { texto: 'En riesgo', detalle: 'el avance va detrás del calendario', tono: 'naranja' },
  ATRASADO: { texto: 'Atrasado', detalle: 'la fase ya pasó su fecha de fin', tono: 'rojo' },
  SIN_FECHAS: { texto: 'Sin fechas', detalle: 'la fase actual no tiene fechas', tono: 'gris' },
};

export function aListado(dto: ListadoDto): ListadoProyectos {
  return {
    puedeCrear: dto.puedeCrear,
    proyectos: dto.proyectos.map((p) => ({
      id: p.id,
      codigo: p.codigo,
      nombre: p.nombre,
      estado: p.estado,
      etiqueta: etiquetaProyecto(p.estado),
      prioridad: p.prioridad,
      clienteId: p.clienteId,
      clienteNombre: p.clienteNombre,
      modulos: p.modulos.map(aChip),
      avance: p.avance,
      tono: tonoDeProyecto(p.id, p.estado),
      fases: p.fases,
      historias: p.historias,
      tickets: p.tickets,
      equipo: p.equipo.map(aMiembro),
      liderNombre: p.liderNombre,
    })),
  };
}

export function aResumen(dto: ResumenDto): ResumenProyecto {
  const info = dto.info;
  const inicio = aFecha(info.fechaInicio);
  const entrega = aFecha(info.fechaEntrega);
  const prioridadTexto = PRIORIDADES[info.prioridad] ?? info.prioridad.toLowerCase();
  const fases = dto.fases.map(aFase);
  return {
    id: info.id,
    codigo: info.codigo,
    nombre: info.nombre,
    descripcion: info.descripcion ?? '',
    estado: info.estado,
    etiqueta: etiquetaProyecto(info.estado),
    clienteNombre: info.clienteNombre,
    prioridadTexto: prioridadTexto.charAt(0).toUpperCase() + prioridadTexto.slice(1),
    fechaInicio: inicio,
    fechaEntrega: entrega,
    subtitulo: [info.clienteNombre, `Prioridad ${prioridadTexto}`, rango(inicio, entrega, true)]
      .filter(Boolean)
      .join(' · '),
    liderId: info.liderId,
    liderNombre: info.liderNombre,
    reglas: info.reglas,
    avance: dto.avance,
    tono: tonoDeProyecto(info.id, info.estado),
    modulos: dto.modulos.map((m) => ({
      ...aChip(m.tipo),
      id: m.id,
      nombre: m.nombre,
      tecnologia: m.tecnologia ?? '',
      repositorio: m.repositorio ?? '',
      ramaBase: m.ramaBase,
      ci:
        m.ciEstado === 'PASO'
          ? { texto: 'CI pasó', tono: 'verde', fecha: aFechaHora(m.ciFecha) }
          : m.ciEstado === 'FALLO'
            ? { texto: 'CI falló', tono: 'rojo', fecha: aFechaHora(m.ciFecha) }
            : m.ciEstado === 'PENDIENTE'
              ? { texto: 'CI corriendo', tono: 'ambar', fecha: aFechaHora(m.ciFecha) }
              : { texto: 'Sin despliegues', tono: 'gris', fecha: null },
    })),
    equipo: dto.equipo.map(aMiembro),
    fases,
    faseActual: dto.faseActual ? aFase(dto.faseActual) : null,
    ticketsAbiertos: dto.ticketsAbiertos,
    bugsAbiertos: dto.bugsAbiertos,
    bugsEnProduccion: dto.bugsEnProduccion,
    mantenimientoAbiertos: dto.mantenimientoAbiertos,
    ticketsListos: dto.ticketsListos,
    ticketsFaseActual: dto.ticketsFaseActual,
    ritmo: RITMOS[dto.ritmo],
    salud: {
      rechazosQaMes: dto.salud.rechazosQaMes,
      diasPromedioEnQa:
        dto.salud.diasPromedioEnQa === null ? '—' : `${dto.salud.diasPromedioEnQa.toFixed(1)} días`,
      horas: `${Math.round(dto.salud.horasEstimadas)} / ${Math.round(dto.salud.horasReales)} h`,
      historiasSinTickets: dto.salud.historiasSinTickets,
    },
    detenidos: dto.detenidos.map((d) => {
      const dias = d.desde ? diasDesde(aFechaHora(d.desde)!) : 0;
      const bloqueado = d.estado === 'BLOQUEADA';
      return {
        ticketId: d.ticketId,
        codigo: d.codigo,
        titulo: d.titulo,
        bloqueado,
        detalle: bloqueado
          ? `Bloqueada ${plural(dias, 'día', 'días')}${d.motivo ? ' · ' + d.motivo : ''}`
          : `En QA hace ${plural(dias, 'día', 'días')}`,
      };
    }),
    puedeEditar: dto.puedeEditar,
    puedeCrearFase: dto.puedeCrearFase,
    puedeCrearHistoria: dto.puedeCrearHistoria,
    puedeCrearTicket: dto.puedeCrearTicket,
  };
}

export function aArbol(dto: FaseArbolDto[]): FaseArbol[] {
  return dto.map((f) => ({
    fase: aFase(f.fase),
    historias: f.historias.map(
      (h): HistoriaArbol => ({
        id: h.id,
        codigo: h.codigo,
        titulo: h.titulo,
        estado: h.estado,
        estadoTexto: textoEstado(h.estado),
        analistaId: h.analistaId,
        analista: { iniciales: h.analistaIniciales, tono: tonoDeAvatar(h.analistaId) },
        aceptaTickets: ['LISTA', 'LISTA_DESARROLLO', 'EN_DESARROLLO'].includes(h.estado),
        listos: h.listos,
        total: h.total,
        tickets: h.tickets.map(aTicket),
      }),
    ),
    sueltos: f.sueltos.map(aTicket),
  }));
}

export function aPersona(dto: PersonaDto): Persona {
  return {
    id: dto.id,
    nombreCompleto: dto.nombreCompleto,
    iniciales: dto.iniciales,
    roles: ordenarRoles(dto.roles),
    activo: dto.activo,
    tono: tonoDeAvatar(dto.id, dto.activo),
  };
}

export function aDatos(dto: ConfiguracionDto): DatosProyecto {
  return {
    nombre: dto.nombre,
    clienteId: dto.clienteId,
    prioridad: dto.prioridad,
    estado: dto.estado,
    fechaInicio: dto.fechaInicio ?? '',
    fechaEntrega: dto.fechaEntrega ?? '',
    descripcion: dto.descripcion ?? '',
    liderId: dto.liderId,
    miembroIds: dto.miembroIds,
    modulos: dto.modulos.map((m) => ({
      id: m.id,
      tipo: m.tipo,
      nombre: m.nombre,
      tecnologia: m.tecnologia ?? '',
      repositorio: m.repositorio ?? '',
      ramaBase: m.ramaBase ?? 'develop',
    })),
    reglas: dto.reglas,
  };
}

export function aRequest(datos: DatosProyecto): ProyectoRequestDto {
  const texto = (valor: string) => valor.trim() || null;
  return {
    nombre: datos.nombre.trim(),
    clienteId: datos.clienteId,
    prioridad: datos.prioridad,
    estado: datos.estado,
    fechaInicio: texto(datos.fechaInicio),
    fechaEntrega: texto(datos.fechaEntrega),
    descripcion: texto(datos.descripcion),
    liderId: datos.liderId!,
    miembroIds: datos.miembroIds,
    modulos: datos.modulos.map((m) => ({
      id: m.id,
      tipo: m.tipo,
      nombre: m.nombre.trim(),
      tecnologia: texto(m.tecnologia),
      repositorio: texto(m.repositorio),
      ramaBase: texto(m.ramaBase),
    })),
    reglas: datos.reglas,
  };
}

function aFase(dto: FaseDto): FaseResumen {
  const inicio = aFecha(dto.fechaInicio);
  const fin = aFecha(dto.fechaFin);
  const publicada = dto.estado === 'PUBLICADA';
  return {
    id: dto.id,
    numero: dto.numero,
    titulo: dto.mantenimiento ? dto.nombre : `Fase ${dto.numero} · ${dto.nombre}`,
    nombre: dto.nombre,
    estado: dto.estado,
    etiqueta: etiquetaFase(dto.estado),
    fechaInicio: inicio,
    fechaFin: fin,
    fechas: rango(inicio, fin, false),
    versionRelease: dto.versionRelease,
    mantenimiento: dto.mantenimiento,
    modulos: dto.modulos.map(aChip),
    historias: dto.historias,
    tickets: dto.tickets,
    avance: dto.avance,
    tonoAvance: publicada ? 'verde' : 'morado',
  };
}

function aTicket(dto: TicketArbolDto): TicketArbol {
  return {
    id: dto.id,
    tipo: dto.tipo,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estado: dto.estado,
    estadoTexto: textoEstado(dto.estado),
    modulo: dto.modulo ? aChip(dto.modulo) : null,
    responsableId: dto.responsableId,
    responsable:
      dto.responsableId && dto.responsableIniciales
        ? { iniciales: dto.responsableIniciales, tono: tonoDeAvatar(dto.responsableId), nombre: dto.responsableNombre ?? '' }
        : null,
    accion: dto.accion,
    quien: dto.quien,
  };
}

function aMiembro(dto: MiembroDto): Miembro {
  return {
    id: dto.id,
    nombreCompleto: dto.nombreCompleto,
    iniciales: dto.iniciales,
    roles: ordenarRoles(dto.roles),
    tono: tonoDeAvatar(dto.id),
  };
}

function aChip(tipo: string): ModuloChip {
  return { tipo, ...infoModulo(tipo) };
}

/** "3 feb – 30 may 2026" (con año) o "24 feb – 25 abr" */
function rango(inicio: Date | null, fin: Date | null, conAnio: boolean): string {
  if (!inicio && !fin) return '';
  const formato = (d: Date | null) => (d ? (conAnio ? fechaCorta(d) : diaYMes(d)) : '…');
  if (conAnio && inicio && fin && inicio.getFullYear() === fin.getFullYear()) {
    return `${diaYMes(inicio)} – ${fechaCorta(fin)}`;
  }
  return `${formato(inicio)} – ${formato(fin)}`;
}

function diasDesde(fecha: Date): number {
  return Math.max(0, Math.floor((Date.now() - fecha.getTime()) / 86_400_000));
}

function plural(n: number, uno: string, varios: string): string {
  return `${n} ${n === 1 ? uno : varios}`;
}
