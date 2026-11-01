import { aFechaHora } from '../../../shared/utils/fechas';
import { textoEstado } from '../../../shared/utils/tickets';
import { Dashboard, ItemPendiente, Metrica } from '../models/dashboard.models';
import { DashboardDto, ItemPendienteDto, MetricasDto } from './dashboard.dto';

/** DTO del back -> modelo listo para pintar. */
export function aDashboard(dto: DashboardDto): Dashboard {
  return {
    proyectoNombre: dto.proyectoNombre,
    metricas: aMetricas(dto.metricas),
    pendientesMios: dto.pendientesMios,
    teToca: dto.teToca.map(aItem),
    esperandoAOtros: dto.esperandoAOtros.map(aItem),
    fases: dto.fases.map((f) => ({
      nombre: `Fase ${f.numero} · ${f.nombre}`,
      avance: f.avance,
      tono: f.estado === 'PUBLICADA' ? 'verde' : 'morado',
    })),
    actividad: dto.actividad.map((m) => ({ descripcion: m.descripcion, fecha: aFechaHora(m.fecha) })),
  };
}

function aMetricas(m: MetricasDto): Metrica[] {
  return [
    {
      titulo: 'Tickets abiertos',
      valor: m.ticketsAbiertos,
      detalle: `+${m.abiertosEstaSemana} esta semana`,
      alerta: false,
    },
    {
      titulo: 'En QA',
      valor: m.enQa,
      detalle:
        m.enQaMasDe3Dias === 0
          ? 'Ninguno atorado'
          : `${m.enQaMasDe3Dias} ${m.enQaMasDe3Dias === 1 ? 'lleva' : 'llevan'} más de 3 días`,
      alerta: false,
    },
    {
      titulo: 'Bugs abiertos',
      valor: m.bugsAbiertos,
      detalle: m.bugsPrioridadAlta ? `${m.bugsPrioridadAlta} de prioridad alta` : 'Ninguno de prioridad alta',
      alerta: m.bugsAbiertos > 0,
    },
    {
      titulo: 'Cerrados esta semana',
      valor: m.historiasCerradasSemana + m.ticketsCerradosSemana,
      detalle: `Historias: ${m.historiasCerradasSemana} · Tickets: ${m.ticketsCerradosSemana}`,
      alerta: false,
    },
  ];
}

function aItem(dto: ItemPendienteDto): ItemPendiente {
  return {
    id: dto.id,
    tipo: dto.tipo,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estado: dto.estado,
    estadoTexto: textoEstado(dto.estado),
    accion: dto.accion,
    ejecutable: dto.ejecutable,
    quien: dto.actorNombre ? `${dto.actorNombre} · ${dto.actorRol}` : null,
  };
}
