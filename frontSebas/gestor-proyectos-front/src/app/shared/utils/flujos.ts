// Los pasos de cada tipo de ticket y quién actúa en cada uno, para mostrarlos ("Flujo que seguirá").
// Es el mismo flujo que aplica el back (FlujoTrabajo): aquí solo se describe.

export type Actor = 'responsable' | 'tester' | 'lider' | null;

/** El tipo de usuario que actúa en cada paso, como se ve en el Flujo. */
export const ROL_DE_ACTOR: Record<NonNullable<Actor>, string> = {
  responsable: 'Desarrollador',
  // Quien prueba el hotfix es un Analista
  tester: 'Analista',
  lider: 'Líder',
};

/** Color de la etiqueta de cada rol en el Flujo. */
export const TONO_ROL: Partial<Record<string, string>> = {
  Líder: 'morado',
  Analista: 'azul',
  Desarrollador: 'verde',
};

export interface PasoFlujo {
  /** "EN_QA": para saber en qué paso va el ticket */
  codigo: string;
  estado: string;
  /** Quién hace la acción para salir de este paso; null si avanza solo */
  actor: Actor;
  /** "inicia", "revisa el PR", "se integra con su historia"... (quién lo hace va en su rol) */
  accion: string;
}

export const TIPOS_TICKET: { tipo: string; nombre: string; descripcion: string }[] = [
  { tipo: 'ACTV', nombre: 'Actividad', descripcion: 'Funcionalidad nueva' },
  { tipo: 'BUGS', nombre: 'Error', descripcion: 'Algo que no funciona' },
  { tipo: 'MEJR', nombre: 'Mejora', descripcion: 'Cambio a algo existente' },
];

// El Líder ya no cierra los tickets: pasan solos a Integrada al integrar su historia
// y a Cerrada al publicar la fase (PDF pág. 22). Cada ticket manda su PR y lo revisa el Líder;
// QA se hace a la historia completa.
export const FLUJOS: Record<string, PasoFlujo[]> = {
  ACTV: [
    // Nace en Borrador: el Líder la termina de definir y la marca Lista.
    { codigo: 'BORRADOR', estado: 'Borrador', actor: 'lider', accion: 'la marca Lista' },
    { codigo: 'POR_HACER', estado: 'Por hacer', actor: 'responsable', accion: 'inicia' },
    { codigo: 'EN_PROGRESO', estado: 'En progreso', actor: 'responsable', accion: 'envía su PR' },
    { codigo: 'EN_REVISION', estado: 'PR en revisión', actor: 'lider', accion: 'revisa el PR' },
    { codigo: 'APROBADA', estado: 'Aprobada', actor: null, accion: 'QA prueba la historia completa' },
    { codigo: 'INTEGRADA', estado: 'Integrada', actor: null, accion: 'se cierra al publicar la fase' },
    { codigo: 'CERRADA', estado: 'Cerrada', actor: null, accion: '' },
  ],
  BUGS: [
    { codigo: 'REPORTADO', estado: 'Reportado', actor: 'lider', accion: 'confirma' },
    { codigo: 'CONFIRMADO', estado: 'Confirmado', actor: 'responsable', accion: 'inicia la corrección' },
    { codigo: 'EN_CORRECCION', estado: 'En corrección', actor: 'responsable', accion: 'envía su PR' },
    { codigo: 'EN_REVISION', estado: 'PR en revisión', actor: 'lider', accion: 'revisa el PR' },
    { codigo: 'APROBADO', estado: 'Aprobado', actor: null, accion: 'sale con su fase' },
    { codigo: 'CERRADO', estado: 'Cerrado', actor: null, accion: '' },
  ],
  // Bug Crítico o Alto en producción: no espera a una historia, lo prueba un Analista antes del parche.
  BUGS_HOTFIX: [
    { codigo: 'REPORTADO', estado: 'Reportado', actor: 'lider', accion: 'confirma' },
    { codigo: 'CONFIRMADO', estado: 'Confirmado', actor: 'responsable', accion: 'inicia la corrección' },
    { codigo: 'EN_CORRECCION', estado: 'En corrección', actor: 'responsable', accion: 'corrige' },
    { codigo: 'EN_QA', estado: 'En QA', actor: 'tester', accion: 'lo prueba' },
    { codigo: 'APROBADO', estado: 'Aprobado', actor: 'lider', accion: 'publica el parche' },
    { codigo: 'PUBLICADO', estado: 'Publicado', actor: 'tester', accion: 'verifica' },
    { codigo: 'CERRADO', estado: 'Cerrado', actor: null, accion: '' },
  ],
  MEJR: [
    { codigo: 'PROPUESTA', estado: 'Propuesta', actor: 'lider', accion: 'aprueba o rechaza' },
    { codigo: 'APROBADA', estado: 'Aprobada', actor: 'responsable', accion: 'inicia' },
    { codigo: 'EN_PROGRESO', estado: 'En progreso', actor: 'responsable', accion: 'envía su PR' },
    { codigo: 'EN_REVISION', estado: 'PR en revisión', actor: 'lider', accion: 'revisa el PR' },
    { codigo: 'APROBADA_QA', estado: 'PR aprobado', actor: null, accion: 'QA prueba la historia completa' },
    { codigo: 'INTEGRADA', estado: 'Integrada', actor: null, accion: 'se cierra al publicar la fase' },
    { codigo: 'CERRADA', estado: 'Cerrada', actor: null, accion: '' },
  ],
};

/**
 * La rama que se creará al presionar Iniciar. Es la misma regla del back (TicketParaMover.ramaSugerida):
 * "feature/actv-41-servicio-de-consulta-de-pedidos", "fix/bugs-31-..." para errores y
 * "hotfix/bugs-50-..." para los bugs de producción que salen desde main.
 */
export function ramaSugerida(tipo: string, numero: number, titulo: string, hotfix = false): string {
  const prefijo = hotfix ? 'hotfix/' : tipo === 'BUGS' ? 'fix/' : 'feature/';
  let slug = titulo
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '');
  if (slug.length > 40) slug = slug.slice(0, 40).replace(/-[^-]*$/, '');
  const codigo = `${tipo}-${String(numero).padStart(2, '0')}`.toLowerCase();
  return `${prefijo}${codigo}${slug ? '-' + slug : ''}`;
}

/** Un paso listo para pintarse en <app-flujo>. */
export interface PasoVisible {
  estado: string;
  /** Tipo de usuario que actúa: ["Analista", "Líder"]; vacío si avanza solo */
  roles: string[];
  /** Qué hace o qué pasa: "la completa", "pasa sola al aprobarse sus PR" */
  detalle: string;
  /** hecho: ya pasó; actual: ahí está; pendiente: falta */
  avance: 'hecho' | 'actual' | 'pendiente';
}

interface PasoSimple {
  codigo: string;
  estado: string;
  roles: string[];
  detalle: string;
}

/** Historia de usuario (PDF pág. 21). Descartada queda fuera del camino. */
export const FLUJO_HISTORIA: PasoSimple[] = [
  { codigo: 'BORRADOR', estado: 'Borrador', roles: ['Analista', 'Líder'], detalle: 'lo básico; inician su planeación' },
  { codigo: 'EN_PLANEACION', estado: 'En planeación', roles: ['Analista', 'Líder'], detalle: 'cualquier Analista la edita completa y la marca Lista' },
  { codigo: 'LISTA', estado: 'Lista', roles: ['Líder'], detalle: 'crea sus Actividades y las marca Listas' },
  { codigo: 'LISTA_DESARROLLO', estado: 'Lista para desarrollo', roles: ['Líder'], detalle: 'arranca al iniciar la fase' },
  { codigo: 'EN_DESARROLLO', estado: 'En desarrollo', roles: ['Desarrollador'],
    detalle: 'trabaja sus tickets; pasa a validación al aprobarse sus PR' },
  { codigo: 'EN_VALIDACION', estado: 'En validación', roles: ['Analista'], detalle: 'la prueba completa; acepta o devuelve' },
  { codigo: 'ACEPTADA', estado: 'Aceptada', roles: ['Líder'], detalle: 'integra' },
  { codigo: 'INTEGRADA', estado: 'Integrada', roles: [], detalle: 'se cierra al publicar' },
  { codigo: 'CERRADA', estado: 'Cerrada', roles: [], detalle: '' },
];

/** Fase (PDF pág. 20). Pausada se ve en En curso; Revertida, en Publicada; Cancelada queda fuera. */
export const FLUJO_FASE: PasoSimple[] = [
  { codigo: 'PLANEADA', estado: 'Planeada', roles: ['Líder'], detalle: 'la inicia' },
  { codigo: 'EN_CURSO', estado: 'En curso', roles: ['Líder'], detalle: 'marca sus criterios; se cierra al cumplirlos' },
  { codigo: 'LISTA_PARA_RELEASE', estado: 'Lista para release', roles: ['Líder'], detalle: 'sube a main' },
  { codigo: 'EN_PUBLICACION', estado: 'En publicación', roles: [], detalle: 'se despliega' },
  { codigo: 'PUBLICADA', estado: 'Publicada', roles: [], detalle: '' },
];

/**
 * Los pasos con el actual marcado.
 *
 * @param pasoDe estados fuera del camino que se ven en un paso: { PAUSADA: 'EN_CURSO' }
 */
export function pasosDe(flujo: PasoSimple[], estado: string, pasoDe: Record<string, string> = {}): PasoVisible[] {
  const actual = flujo.findIndex((p) => p.codigo === (pasoDe[estado] ?? estado));
  // El último paso no tiene a dónde ir: si ya llegó, está hecho.
  const final = actual === flujo.length - 1;
  return flujo.map((p, n) => ({
    estado: p.estado,
    roles: p.roles,
    detalle: p.detalle,
    avance: actual < 0 ? 'pendiente' : n < actual || (final && n === actual) ? 'hecho' : n === actual ? 'actual' : 'pendiente',
  }));
}
