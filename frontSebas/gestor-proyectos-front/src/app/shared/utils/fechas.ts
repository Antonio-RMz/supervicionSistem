// Formatos de fecha en español sin depender de registrar el locale de Angular.

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
const MESES_LARGOS = [
  'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
  'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre',
];

/** "2026-07-31" (LocalDate de Java) -> Date en hora local, sin corrimiento por zona horaria. */
export function aFecha(texto: string | null): Date | null {
  if (!texto) return null;
  const [anio, mes, dia] = texto.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** "2026-07-31T10:15:00" (LocalDateTime de Java) -> Date en hora local. */
export function aFechaHora(texto: string | null): Date | null {
  return texto ? new Date(texto) : null;
}

/** "hace 10 min", "hace 2 h", "ayer", "hace 3 días" */
export function haceCuanto(fecha: Date | null, ahora = new Date()): string {
  if (!fecha) return '';
  const minutos = Math.floor((ahora.getTime() - fecha.getTime()) / 60000);
  if (minutos < 1) return 'justo ahora';
  if (minutos < 60) return `hace ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `hace ${horas} h`;
  const dias = Math.floor(horas / 24);
  return dias === 1 ? 'ayer' : `hace ${dias} días`;
}

/** "31 jul 2026" */
export function fechaCorta(fecha: Date | null): string {
  return fecha ? `${fecha.getDate()} ${MESES[fecha.getMonth()]} ${fecha.getFullYear()}` : '';
}

/** "31 jul" */
export function diaYMes(fecha: Date | null): string {
  return fecha ? `${fecha.getDate()} ${MESES[fecha.getMonth()]}` : '';
}

/** "31 de julio de 2026" */
export function fechaLarga(fecha: Date | null): string {
  return fecha ? `${fecha.getDate()} de ${MESES_LARGOS[fecha.getMonth()]} de ${fecha.getFullYear()}` : '';
}

/** "Jul" */
export function mesCorto(fecha: Date | null): string {
  if (!fecha) return '';
  const mes = MESES[fecha.getMonth()];
  return mes.charAt(0).toUpperCase() + mes.slice(1);
}

/** "09:30" */
export function hora(fecha: Date | null): string {
  if (!fecha) return '';
  return `${String(fecha.getHours()).padStart(2, '0')}:${String(fecha.getMinutes()).padStart(2, '0')}`;
}

/** "hoy 10:41", "ayer 18:05", "15 oct 10:41" */
export function diaYHora(fecha: Date | null, ahora = new Date()): string {
  if (!fecha) return '';
  const inicio = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const dias = Math.round((inicio(ahora) - inicio(fecha)) / 86_400_000);
  const dia = dias === 0 ? 'hoy' : dias === 1 ? 'ayer' : diaYMes(fecha);
  return `${dia} ${hora(fecha)}`;
}
