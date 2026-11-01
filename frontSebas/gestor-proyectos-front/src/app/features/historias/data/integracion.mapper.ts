import { Etiqueta } from '../../../shared/models/etiqueta.model';
import { aFechaHora, diaYHora, diaYMes, haceCuanto } from '../../../shared/utils/fechas';
import { infoModulo } from '../../../shared/utils/proyectos';
import { codigoHistoria, etiquetaHistoria } from '../../../shared/utils/tickets';
import { IntegracionHistoria, PasoFlujo, PuntoRevision, TicketAIntegrar } from '../models/integracion.models';
import { IntegracionDto, TicketIntegrarDto } from './integracion.dto';

const ESTADO_PR: Record<string, Etiqueta> = {
  ABIERTO: { texto: 'En revisión', tono: 'ambar' },
  APROBADO: { texto: 'Aprobado', tono: 'verde' },
  CERRADO: { texto: 'Cerrado', tono: 'rojo' },
};

const CI: Record<string, Etiqueta> = {
  PASO: { texto: 'CI ✓', tono: 'verde' },
  FALLO: { texto: 'CI ✗', tono: 'rojo' },
  PENDIENTE: { texto: 'CI corriendo', tono: 'ambar' },
};

export function aIntegracion(dto: IntegracionDto, ahora = new Date()): IntegracionHistoria {
  const tickets = dto.modulos.reduce((n, m) => n + m.tickets.length, 0);
  const faltan = dto.revision.filter((r) => r.resultado === 'FALTA').length;
  const avisos = dto.revision.filter((r) => r.resultado === 'AVISO').length;
  const porIntegrar = dto.historiasDeFase.length;
  const release = dto.version ? `el release ${dto.version}` : `el release de la Fase ${dto.faseNumero}`;
  return {
    historiaId: dto.historiaId,
    proyectoId: dto.proyectoId,
    proyectoNombre: dto.proyectoNombre,
    faseId: dto.faseId,
    faseTitulo: `Fase ${dto.faseNumero}`,
    codigo: dto.codigo,
    titulo: dto.titulo,
    estado: dto.estado,
    etiqueta: etiquetaHistoria(dto.estado),
    descripcion: descripcion(dto, release),
    rol: dto.rol,
    impedimentos: dto.impedimentos,
    flujo: flujo(dto),
    resumen: `${dto.modulos.length} ${dto.modulos.length === 1 ? 'repositorio' : 'repositorios'} · `
      + `${tickets} ${tickets === 1 ? 'ticket' : 'tickets'}`,
    modulos: dto.modulos.map((m) => {
      const info = infoModulo(m.tipo);
      return {
        nombre: m.nombre,
        descripcion: [info.largo, m.tecnologia?.split(' · ').pop()].filter(Boolean).join(' · '),
        icono: info.icono,
        tono: info.tono,
        tickets: m.tickets.map(aTicket),
      };
    }),
    revision: dto.revision.map(
      (r): PuntoRevision => ({
        texto: r.texto,
        estado: r.resultado === 'OK' ? 'ok' : r.resultado === 'AVISO' ? 'aviso' : 'falta',
        detalle: [r.detalle, diaYHora(aFechaHora(r.fecha), ahora)].filter(Boolean).join(' · '),
      }),
    ),
    resumenRevision: faltan ? `Falta${faltan === 1 ? '' : 'n'} ${faltan}` : avisos ? 'Con avisos' : 'Todo listo',
    ambientes: dto.ambientes.map((a, i) => {
      const fecha = aFechaHora(a.fecha);
      return {
        nombre: a.nombre,
        tono: i === 0 ? 'morado' : i === 1 ? 'naranja' : 'verde',
        // develop dice hace cuánto se integró; producción, el día que se publicó
        texto: [a.version, a.detalle, i === 0 ? haceCuanto(fecha, ahora) : diaYMes(fecha)].filter(Boolean).join(' · '),
      };
    }),
    historiasDeFase: dto.historiasDeFase.map((h) => ({
      id: h.id,
      codigo: codigoHistoria(h.numero),
      etiqueta: h.id === dto.historiaId && h.estado === 'ACEPTADA'
        ? { texto: 'Integrando ahora', tono: 'morado' }
        : etiquetaHistoria(h.estado),
    })),
    notaRelease: porIntegrar === 1
      ? `${capital(release)} se habilita cuando esta historia esté integrada.`
      : `${capital(release)} se habilita cuando las ${porIntegrar} estén integradas.`,
    pasos: dto.pasos,
  };
}

function aTicket(t: TicketIntegrarDto): TicketAIntegrar {
  let chips: Etiqueta[];
  if (t.enDevelop) {
    chips = [{ texto: 'Ya en develop', tono: 'verde' }];
  } else if (t.prNumero === null) {
    chips = [{ texto: 'Sin PR registrado', tono: 'ambar' }];
  } else {
    chips = [
      ESTADO_PR[t.prEstado ?? ''] ?? { texto: t.prEstado ?? '', tono: 'gris' },
      CI[t.prCi ?? ''] ?? { texto: 'CI', tono: 'gris' },
      t.conConflictos ? { texto: 'Con conflictos', tono: 'rojo' } : { texto: 'Sin conflictos', tono: 'verde' },
    ];
  }
  return {
    id: t.id,
    tipo: t.tipo,
    codigo: t.codigo,
    titulo: t.titulo,
    rama: t.rama,
    pr: t.prNumero === null ? null : `PR #${t.prNumero}`,
    chips,
  };
}

/** Lo que dice debajo del título según dónde va la historia. */
function descripcion(dto: IntegracionDto, release: string): string {
  const quien = dto.revision[0]?.detalle?.split(' ')[0] ?? 'el Analista';
  switch (dto.estado) {
    case 'ACEPTADA':
      return `La historia ya fue aceptada por ${quien}. Integrar hace merge de sus PR a develop, despliega staging `
        + 'y la deja lista para el release de la fase.';
    case 'INTEGRADA':
      return `Ya está en develop y staging. Sale a producción con ${release}.`;
    case 'CERRADA':
      return `Ya está en producción con ${release}.`;
    case 'DESCARTADA':
      return 'La historia se descartó: no hay nada que integrar.';
    default:
      return 'Todavía no se puede integrar: primero sus tickets se aprueban y el Analista la acepta.';
  }
}

/** Ramas → develop → staging → release, como en el PDF. */
function flujo(dto: IntegracionDto): PasoFlujo[] {
  const integrada = ['INTEGRADA', 'CERRADA'].includes(dto.estado);
  const enTrabajo = ['EN_DESARROLLO', 'EN_VALIDACION'].includes(dto.estado);
  const ramas = (dto.prefijosRama.length ? dto.prefijosRama : ['feature/']).map((p) => `${p}…`).join(' · ');
  return [
    {
      titulo: 'Ramas de tickets',
      detalle: ramas,
      avance: dto.estado === 'ACEPTADA' || integrada ? 'hecho' : enTrabajo ? 'actual' : 'pendiente',
    },
    {
      titulo: 'develop',
      detalle: integrada ? 'integrada' : dto.prsPorIntegrar ? `merge de ${dto.prsPorIntegrar} PR` : 'merge de sus ramas',
      avance: integrada ? 'hecho' : dto.estado === 'ACEPTADA' ? 'actual' : 'pendiente',
    },
    { titulo: 'staging', detalle: 'despliegue automático', avance: integrada ? 'hecho' : 'pendiente' },
    {
      titulo: dto.version ? `Release ${dto.version}` : 'Release',
      detalle: `al cerrar la Fase ${dto.faseNumero}`,
      avance: dto.estado === 'CERRADA' ? 'hecho' : dto.estado === 'INTEGRADA' ? 'actual' : 'pendiente',
    },
  ];
}

function capital(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
