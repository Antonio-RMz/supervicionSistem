import { Tono } from '../../../shared/models/etiqueta.model';
import { tonoDeAvatar } from '../../../shared/utils/roles';
import { Columna, MovimientoTarjeta, Tablero, Tarjeta } from '../models/tablero.models';
import { MovimientoDto, TableroDto, TarjetaDto } from './tablero.dto';

const TONO_PRIORIDAD: Record<string, Tono> = { ALTA: 'rojo', MEDIA: 'naranja', BAJA: 'gris' };

export function aTablero(dto: TableroDto): Tablero {
  return {
    fases: dto.fases.map((f) => ({
      id: f.id,
      nombre: f.mantenimiento ? f.nombre : `Fase ${f.numero} · ${f.nombre}`,
    })),
    faseId: dto.faseId,
    tarjetas: dto.tarjetas.map(aTarjeta),
  };
}

function aTarjeta(dto: TarjetaDto): Tarjeta {
  return {
    id: dto.id,
    tipo: dto.tipo,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estado: dto.estado,
    tonoPrioridad: TONO_PRIORIDAD[dto.prioridad] ?? 'gris',
    textoPrioridad: `Prioridad ${dto.prioridad.toLowerCase()}`,
    columna: dto.columna as Columna,
    historia: dto.historiaCodigo,
    responsable:
      dto.responsableId && dto.responsableNombre
        ? {
            id: dto.responsableId,
            nombre: dto.responsableNombre,
            iniciales: dto.responsableIniciales ?? dto.responsableNombre.slice(0, 2).toUpperCase(),
            tono: tonoDeAvatar(dto.responsableId),
          }
        : null,
    avance: aMovimiento(dto.avance),
    rechazo: aMovimiento(dto.rechazo),
    bloqueadaPor: dto.bloqueadaPor,
    mia: dto.mia,
  };
}

function aMovimiento(dto: MovimientoDto | null): MovimientoTarjeta | null {
  return dto ? { tipo: dto.tipo, accion: dto.accion, columnaDestino: dto.columnaDestino as Columna } : null;
}
