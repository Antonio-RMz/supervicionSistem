import { Tono } from '../../../shared/models/etiqueta.model';
import { aFechaHora, diaYMes } from '../../../shared/utils/fechas';
import { etiquetaDeRol, ordenarRoles, tonoDeAvatar } from '../../../shared/utils/roles';
import { textoEstado } from '../../../shared/utils/tickets';
import {
  Accesos,
  DiaDeActividad,
  Hecho,
  NuevoUsuario,
  PerfilUsuario,
  Permiso,
  Rol,
  Tarea,
  UsuarioFila,
} from '../models/usuarios.models';
import {
  AccesosDto,
  NuevoUsuarioDto,
  PerfilDto,
  PermisoDto,
  RolDto,
  TareaDto,
  UsuarioFilaDto,
} from './usuarios.dto';

/** Color del punto en "Lo que hizo". */
const TONO_HECHO: Record<string, Tono> = {
  INICIO: 'verde',
  ENVIO: 'morado',
  RECHAZO: 'naranja',
  CIERRE: 'turquesa',
  COMENTARIO: 'gris',
};

export function aUsuarioFila(dto: UsuarioFilaDto): UsuarioFila {
  const roles = ordenarRoles(dto.roles);
  return {
    id: dto.id,
    nombreCompleto: dto.nombreCompleto,
    iniciales: dto.iniciales,
    correo: dto.correo,
    roles,
    etiquetasRol: roles.map(etiquetaDeRol),
    proyectos: dto.proyectos,
    activo: dto.activo,
    tonoAvatar: tonoDeAvatar(dto.id, dto.activo),
  };
}

export function aRol(dto: RolDto): Rol {
  return { codigo: dto.codigo, nombre: dto.nombre, descripcion: dto.descripcion ?? '', permisos: dto.permisos };
}

export function aPermiso(dto: PermisoDto): Permiso {
  return { codigo: dto.codigo, nombre: dto.nombre, grupo: dto.grupo };
}

export function aAccesos(dto: AccesosDto): Accesos {
  return {
    roles: ordenarRoles(dto.roles),
    permisosExtra: dto.permisosExtra.map((p) => ({
      permisoCodigo: p.permisoCodigo,
      permisoNombre: p.permisoNombre ?? p.permisoCodigo,
      proyectoId: p.proyectoId,
      proyectoNombre: p.proyectoNombre ?? 'Todos los proyectos',
      otorgado: p.otorgado,
    })),
  };
}

/** Al guardar solo viajan códigos e ids; los nombres los pone el back. */
export function aAccesosDto(accesos: Accesos): AccesosDto {
  return {
    roles: accesos.roles,
    permisosExtra: accesos.permisosExtra.map((p) => ({
      permisoCodigo: p.permisoCodigo,
      proyectoId: p.proyectoId,
      otorgado: p.otorgado,
    })),
  };
}

export function aNuevoUsuarioDto(nuevo: NuevoUsuario): NuevoUsuarioDto {
  return { ...nuevo, apellidos: nuevo.apellidos.trim() || null };
}

export function aPerfil(dto: PerfilDto, ahora = new Date()): PerfilUsuario {
  return {
    id: dto.id,
    nombreCompleto: dto.nombreCompleto,
    iniciales: dto.iniciales,
    correo: dto.correo,
    etiquetasRol: ordenarRoles(dto.roles).map(etiquetaDeRol),
    activo: dto.activo,
    tonoAvatar: tonoDeAvatar(dto.id, dto.activo),
    ultimoAcceso: aFechaHora(dto.ultimoAcceso),
    metricas: dto.metricas,
    trabajandoAhora: dto.trabajandoAhora.map(aTarea),
    leToca: dto.leToca.map(aTarea),
    esperandoAOtros: dto.esperandoAOtros.map(aTarea),
    actividad: porDia(
      dto.actividad.map((h) => ({
        descripcion: h.descripcion,
        tono: TONO_HECHO[h.clase] ?? 'gris',
        fecha: aFechaHora(h.fecha),
      })),
      ahora,
    ),
    proyectos: dto.proyectos,
  };
}

function aTarea(dto: TareaDto): Tarea {
  return {
    id: dto.id,
    tipo: dto.tipo,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estadoTexto: dto.rechazadaEnQa ? 'Rechazada en QA' : textoEstado(dto.estado),
    accion: dto.accion,
    quien: dto.actorNombre ? `${dto.actorNombre} · ${dto.actorRol}` : null,
    rechazadaEnQa: dto.rechazadaEnQa,
    actualizadoEn: aFechaHora(dto.actualizadoEn),
  };
}

/** Los hechos ya vienen del más reciente al más antiguo; se agrupan sin reordenar. */
function porDia(hechos: Hecho[], ahora: Date): DiaDeActividad[] {
  const dias: DiaDeActividad[] = [];
  for (const hecho of hechos) {
    const dia = nombreDelDia(hecho.fecha, ahora);
    const ultimo = dias.at(-1);
    if (ultimo?.dia === dia) {
      ultimo.hechos.push(hecho);
    } else {
      dias.push({ dia, hechos: [hecho] });
    }
  }
  return dias;
}

function nombreDelDia(fecha: Date | null, ahora: Date): string {
  if (!fecha) return 'Sin fecha';
  const inicio = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const dias = Math.round((inicio(ahora) - inicio(fecha)) / 86_400_000);
  if (dias === 0) return 'Hoy';
  if (dias === 1) return 'Ayer';
  return diaYMes(fecha);
}
