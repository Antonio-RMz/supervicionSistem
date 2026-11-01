import { Component, input } from '@angular/core';
import { Tono } from '../../models/etiqueta.model';

/** Chip con ícono: "API", "Web", "App móvil". Con `tono` toma el color del módulo (lado del equipo). */
@Component({
  selector: 'app-chip-modulo',
  template: `<span [class]="tono() ? 'chip con-tono tono-' + tono() : 'chip'"><i class="bi {{ icono() }}"></i>{{ texto() }}</span>`,
  styles: `
    .chip {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.15rem 0.5rem;
      border-radius: 6px;
      background: var(--fondo);
      color: var(--texto-suave);
      font-size: 0.78rem;
      white-space: nowrap;
    }
    .chip.con-tono {
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-weight: 500;
    }
  `,
})
export class ChipModulo {
  readonly texto = input.required<string>();
  readonly icono = input.required<string>();
  readonly tono = input<Tono | null>(null);
}
