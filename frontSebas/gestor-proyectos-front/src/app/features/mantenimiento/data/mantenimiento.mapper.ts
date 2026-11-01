import { Tono } from '../../../shared/models/etiqueta.model';
import { aFechaHora, diaYMes } from '../../../shared/utils/fechas';
import { infoModulo } from '../../../shared/utils/proyectos';
import { nombresDeRoles, tonoDeAvatar } from '../../../shared/utils/roles';
import { etiquetaEstadoTicket } from '../../../shared/utils/tickets';
import { SEVERIDADES } from '../../tickets/models/ticket-detalle.models';
import { Mantenimiento, PreparacionMantenimiento, TicketMantenimiento } from '../models/mantenimiento.models';
import { MantenimientoDto, PreparacionMantenimientoDto, TicketMantenimientoDto } from './mantenimiento.dto';

export function aMantenimiento(dto: MantenimientoDto): Mantenimiento {
  const fase = dto.proximaFaseNumero ? `la Fase ${dto.proximaFaseNumero}` : 'la siguiente fase';
  const horas = dto.horasMedioHotfix;
  return {
    faseId: dto.faseId,
    enProduccion: dto.enProduccion,
    siguienteParche: dto.siguienteParche,
    metricas: [
      {
        titulo: 'Urgentes en producción',
        valor: String(dto.urgentesConfirmados),
        detalle: dto.criticos === 1 ? '1 crítica' : `${dto.criticos} críticas`,
        alerta: dto.urgentesConfirmados > 0,
      },
      { titulo: 'Listos para el parche', valor: String(dto.listos.length), detalle: dto.siguienteParche, alerta: false },
      { titulo: 'Mantenimiento normal', valor: String(dto.normales.length), detalle: `va con ${fase}`, alerta: false },
      {
        titulo: 'Tiempo medio de hotfix',
        valor: horas === null ? '—' : horas < 1 ? `${Math.round(horas * 60)} min` : `${Math.round(horas)} h`,
        detalle: 'últimos 30 días',
        alerta: false,
      },
    ],
    urgentes: dto.urgentes.map(aTicket),
    listos: dto.listos.map(aTicket),
    normales: dto.normales.map(aTicket),
    conLaFase: `va con ${fase}`,
    parches: dto.parches.map((p) => ({
      version: p.version,
      fecha: diaYMes(aFechaHora(p.fecha)),
      bugs: p.bugs === 1 ? '1 bug' : `${p.bugs} bugs`,
    })),
    puedePublicar: dto.puedePublicar,
  };
}

function aTicket(t: TicketMantenimientoDto): TicketMantenimiento {
  const severidad = SEVERIDADES.find((s) => s.codigo === t.severidad);
  return {
    id: t.id,
    tipo: t.tipo,
    codigo: t.codigo,
    titulo: t.titulo,
    estado: t.estado,
    etiqueta: etiquetaEstadoTicket(t.estado),
    severidad: severidad
      ? { texto: severidad.texto, tono: severidad.tono }
      : t.porConfirmar ? { texto: 'Por confirmar', tono: 'gris' as Tono } : null,
    rama: t.rama,
    responsable: t.responsableId && t.responsableNombre
      ? { nombre: t.responsableNombre, iniciales: iniciales(t.responsableNombre), tono: tonoDeAvatar(t.responsableId) }
      : null,
  };
}

/** "Pedro Ramírez" -> "PR" */
function iniciales(nombre: string): string {
  return nombre.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

export function aPreparacion(dto: PreparacionMantenimientoDto): PreparacionMantenimiento {
  return {
    enProduccion: dto.enProduccion,
    conLaFase: dto.proximaFaseNumero ? `la Fase ${dto.proximaFaseNumero}` : 'la siguiente fase',
    modulos: dto.modulos.map((m) => {
      const info = infoModulo(m.tipo);
      // Con dos módulos del mismo tipo, el botón muestra el nombre para distinguirlos.
      const repetido = dto.modulos.filter((o) => o.tipo === m.tipo).length > 1;
      return { id: m.id, nombre: `${m.nombre} · ${info.largo}`, corto: repetido ? m.nombre : info.corto, icono: info.icono };
    }),
    equipo: dto.equipo.map((p) => ({
      id: p.id,
      nombre: p.roles.length ? `${p.nombreCompleto} · ${nombresDeRoles(p.roles)}` : p.nombreCompleto,
      desarrollador: p.roles.includes('DESARROLLADOR'),
    })),
    siguientes: dto.siguientes,
    historias: dto.historias.map((h) => ({ id: h.id, nombre: `${h.codigo} · ${h.titulo}` })),
    puedeCrear: dto.puedeCrear,
    puedeReportar: dto.puedeReportar,
  };
}
