import { Component, input, model, signal } from '@angular/core';

/**
 * Lista de textos que se agregan y quitan (criterios de aceptación, criterios manuales de cierre...).
 * Se usa con dos vías: <app-lista-editable [(items)]="criterios" placeholder="Agregar criterio…" />
 */
@Component({
  selector: 'app-lista-editable',
  template: `
    <ul>
      @for (item of items(); track $index; let i = $index) {
        <li>
          <span class="texto">{{ item }}</span>
          @if (etiqueta()) {
            <span class="etiqueta">{{ etiqueta() }}</span>
          }
          <button type="button" class="quitar" (click)="quitar(i)" [title]="'Quitar: ' + item">
            <i class="bi bi-x-lg"></i><span class="solo-lectores">Quitar {{ item }}</span>
          </button>
        </li>
      }
    </ul>
    <div class="agregar">
      <input #campo [placeholder]="placeholder()" [value]="nuevo()" (input)="nuevo.set(campo.value)"
             (keydown.enter)="$event.preventDefault(); agregar()" [attr.aria-label]="placeholder()" maxlength="300" />
      <button type="button" class="boton boton-secundario" (click)="agregar()" [disabled]="!nuevo().trim()">
        <i class="bi bi-plus-lg"></i> Agregar
      </button>
    </div>
  `,
  styles: `
    :host { display: flex; flex-direction: column; gap: 0.6rem; }
    ul { display: flex; flex-direction: column; gap: 0.6rem; margin: 0; padding: 0; list-style: none; }
    li {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.7rem 0.9rem;
      border: 1px solid var(--borde);
      border-radius: 10px;
    }
    .texto { flex: 1; color: var(--tinta); }
    .etiqueta {
      padding: 0.1rem 0.55rem;
      border-radius: 999px;
      background: var(--naranja-suave);
      color: var(--naranja-texto);
      font-size: 0.8rem;
    }
    .quitar { border: 0; background: none; color: var(--texto-suave); cursor: pointer; }
    .quitar:hover { color: var(--rojo); }
    .agregar { display: flex; gap: 0.6rem; }
    .agregar input {
      flex: 1;
      min-width: 0;
      padding: 0.7rem 0.9rem;
      border: 1px solid var(--borde);
      border-radius: 10px;
      font: inherit;
      outline: 0;
    }
    .agregar input:focus { border-color: var(--morado); box-shadow: 0 0 0 3px var(--morado-suave); }
  `,
})
export class ListaEditable {
  readonly items = model<string[]>([]);
  readonly placeholder = input('Agregar…');
  /** Pastilla junto a cada elemento, ej. "Manual" */
  readonly etiqueta = input('');

  protected readonly nuevo = signal('');

  protected agregar(): void {
    const texto = this.nuevo().trim();
    if (!texto) return;
    this.items.update((lista) => [...lista, texto]);
    this.nuevo.set('');
  }

  protected quitar(indice: number): void {
    this.items.update((lista) => lista.filter((_, i) => i !== indice));
  }
}
