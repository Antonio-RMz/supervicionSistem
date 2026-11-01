import { effect, Injectable, signal } from '@angular/core';

export type Tema = 'claro' | 'oscuro';

const CLAVE = 'tema';

/**
 * Claro u oscuro. Se guarda en localStorage; la primera vez sigue al sistema operativo.
 * index.html pone el mismo atributo antes de que arranque Angular, para que no parpadee.
 */
@Injectable({ providedIn: 'root' })
export class TemaService {
  readonly tema = signal<Tema>(temaInicial());

  constructor() {
    effect(() => {
      const tema = this.tema();
      document.documentElement.dataset['tema'] = tema;
      try {
        localStorage.setItem(CLAVE, tema);
      } catch {
        // Sin almacenamiento (modo privado): el tema dura lo que dure la pestaña.
      }
    });
  }

  alternar(): void {
    this.tema.update((t) => (t === 'oscuro' ? 'claro' : 'oscuro'));
  }
}

function temaInicial(): Tema {
  const enPagina = document.documentElement.dataset['tema'];
  if (enPagina === 'claro' || enPagina === 'oscuro') return enPagina;
  return matchMedia('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro';
}
