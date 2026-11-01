import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Lugar reservado para las pantallas del portal que aún no se construyen (Solicitudes, Versiones). */
@Component({
  selector: 'app-proximamente-page',
  imports: [RouterLink],
  template: `
    <section class="tarjeta">
      <i class="bi bi-tools"></i>
      <h1>{{ titulo() }}</h1>
      <p>Esta sección está en construcción.</p>
      <a class="boton boton-secundario" routerLink="/portal/avance">Volver al avance</a>
    </section>
  `,
  styles: `
    .tarjeta {
      max-width: 520px;
      margin: 3rem auto;
      padding: 3rem 2rem;
      text-align: center;
    }
    i { font-size: 2rem; color: var(--morado); }
    h1 { margin-top: 0.75rem; font-size: 1.5rem; }
    p { color: var(--texto-suave); margin: 0.5rem 0 1.5rem; }
  `,
})
export class ProximamentePage {
  /** Viene de data: { titulo } en la ruta. */
  readonly titulo = input('');
}
