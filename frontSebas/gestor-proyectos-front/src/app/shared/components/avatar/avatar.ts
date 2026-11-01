import { Component, input } from '@angular/core';
import { Tono } from '../../models/etiqueta.model';

/** Iniciales de una persona ("JS", círculo) o de una empresa ("GF", cuadro de color suave). */
@Component({
  selector: 'app-avatar',
  template: `<span class="avatar tono-{{ tono() }}" [class.grande]="grande()" [class.cuadro]="cuadro()" aria-hidden="true">{{ iniciales() }}</span>`,
  styles: `
    .avatar {
      display: grid;
      place-items: center;
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--tono-solido);
      color: var(--sobre-color);
      font-size: 0.85rem;
      font-weight: 600;
    }
    .avatar.cuadro {
      border-radius: 10px;
      background: var(--tono-fondo);
      color: var(--tono-texto);
    }
    .avatar.grande {
      width: 72px;
      height: 72px;
      font-size: 1.4rem;
    }
  `,
})
export class Avatar {
  readonly iniciales = input.required<string>();
  readonly tono = input<Tono>('morado');
  readonly grande = input(false);
  readonly cuadro = input(false);
}
