import { Component, computed, input } from '@angular/core';
import { textoTipo, TONO_TIPO } from '../../utils/tickets';

/** Etiqueta del tipo: ACTV, BUGS, MEJR, HU (historia)... cada uno con su color. */
@Component({
  selector: 'app-tipo-ticket',
  template: `<span class="tipo tono-{{ tono() }}">{{ texto() }}</span>`,
  styles: `
    .tipo {
      display: inline-block;
      min-width: 3.4rem;
      padding: 0.15rem 0.45rem;
      border-radius: 6px;
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-size: 0.75rem;
      font-weight: 600;
      text-align: center;
      letter-spacing: 0.02em;
    }
  `,
})
export class TipoTicket {
  readonly tipo = input.required<string>();
  protected readonly tono = computed(() => TONO_TIPO[this.tipo()] ?? 'gris');
  protected readonly texto = computed(() => textoTipo(this.tipo()));
}
