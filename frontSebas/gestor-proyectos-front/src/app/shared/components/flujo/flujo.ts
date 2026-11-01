import { Component, input } from '@angular/core';
import { Etiqueta } from '../../models/etiqueta.model';
import { PasoVisible, TONO_ROL } from '../../utils/flujos';

/**
 * El flujo de un ticket, historia o fase en la columna derecha de su detalle: los pasos de inicio a fin,
 * los que ya pasaron, en cuál va y qué tipo de usuario actúa en cada uno. Las acciones van aparte, en la columna izquierda.
 */
@Component({
  selector: 'app-flujo',
  template: `
    <section class="tarjeta">
      <div class="cabecera">
        <h2>Flujo</h2>
        @if (fuera(); as f) {
          <span class="pastilla tono-{{ f.tono }}">{{ f.texto }}</span>
        }
      </div>
      <ol>
        @for (p of pasos(); track p.estado; let i = $index) {
          <li [class]="p.avance">
            <span class="circulo">
              @if (p.avance === 'hecho') {
                <i class="bi bi-check-lg"></i>
              } @else {
                {{ i + 1 }}
              }
            </span>
            <span class="texto">
              <span class="titulo">
                <strong>{{ p.estado }}</strong>
                @for (rol of p.roles; track rol) {
                  <span class="rol tono-{{ tonoRol[rol] ?? 'gris' }}">{{ rol }}</span>
                }
              </span>
              @if (p.detalle) {
                <small>{{ p.detalle }}</small>
              }
            </span>
          </li>
        }
      </ol>
    </section>
  `,
  styles: `
    section { padding: 1.25rem 1.5rem; }
    .cabecera {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.5rem;
    }
    h2 { margin: 0; font-size: 1.05rem; }
    .pastilla {
      padding: 0.2rem 0.65rem;
      border-radius: 999px;
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-size: 0.8rem;
      font-weight: 600;
    }
    ol {
      margin: 1rem 0 0;
      padding: 0;
      list-style: none;
    }
    li {
      position: relative;
      display: flex;
      gap: 0.75rem;
      padding-bottom: 1rem;
    }
    li:last-child { padding-bottom: 0; }
    /* La línea que baja de cada paso al siguiente */
    li:not(:last-child)::after {
      content: '';
      position: absolute;
      top: 28px;
      bottom: 2px;
      left: 12px;
      width: 2px;
      border-radius: 2px;
      background: var(--borde);
    }
    li.hecho:not(:last-child)::after { background: var(--verde); }
    .circulo {
      display: grid;
      flex: none;
      place-items: center;
      width: 26px;
      height: 26px;
      border: 2px solid var(--borde-fuerte);
      border-radius: 50%;
      background: var(--blanco);
      color: var(--texto-suave);
      font-size: 0.75rem;
      font-weight: 700;
    }
    .hecho .circulo { border-color: var(--verde); background: var(--verde); color: var(--sobre-color); }
    .actual .circulo {
      border-color: var(--morado);
      background: var(--morado);
      color: var(--sobre-color);
      box-shadow: 0 0 0 4px var(--morado-suave);
    }
    .texto { display: flex; flex-direction: column; padding-top: 0.15rem; }
    .titulo { display: flex; flex-wrap: wrap; align-items: center; gap: 0.35rem; }
    .rol {
      padding: 0.05rem 0.45rem;
      border-radius: 999px;
      background: var(--tono-fondo);
      color: var(--tono-texto);
      font-size: 0.72rem;
      font-weight: 600;
    }
    strong { color: var(--texto-suave); font-weight: 600; }
    .hecho strong { color: var(--tinta); font-weight: 500; }
    .actual strong { color: var(--morado); }
    small { color: var(--texto-suave); font-size: 0.82rem; }
  `,
})
export class Flujo {
  readonly pasos = input.required<PasoVisible[]>();
  /** Un estado fuera del camino normal: Bloqueada, Pausada, Descartada, Cancelada... */
  readonly fuera = input<Etiqueta | null>(null);

  protected readonly tonoRol = TONO_ROL;
}
