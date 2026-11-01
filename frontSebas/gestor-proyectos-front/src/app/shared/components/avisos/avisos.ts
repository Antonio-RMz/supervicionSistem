import { Component, inject } from '@angular/core';
import { AvisosService } from './avisos.service';

/**
 * Los avisos flotantes, apilados arriba a la derecha. Cada uno se va solo cuando termina su barra (5 s; 8 s si es un
 * error); con el mouse encima se detiene para alcanzar a leerlo.
 */
@Component({
  selector: 'app-avisos',
  template: `
    <div class="pila" aria-live="polite">
      @for (a of avisos.lista(); track a.id) {
        <div class="aviso-flotante" [class.error]="a.error" [attr.role]="a.error ? 'alert' : 'status'">
          <span class="icono"><i class="bi" [class.bi-check-lg]="!a.error" [class.bi-exclamation-lg]="a.error"></i></span>
          <div class="cuerpo">
            <strong>{{ a.error ? 'No se pudo' : 'Listo' }}</strong>
            <p>{{ a.texto }}</p>
          </div>
          <button type="button" class="cerrar" (click)="avisos.cerrar(a.id)" title="Cerrar aviso">
            <i class="bi bi-x-lg"></i><span class="solo-lectores">Cerrar aviso</span>
          </button>
          <span class="barra" (animationend)="avisos.cerrar(a.id)"></span>
        </div>
      }
    </div>
  `,
  styles: `
    .pila {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: 0.6rem;
      width: min(380px, calc(100vw - 2rem));
      pointer-events: none;
    }

    .aviso-flotante {
      --tono: var(--verde);
      --tono-suave: var(--verde-suave);
      position: relative;
      display: grid;
      grid-template-columns: auto minmax(0, 1fr) auto;
      align-items: start;
      gap: 0.75rem;
      padding: 0.85rem 0.9rem 1rem;
      overflow: hidden;
      border: 1px solid var(--borde);
      border-left: 4px solid var(--tono);
      border-radius: 12px;
      background: var(--blanco);
      box-shadow: 0 12px 32px rgb(0 0 0 / 0.22);
      pointer-events: auto;
      animation: entrar 0.25s ease-out;

      &.error {
        --tono: var(--rojo);
        --tono-suave: var(--rojo-suave);
      }

      &:hover .barra { animation-play-state: paused; }
    }

    .icono {
      display: grid;
      place-items: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--tono-suave);
      color: var(--tono);
      font-size: 1rem;
    }

    .cuerpo {
      strong {
        display: block;
        color: var(--tinta);
        font-size: 0.92rem;
      }

      p {
        margin: 0.15rem 0 0;
        color: var(--texto-suave);
        font-size: 0.88rem;
        line-height: 1.4;
      }
    }

    .cerrar {
      border: 0;
      background: none;
      color: var(--texto-suave);
      cursor: pointer;

      &:hover { color: var(--tinta); }
    }

    .barra {
      position: absolute;
      bottom: 0;
      left: 0;
      height: 3px;
      width: 100%;
      background: var(--tono);
      opacity: 0.6;
      transform-origin: left;
      animation: tiempo 5s linear forwards;

      .error & { animation-duration: 8s; }
    }

    @keyframes entrar {
      from { opacity: 0; transform: translateX(24px); }
      to { opacity: 1; transform: none; }
    }

    @keyframes tiempo {
      from { transform: scaleX(1); }
      to { transform: scaleX(0); }
    }

    @media (prefers-reduced-motion: reduce) {
      .aviso-flotante { animation: none; }
    }
  `,
})
export class Avisos {
  protected readonly avisos = inject(AvisosService);
}
