import { aFechaHora, haceCuanto } from '../../../shared/utils/fechas';
import { etiquetaHistoria, etiquetaPrioridad } from '../../../shared/utils/tickets';
import { ListadoHistorias } from '../models/listado-historias.models';
import { ListadoHistoriasDto } from './listado-historias.dto';

export function aListadoHistorias(dto: ListadoHistoriasDto, ahora = new Date()): ListadoHistorias {
  return {
    proyectoId: dto.proyectoId,
    proyectoNombre: dto.proyectoNombre,
    puedeCrear: dto.puedeCrear,
    historias: dto.historias.map((h) => ({
      id: h.id,
      codigo: h.codigo,
      titulo: h.titulo,
      estado: h.estado,
      etiqueta: etiquetaHistoria(h.estado),
      prioridad: etiquetaPrioridad(h.prioridad),
      puntos: h.puntos,
      faseId: h.faseId,
      fase: h.faseNumero === 0 ? h.faseNombre : `Fase ${h.faseNumero} · ${h.faseNombre}`,
      analista: h.analistaNombre,
      ticketsTerminados: h.ticketsTerminados,
      ticketsTotal: h.ticketsTotal,
      avance: h.ticketsTotal ? Math.round((h.ticketsTerminados * 100) / h.ticketsTotal) : 0,
      actualizada: haceCuanto(aFechaHora(h.actualizadoEn), ahora),
    })),
  };
}
