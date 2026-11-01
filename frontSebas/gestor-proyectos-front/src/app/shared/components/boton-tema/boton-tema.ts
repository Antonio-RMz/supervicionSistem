import { Component, computed, inject } from '@angular/core';
import { TemaService } from '../../../core/tema/tema.service';

/** Botón de la barra superior que cambia entre tema claro y oscuro. */
@Component({
  selector: 'app-boton-tema',
  template: `
    <button type="button" (click)="tema.alternar()" [title]="texto()" [attr.aria-pressed]="oscuro()">
      <i class="bi" [class.bi-moon-stars]="!oscuro()" [class.bi-sun]="oscuro()"></i>
      <span class="solo-lectores">{{ texto() }}</span>
    </button>
  `,
  styles: `
    button {
      display: grid;
      place-items: center;
      width: 38px;
      height: 38px;
      border: 0;
      border-radius: 10px;
      background: none;
      color: var(--texto-suave);
      font-size: 1.15rem;
      cursor: pointer;

      &:hover { background: var(--fondo); color: var(--tinta); }
    }
  `,
})
export class BotonTema {
  protected readonly tema = inject(TemaService);
  protected readonly oscuro = computed(() => this.tema.tema() === 'oscuro');
  protected readonly texto = computed(() => (this.oscuro() ? 'Usar tema claro' : 'Usar tema oscuro'));
}
