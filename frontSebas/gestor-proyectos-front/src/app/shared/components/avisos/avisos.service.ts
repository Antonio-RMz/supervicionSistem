import { inject, Injectable, signal } from '@angular/core';

export interface Aviso {
  id: number;
  texto: string;
  error: boolean;
}

/** Cuántos caben a la vez; al llegar otro se va el más viejo. */
const MAXIMO = 4;

/** Los avisos flotantes del resultado de una acción (arriba a la derecha). Los muestra <app-avisos>. */
@Injectable({ providedIn: 'root' })
export class AvisosService {
  private siguiente = 1;
  readonly lista = signal<Aviso[]>([]);

  mostrar(texto: string, error = false): void {
    const aviso = { id: this.siguiente++, texto, error };
    this.lista.update((avisos) => [...avisos, aviso].slice(-MAXIMO));
  }

  cerrar(id: number): void {
    this.lista.update((avisos) => avisos.filter((a) => a.id !== id));
  }
}

/**
 * Para las páginas: `aviso.set({ texto, error })` muestra un aviso flotante. `set(null)` no hace nada: el aviso se
 * va solo o con su ✕. Se llama en un inicializador de campo (necesita el inyector).
 */
export function avisoFlotante(): { set(aviso: { texto: string; error: boolean } | null): void } {
  const avisos = inject(AvisosService);
  return {
    set: (aviso) => {
      if (aviso) avisos.mostrar(aviso.texto, aviso.error);
    },
  };
}
