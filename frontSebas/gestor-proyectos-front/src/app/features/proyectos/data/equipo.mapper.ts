import { aFechaHora, haceCuanto } from '../../../shared/utils/fechas';
import { infoModulo } from '../../../shared/utils/proyectos';
import { etiquetaDeRol, nombresDeRoles, ordenarRoles, tonoDeAvatar } from '../../../shared/utils/roles';
import { Candidato, EquipoProyecto, MiembroEquipo } from '../models/equipo.models';
import { CandidatoDto, EquipoDto, MiembroDto } from './equipo.dto';

/** Con 10 tickets abiertos la barra de carga se ve llena. */
const CARGA_LLENA = 10;

const TONO_CARGA = { BAJA: 'morado', MEDIA: 'naranja', ALTA: 'rojo' } as const;

export function aEquipo(dto: EquipoDto, ahora = new Date()): EquipoProyecto {
  const r = dto.reglas;
  return {
    miembros: dto.miembros.map((m) => aMiembro(m, ahora)),
    cobertura: dto.cobertura.map((c) => ({
      rol: c.rol,
      estado: c.resultado === 'OK' ? 'ok' : c.resultado === 'AVISO' ? 'aviso' : 'falta',
      quien: c.quien,
    })),
    alertas: dto.alertas,
    permisos: dto.permisos.map((p) => ({
      permiso: p.permiso,
      usuarioId: p.usuarioId,
      usuario: p.usuario,
      etiqueta: p.otorgado ? { texto: 'Otorgado', tono: 'verde' } : { texto: 'Quitado', tono: 'rojo' },
    })),
    reglas: [
      { nombre: 'Autovalidación', valor: r.permitirAutovalidacion ? 'Permitida' : 'No permitida' },
      { nombre: 'Comentario al rechazar', valor: r.comentarioAlRechazar ? 'Obligatorio' : 'Opcional' },
      { nombre: 'Rama al iniciar', valor: r.ramaAlIniciar ? 'Automática' : 'Manual' },
      { nombre: 'Avisar al cliente por fase', valor: r.notificarClienteFase ? 'Sí' : 'No' },
    ],
    puedeGestionar: dto.puedeGestionar,
  };
}

function aMiembro(m: MiembroDto, ahora: Date): MiembroEquipo {
  const acceso = aFechaHora(m.ultimoAcceso);
  const nombre = m.nombreCompleto.split(' ')[0];
  return {
    id: m.id,
    nombreCompleto: m.nombreCorto,
    nombre,
    iniciales: m.iniciales,
    tono: tonoDeAvatar(m.id),
    roles: ordenarRoles(m.roles).map(etiquetaDeRol),
    actividad: acceso ? `Último acceso ${haceCuanto(acceso, ahora)}` : 'Aún no entra al sistema',
    esLider: m.esLider,
    modulos: m.modulos.map(infoModulo),
    ticketsAbiertos: m.ticketsAbiertos,
    cargaTexto: m.ticketsAbiertos === 1 ? '1 ticket abierto' : `${m.ticketsAbiertos} tickets abiertos`,
    cargaPorcentaje: Math.min(100, (m.ticketsAbiertos * 100) / CARGA_LLENA),
    cargaTono: TONO_CARGA[m.carga],
    leToca: m.leToca,
  };
}

export function aCandidato(c: CandidatoDto): Candidato {
  return { id: c.id, nombreCompleto: c.nombreCompleto, roles: nombresDeRoles(c.roles) };
}
