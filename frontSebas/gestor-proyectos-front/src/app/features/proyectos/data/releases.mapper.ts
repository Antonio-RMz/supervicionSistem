import { aFechaHora, diaYMes } from '../../../shared/utils/fechas';
import { ReleasesProyecto } from '../models/releases.models';
import { ReleasesDto } from './releases.dto';
import { codigoHistoria } from '../../../shared/utils/tickets';

export function aReleases(dto: ReleasesDto): ReleasesProyecto {
  const p = dto.parche;
  return {
    listasParaIntegrar: dto.listasParaIntegrar.map((h) => ({
      id: h.id,
      codigo: codigoHistoria(h.numero),
      titulo: h.titulo,
    })),
    puedeIntegrar: dto.puedeIntegrar,
    proximaFaseId: dto.proximaFaseId,
    parche: p
      ? {
          version: p.version,
          resumen: [
            p.aprobados ? `${p.aprobados} ${p.aprobados === 1 ? 'bug aprobado' : 'bugs aprobados'}` : '',
            p.enCamino ? `${p.enCamino} en corrección` : '',
          ].filter(Boolean).join(', ') + ' · se publica sin esperar a la fase',
          mantenimientoFaseId: p.mantenimientoFaseId,
        }
      : null,
    publicados: dto.publicados.map((r) => {
      const esFase = r.tipo === 'FASE';
      return {
        id: r.id,
        version: r.version,
        esFase,
        faseId: r.faseId,
        origen: esFase ? `Fase ${r.faseNumero} · ${r.faseNombre}` : 'Parche · Mantenimiento',
        revertida: r.estado === 'REVERTIDA',
        fecha: fechaCorta(aFechaHora(r.fecha)),
        contenido: esFase
          ? `${r.historias} ${r.historias === 1 ? 'historia' : 'historias'} · ${r.tickets} ${r.tickets === 1 ? 'ticket' : 'tickets'}`
          : `${r.bugs.length} ${r.bugs.length === 1 ? 'bug' : 'bugs'}`,
        bugs: r.bugs,
        publicadoPor: r.publicadoPor ?? '—',
        enProduccion: r.enProduccion,
      };
    }),
  };
}

/** "21 feb 2026" */
function fechaCorta(fecha: Date | null): string {
  return fecha ? `${diaYMes(fecha)} ${fecha.getFullYear()}` : '—';
}
