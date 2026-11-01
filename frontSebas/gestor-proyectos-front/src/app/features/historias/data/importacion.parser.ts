import { fechaIso, limpiarCercas } from '../../../shared/utils/json';
import { textoEstado } from '../../../shared/utils/tickets';
import { CamposJson, HistoriaCompleta, HistoriaImportada, HistoriasDeFase, LecturaJson } from '../models/importacion.models';

/** Igual que en el back (HistoriaImportada y NuevaHistoria). */
const MAXIMO = 50;
const PRIORIDADES = ['ALTA', 'MEDIA', 'BAJA'];
const PUNTOS = [1, 2, 3, 5, 8, 13];

/** Lee el JSON pegado con el formato v1 del prompt. Si algo no cuadra dice qué historia y qué campo. */
export function leerJson(texto: string): LecturaJson {
  if (!texto.trim()) return { valido: false, error: 'Pega el JSON que te devolvió la IA.' };

  let datos: unknown;
  try {
    datos = JSON.parse(limpiarCercas(texto));
  } catch (e) {
    return { valido: false, error: `No es un JSON válido: ${(e as Error).message}` };
  }

  const lista = (datos as { historias?: unknown } | null)?.historias;
  if (!Array.isArray(lista)) return { valido: false, error: 'Falta la lista "historias".' };
  if (!lista.length) return { valido: false, error: 'La lista "historias" está vacía.' };
  if (lista.length > MAXIMO) return { valido: false, error: `Se pueden importar hasta ${MAXIMO} historias a la vez.` };

  const historias: HistoriaImportada[] = [];
  for (const [i, item] of lista.entries()) {
    const resultado = leerHistoria(item);
    if (typeof resultado === 'string') return { valido: false, error: `Historia ${i + 1}: ${resultado}` };
    historias.push(resultado);
  }
  const incompletas = historias.filter((h) => !h.quiero || !h.para || !h.criterios.length).length;
  return { valido: true, historias, incompletas };
}

function leerHistoria(item: unknown): HistoriaImportada | string {
  if (!item || typeof item !== 'object' || Array.isArray(item)) return 'debe ser un objeto { … }.';
  const h = item as Record<string, unknown>;

  const titulo = texto(h['titulo']);
  if (!titulo) return 'falta "titulo".';
  if (titulo.length > 150) return '"titulo" pasa de 150 caracteres.';
  const como = texto(h['como']);
  if (!como) return 'falta "como".';

  for (const campo of ['como', 'quiero', 'para']) {
    if (h[campo] != null && typeof h[campo] !== 'string') return `"${campo}" debe ser texto.`;
  }

  let prioridad: string | null = null;
  if (h['prioridad'] != null) {
    prioridad = String(h['prioridad']).trim().toUpperCase();
    if (!PRIORIDADES.includes(prioridad)) return '"prioridad" debe ser Alta, Media o Baja.';
  }

  let puntos: number | null = null;
  if (h['puntos'] != null) {
    puntos = Number(h['puntos']);
    if (!PUNTOS.includes(puntos)) return '"puntos" debe ser 1, 2, 3, 5, 8 o 13.';
  }

  const criterios = h['criterios'] ?? [];
  if (!Array.isArray(criterios) || criterios.some((c) => typeof c !== 'string')) {
    return '"criterios" debe ser una lista de textos.';
  }

  return {
    titulo,
    como,
    quiero: texto(h['quiero']),
    para: texto(h['para']),
    prioridad,
    puntos,
    criterios: criterios.map((c: string) => c.trim()).filter(Boolean),
  };
}

function texto(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor.trim() : null;
}

/**
 * El JSON de las historias para copiar. Usa los mismos nombres que el formato v1 de importar, así que se puede
 * pegar de vuelta en otra fase (los campos extra como id o estado se ignoran).
 */
export function aJson(datos: HistoriasDeFase, historias: HistoriaCompleta[], campos: CamposJson, hoy = new Date()): string {
  return JSON.stringify(
    {
      proyecto: datos.proyectoNombre,
      fase: datos.faseTitulo,
      exportado: fechaIso(hoy),
      historias: historias.map((h) => {
        const salida: Record<string, unknown> = {};
        if (campos.ids) salida['id'] = h.codigo;
        if (campos.estado) salida['estado'] = textoEstado(h.estado);
        salida['titulo'] = h.titulo;
        if (h.como) salida['como'] = h.como;
        if (h.quiero) salida['quiero'] = h.quiero;
        if (h.para) salida['para'] = h.para;
        if (campos.prioridad) {
          salida['prioridad'] = h.prioridad.charAt(0) + h.prioridad.slice(1).toLowerCase();
          if (h.puntos) salida['puntos'] = h.puntos;
        }
        if (campos.criterios && h.criterios.length) salida['criterios'] = h.criterios;
        if (campos.figma && h.figmaUrl) salida['figma'] = h.figmaUrl;
        return salida;
      }),
    },
    null,
    2,
  );
}
