import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Detalle | Árbol, arriba a la derecha del detalle de una fase, historia o ticket.
 * La vista va en la URL (?vista=arbol) para poder recargarla o compartirla.
 */
@Component({
  selector: 'app-cambio-vista',
  imports: [RouterLink],
  template: `
    <div class="cambio-vista" role="group" aria-label="Vista">
      <a [routerLink]="[]" [queryParams]="{ vista: null }" queryParamsHandling="merge" [class.activa]="!arbol()">
        <i class="bi bi-list-ul"></i> Detalle
      </a>
      <a [routerLink]="[]" [queryParams]="{ vista: 'arbol' }" queryParamsHandling="merge" [class.activa]="arbol()">
        <i class="bi bi-diagram-3"></i> Árbol
      </a>
    </div>
  `,
  styles: `
    .cambio-vista {
      display: flex;
      padding: 0.25rem;
      border: 1px solid var(--borde);
      border-radius: 12px;
      background: var(--blanco);
    }
    a {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.45rem 0.9rem;
      border-radius: 9px;
      color: var(--texto-suave);
      font-weight: 600;
      text-decoration: none;
      white-space: nowrap;
    }
    a.activa {
      background: var(--morado);
      color: var(--sobre-color);
    }
  `,
})
export class CambioVista {
  readonly arbol = input(false);
}
