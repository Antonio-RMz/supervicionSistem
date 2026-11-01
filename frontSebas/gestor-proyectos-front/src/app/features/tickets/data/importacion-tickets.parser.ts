import { fechaIso, limpiarCercas } from '../../../shared/utils/json';
import { TicketDeHistoria } from '../../historias/models/historia-detalle.models';
import { CamposTickets, LecturaTickets, RepositorioDeHistoria, TicketImportado } from '../models/importacion-tickets.models';

/** Igual que en el back (ImportarTicketsRequest). */
const MAXIMO = 50;
const MAXIMO_SUBTAREAS = 30;
const LARGO_SUBTAREA = 300;

/**
 * Lee el JSON pegado con el formato del prompt de tickets. El repositorio se busca por tipo ("API") o por nombre;
 * si la historia tiene uno solo, puede faltar.
 */
export function leerJsonTickets(texto: string, repositorios: RepositorioDeHistoria[]): LecturaTickets {
  if (!texto.trim()) return { valido: false, error: 'Pega el JSON que te devolvió la IA.' };

  let datos: unknown;
  try {
    datos = JSON.parse(limpiarCercas(texto));
  } catch (e) {
    return { valido: false, error: `No es un JSON válido: ${(e as Error).message}` };
  }

  const lista = (datos as { tickets?: unknown } | null)?.tickets;
  if (!Array.isArray(lista)) return { valido: false, error: 'Falta la lista "tickets".' };
  if (!lista.length) return { valido: false, error: 'La lista "tickets" está vacía.' };
  if (lista.length > MAXIMO) return { valido: false, error: `Se pueden importar hasta ${MAXIMO} tickets a la vez.` };

  const tickets: TicketImportado[] = [];
  for (const [i, item] of lista.entries()) {
    const resultado = leerTicket(item, repositorios);
    if (typeof resultado === 'string') return { valido: false, error: `Ticket ${i + 1}: ${resultado}` };
    tickets.push(resultado);
  }
  return { valido: true, tickets };
}

/** El JSON de los tickets de la historia para copiar; usa los mismos nombres que importar. */
export function aJsonTickets(
  proyecto: string,
  historia: string,
  tickets: TicketDeHistoria[],
  campos: CamposTickets,
  hoy = new Date(),
): string {
  return JSON.stringify(
    {
      proyecto,
      historia,
      exportado: fechaIso(hoy),
      tickets: tickets.map((t) => {
        const salida: Record<string, unknown> = {};
        if (campos.ids) salida['id'] = t.codigo;
        if (campos.estado) salida['estado'] = t.estado.texto;
        salida['titulo'] = t.titulo;
        if (campos.repositorio && t.modulo) salida['repositorio'] = t.modulo.corto;
        if (campos.responsable && t.responsable) salida['responsable'] = t.responsable.nombre;
        return salida;
      }),
    },
    null,
    2,
  );
}

function leerTicket(item: unknown, repositorios: RepositorioDeHistoria[]): TicketImportado | string {
  if (!item || typeof item !== 'object' || Array.isArray(item)) return 'debe ser un objeto { … }.';
  const t = item as Record<string, unknown>;

  const titulo = texto(t['titulo']);
  if (!titulo) return 'falta "titulo".';
  if (titulo.length > 200) return '"titulo" pasa de 200 caracteres.';
  if (t['descripcion'] != null && typeof t['descripcion'] !== 'string') return '"descripcion" debe ser texto.';

  const nombres = repositorios.map((r) => r.corto).join(', ');
  const escrito = texto(t['repositorio']);
  if (!escrito && repositorios.length > 1) return `falta "repositorio" (${nombres}).`;
  let repositorio: string | null = null;
  if (escrito) {
    const buscado = escrito.toLowerCase();
    const encontrado = repositorios.find((r) => [r.corto, r.tipo, r.nombre].some((n) => n.toLowerCase() === buscado));
    if (!encontrado) return `el repositorio "${escrito}" no es de la historia (${nombres}).`;
    // Al back va su tipo ("MOVIL"), que no depende de acentos ni mayúsculas.
    repositorio = encontrado.tipo;
  }

  const subtareas = t['subtareas'] ?? [];
  if (!Array.isArray(subtareas) || subtareas.some((s) => typeof s !== 'string')) {
    return '"subtareas" debe ser una lista de textos.';
  }
  const limpias = subtareas.map((s: string) => s.trim()).filter(Boolean);
  if (limpias.length > MAXIMO_SUBTAREAS) return `trae más de ${MAXIMO_SUBTAREAS} subtareas.`;
  const larga = limpias.findIndex((s) => s.length > LARGO_SUBTAREA);
  if (larga >= 0) return `la subtarea ${larga + 1} pasa de ${LARGO_SUBTAREA} caracteres.`;

  return { titulo, descripcion: texto(t['descripcion']), repositorio, subtareas: limpias };
}

function texto(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor.trim() : null;
}
