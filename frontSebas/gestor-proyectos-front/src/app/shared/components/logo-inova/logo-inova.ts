import { Component, input } from '@angular/core';

/**
 * Logo de Inova Byte Systems: los 4 cuadros (uno verde) + "Inova Byte SYSTEMS".
 * - sobreOscuro: letras claras para fondos oscuros (logins).
 * - soloIcono: solo los cuadros (menú contraído).
 * - tamano: alto del ícono en px; el texto escala con él.
 */
@Component({
  selector: 'app-logo-inova',
  template: `
    <span class="logo" [class.sobre-oscuro]="sobreOscuro()" [style.--alto.px]="tamano()">
      <svg class="icono" viewBox="0 0 24 24" aria-hidden="true">
        <rect x="1" y="1" width="10" height="10" rx="2.4" class="oscuro" />
        <rect x="13" y="1" width="10" height="10" rx="2.4" class="verde" />
        <rect x="1" y="13" width="10" height="10" rx="2.4" class="oscuro" />
        <rect x="13" y="13" width="10" height="10" rx="2.4" class="oscuro" />
      </svg>
      @if (!soloIcono()) {
        <span class="nombre" aria-hidden="true">
          <span class="inova">Inova</span><span class="byte">Byte</span>
          <span class="systems">SYSTEMS</span>
        </span>
      }
      <span class="solo-lectores">Inova Byte Systems</span>
    </span>
  `,
  styles: `
    :host { display: inline-flex; }
    .logo {
      --alto: 32px;
      --tinta-logo: var(--marino-texto);
      display: inline-flex;
      align-items: center;
      gap: calc(var(--alto) * 0.35);
      white-space: nowrap;
    }
    .logo.sobre-oscuro { --tinta-logo: #ffffff; }
    .icono { width: var(--alto); height: var(--alto); flex-shrink: 0; }
    .oscuro { fill: var(--tinta-logo); }
    .verde { fill: var(--azul); }
    .nombre {
      display: inline-flex;
      align-items: baseline;
      font-family: var(--fuente-titulos);
      font-size: calc(var(--alto) * 0.78);
      font-weight: 600;
      letter-spacing: -0.01em;
      line-height: 1;
    }
    .inova { color: var(--tinta-logo); margin-right: 0.12em; }
    .byte { color: var(--azul); }
    .systems {
      margin-left: 0.45em;
      color: var(--texto-suave);
      font-family: var(--fuente);
      font-size: 0.42em;
      font-weight: 500;
      letter-spacing: 0.14em;
    }
    .sobre-oscuro .systems { color: #aeb6bf; }
    .solo-lectores {
      position: absolute; width: 1px; height: 1px; overflow: hidden;
      clip: rect(0 0 0 0); white-space: nowrap;
    }
  `,
})
export class LogoInova {
  readonly sobreOscuro = input(false);
  readonly soloIcono = input(false);
  readonly tamano = input(32);
}
