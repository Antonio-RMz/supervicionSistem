import { tonoDeAvatar } from '../../../shared/utils/roles';
import { etiquetaPrioridad, textoEstado } from '../../../shared/utils/tickets';
import { TicketMio } from '../models/mis-tickets.models';
import { TicketMioDto } from './mis-tickets.dto';

export function aTicketMio(dto: TicketMioDto): TicketMio {
  const fase = `Fase ${dto.faseNumero}`;
  return {
    id: dto.id,
    tipo: dto.tipo,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estado: dto.estado,
    estadoTexto: dto.rechazadaEnQa ? 'Rechazada en QA' : textoEstado(dto.estado),
    prioridad: dto.prioridad,
    etiquetaPrioridad: etiquetaPrioridad(dto.prioridad),
    fase,
    historiaOFase: dto.historiaCodigo ?? fase,
    historia: dto.historiaCodigo,
    responsable:
      dto.responsableId && dto.responsableNombre
        ? {
            nombre: dto.responsableNombre,
            iniciales: dto.responsableIniciales ?? dto.responsableNombre.slice(0, 2).toUpperCase(),
            tono: tonoDeAvatar(dto.responsableId),
          }
        : null,
    accion: dto.accion,
    rechazadaEnQa: dto.rechazadaEnQa,
    meToca: dto.meToca,
    asignado: dto.asignado,
    creadoPorMi: dto.creadoPorMi,
    ejecutable: dto.ejecutable,
  };
}
