/** Color de una etiqueta o barra. Cada tono está definido en styles.scss. */
export type Tono = 'morado' | 'verde' | 'naranja' | 'gris' | 'azul' | 'turquesa' | 'rojo' | 'violeta' | 'ambar';

/** Texto + color de un estado, ej. { texto: 'En curso', tono: 'morado' }. */
export interface Etiqueta {
  texto: string;
  tono: Tono;
}
