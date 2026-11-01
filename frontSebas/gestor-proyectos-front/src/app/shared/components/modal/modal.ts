import { Component, input, output } from '@angular/core';

/**
 * Ventana sobre la página: fondo oscuro, título, cuerpo con scroll y pie con botones.
 * El cuerpo va como contenido normal y los botones dentro de un elemento con el atributo `pie`:
 *
 *   <app-modal titulo="Nuevo cliente" (cerrar)="...">
 *     <form id="f">...</form>
 *     <div pie><button form="f" type="submit">Crear</button></div>
 *   </app-modal>
 */
@Component({
  selector: 'app-modal',
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
  template: `
    <div class="fondo" (click)="cerrar.emit()"></div>
    <section class="ventana" role="dialog" aria-modal="true" [attr.aria-label]="titulo()">
      <header>
        <div>
          <h2>{{ titulo() }}</h2>
          @if (subtitulo()) {
            <p>{{ subtitulo() }}</p>
          }
        </div>
        <button type="button" class="cerrar" (click)="cerrar.emit()" title="Cerrar">
          <i class="bi bi-x-lg"></i>
          <span class="solo-lectores">Cerrar</span>
        </button>
      </header>
      <div class="cuerpo"><ng-content /></div>
      <footer><ng-content select="[pie]" /></footer>
    </section>
  `,
  styles: `
    :host {
      position: fixed;
      inset: 0;
      z-index: 50;
      display: grid;
      place-items: center;
      padding: 1rem;
    }
    .fondo {
      position: absolute;
      inset: 0;
      background: rgba(15, 23, 42, 0.45);
    }
    .ventana {
      position: relative;
      display: flex;
      flex-direction: column;
      width: min(720px, 100%);
      max-height: calc(100vh - 2rem);
      border-radius: 18px;
      background: var(--blanco);
      box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
    }
    header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 1.5rem 2rem 1.1rem;
      border-bottom: 1px solid var(--borde);
    }
    h2 { font-size: 1.35rem; }
    header p {
      margin: 0.25rem 0 0;
      color: var(--texto-suave);
    }
    .cerrar {
      border: 0;
      background: none;
      color: var(--texto-suave);
      font-size: 1.15rem;
      cursor: pointer;
    }
    .cerrar:hover { color: var(--tinta); }
    .cuerpo {
      padding: 1.25rem 2rem 1.5rem;
      overflow-y: auto;
    }
    footer {
      padding: 1.1rem 2rem;
      border-top: 1px solid var(--borde);
      border-radius: 0 0 18px 18px;
      background: var(--fondo-pagina);
    }
    @media (max-width: 640px) {
      header, .cuerpo, footer { padding-inline: 1.25rem; }
    }
  `,
})
export class Modal {
  readonly titulo = input.required<string>();
  readonly subtitulo = input('');
  readonly cerrar = output<void>();
}
