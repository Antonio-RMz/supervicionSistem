import { Component, input } from '@angular/core';
import { Etiqueta } from '../../models/etiqueta.model';

/** Pastilla de estado: "En curso", "Entregada", "En pruebas"... */
@Component({
  selector: 'app-etiqueta-estado',
  template: `<span class="etiqueta tono-{{ etiqueta().tono }}">{{ etiqueta().texto }}</span>`,
  styles: `
    .etiqueta {
      display: inline-block;
      padding: 0.2rem 0.65rem;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 500;
      white-space: nowrap;
      background: var(--tono-fondo);
      color: var(--tono-texto);
    }
  `,
})
export class EtiquetaEstado {
  readonly etiqueta = input.required<Etiqueta>();
}
