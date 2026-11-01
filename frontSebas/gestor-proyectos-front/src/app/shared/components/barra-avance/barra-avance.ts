import { Component, computed, input } from '@angular/core';
import { Tono } from '../../models/etiqueta.model';

/** Barra de progreso de 0 a 100. */
@Component({
  selector: 'app-barra-avance',
  template: `
    <div class="pista" [class.gruesa]="gruesa()" role="progressbar"
         [attr.aria-valuenow]="valor()" aria-valuemin="0" aria-valuemax="100">
      <div class="relleno tono-{{ tono() }}" [style.width.%]="ancho()"></div>
    </div>
  `,
  styles: `
    .pista {
      height: 6px;
      border-radius: 999px;
      background: var(--pista);
      overflow: hidden;
    }
    .pista.gruesa { height: 10px; }
    .relleno {
      height: 100%;
      border-radius: inherit;
      background: var(--tono-solido);
      transition: width 0.4s ease;
    }
  `,
})
export class BarraAvance {
  readonly valor = input.required<number>();
  readonly tono = input<Tono>('morado');
  readonly gruesa = input(false);

  /** Un mínimo visible para que 0-2% no se vea vacío. */
  protected readonly ancho = computed(() => Math.min(100, Math.max(this.valor(), 1)));
}
