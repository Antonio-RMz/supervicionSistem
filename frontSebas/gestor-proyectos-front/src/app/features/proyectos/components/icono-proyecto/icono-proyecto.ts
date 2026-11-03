import { Component, computed, inject, input } from '@angular/core';
import { Tono } from '../../../../shared/models/etiqueta.model';
import { IconosProyectoService } from '../../data/iconos-proyecto.service';

/** El ícono del proyecto; si no tiene (o mientras carga), sus iniciales en un cuadro de color suave. */
@Component({
  selector: 'app-icono-proyecto',
  template: `
    <span class="icono tono-{{ tono() }}" [style.--tamano.px]="tamano()" aria-hidden="true">
      @if (url(); as u) {
        <img [src]="u" alt="" />
      } @else {
        {{ codigo() }}
      }
    </span>
  `,
  styles: `
    :host { display: inline-flex; flex-shrink: 0; }
    .icono {
      --tamano: 40px;
      display: grid;
      place-items: center;
      width: var(--tamano);
      height: var(--tamano);
      overflow: hidden;
      border-radius: calc(var(--tamano) * 0.22);
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-size: calc(var(--tamano) * 0.32);
      font-weight: 600;
    }
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      background: var(--blanco);
    }
  `,
})
export class IconoProyecto {
  private readonly iconos = inject(IconosProyectoService);

  readonly proyectoId = input.required<number>();
  /** Las iniciales del proyecto ("SC") */
  readonly codigo = input.required<string>();
  readonly tono = input<Tono>('morado');
  readonly tamano = input(40);

  protected readonly url = computed(() => this.iconos.icono(this.proyectoId())());
}
