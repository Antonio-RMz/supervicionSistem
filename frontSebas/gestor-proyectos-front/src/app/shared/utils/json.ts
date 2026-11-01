/** La IA a veces envuelve el JSON en ```json ... ```. */
export function limpiarCercas(texto: string): string {
  return texto.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '');
}

/** Lo bonito: mismo contenido con sangría de 2. Si no es JSON, lo deja como está. */
export function formatear(texto: string): string {
  try {
    return JSON.stringify(JSON.parse(limpiarCercas(texto)), null, 2);
  } catch {
    return texto;
  }
}

/** "1\n2\n3…": uno por renglón, para el margen de un editor. */
export function numerosDeLinea(texto: string): string {
  return Array.from({ length: texto.split('\n').length }, (_, i) => i + 1).join('\n');
}

/** "2026-10-29", en la hora local. */
export function fechaIso(fecha: Date): string {
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${fecha.getFullYear()}-${dos(fecha.getMonth() + 1)}-${dos(fecha.getDate())}`;
}
