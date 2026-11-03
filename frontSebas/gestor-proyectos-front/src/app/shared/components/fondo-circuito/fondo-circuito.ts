import { Component } from '@angular/core';

/** Líneas de circuito decorativas para fondos oscuros (logins). Se acomoda detrás del contenido. */
@Component({
  selector: 'app-fondo-circuito',
  template: `
    <svg viewBox="0 0 1000 625" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <g fill="none" stroke="currentColor" stroke-width="1.2">
        <path d="M0 82 H120 l30 30 H250" /><circle cx="250" cy="111" r="3.5" />
        <path d="M326 0 V40 l28 28 H416" /><circle cx="416" cy="69" r="3.5" />
        <path d="M451 0 V76" /><circle cx="451" cy="76" r="3.5" />
        <path d="M541 0 V27 l28 28 V104" /><circle cx="569" cy="104" r="3.5" />
        <path d="M0 207 H62 l28 -27 H180" /><circle cx="180" cy="180" r="3.5" />
        <path d="M0 326 H41" /><circle cx="41" cy="326" r="3.5" />
        <path d="M0 444 H97 l28 28 H208" /><circle cx="208" cy="472" r="3.5" />
        <path d="M160 625 V548 l28 -28 H291" /><circle cx="291" cy="520" r="3.5" />
        <path d="M361 625 V573 l28 -28 H472" /><circle cx="472" cy="548" r="3.5" />
        <path d="M1000 82 H910 l-28 27 H826" /><circle cx="826" cy="110" r="3.5" />
        <path d="M1000 207 H958" /><circle cx="958" cy="207" r="3.5" />
        <path d="M1000 330 H930 l-28 28 H850" /><circle cx="850" cy="358" r="3.5" />
        <path d="M1000 527 H923 l-28 -28 H820" /><circle cx="820" cy="499" r="3.5" />
        <path d="M611 625 V555 l28 -28 H680" /><circle cx="680" cy="527" r="3.5" />
        <path d="M868 602 H1000" /><circle cx="868" cy="602" r="3.5" />
        <path d="M700 0 V50 l28 28 H780" /><circle cx="780" cy="78" r="3.5" />
      </g>
    </svg>
  `,
  styles: `
    :host {
      position: absolute;
      inset: 0;
      color: rgba(31, 181, 122, 0.28);
      pointer-events: none;
    }
    svg { width: 100%; height: 100%; }
  `,
})
export class FondoCircuito {}
