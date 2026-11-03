import { Component, DestroyRef, effect, inject, input, signal, untracked } from '@angular/core';
import { MantenimientoService } from '../../../mantenimiento/data/mantenimiento.service';
import { ImagenDeTicket } from '../../../mantenimiento/models/mantenimiento.models';

/**
 * Las imágenes del ticket (la captura con la que se reportó un bug). Solo se muestra si tiene alguna.
 * Cada archivo pasa por el token: se pide como Blob y se muestra con una URL local que se libera al salir.
 */
@Component({
  selector: 'app-imagenes-ticket',
  template: `
    @if (imagenes().length) {
      <ul class="imagenes">
        @for (img of imagenes(); track img.id) {
          <li>
            @if (urls()[img.id]; as url) {
              <a [href]="url" target="_blank" rel="noopener" [title]="'Abrir ' + img.nombre">
                <img [src]="url" [alt]="img.nombre" />
              </a>
            } @else {
              <div class="cargando"><i class="bi bi-image"></i></div>
            }
          </li>
        }
      </ul>
    }
  `,
  styles: `
    .imagenes {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 0.75rem;
      margin: 1rem 0 0;
      padding: 0;
      list-style: none;
    }
    li { border: 1px solid var(--borde); border-radius: 10px; overflow: hidden; background: var(--fondo); }
    a, .cargando { display: block; aspect-ratio: 4 / 3; background: var(--fondo-pagina); }
    img { display: block; width: 100%; height: 100%; object-fit: cover; }
    .cargando { display: grid; place-items: center; color: var(--texto-suave); font-size: 1.4rem; }
  `,
})
export class ImagenesTicket {
  private readonly service = inject(MantenimientoService);

  readonly proyectoId = input.required<number>();
  readonly ticketId = input.required<number>();

  protected readonly imagenes = signal<ImagenDeTicket[]>([]);
  protected readonly urls = signal<Record<number, string>>({});

  constructor() {
    effect((onCleanup) => {
      const proyectoId = this.proyectoId();
      const ticketId = this.ticketId();
      const pedidas = [
        this.service.imagenes(proyectoId, ticketId).subscribe({
          next: (lista) => {
            this.imagenes.set(lista);
            for (const img of lista) {
              pedidas.push(
                this.service.imagen(proyectoId, ticketId, img.id).subscribe({
                  next: (blob) => this.urls.update((u) => ({ ...u, [img.id]: URL.createObjectURL(blob) })),
                  error: () => undefined, // se queda el ícono
                }),
              );
            }
          },
          error: () => this.imagenes.set([]),
        }),
      ];
      onCleanup(() => {
        pedidas.forEach((s) => s.unsubscribe());
        this.liberar();
      });
    });
    inject(DestroyRef).onDestroy(() => this.liberar());
  }

  private liberar(): void {
    untracked(() => {
      Object.values(this.urls()).forEach((url) => URL.revokeObjectURL(url));
      this.urls.set({});
    });
  }
}
