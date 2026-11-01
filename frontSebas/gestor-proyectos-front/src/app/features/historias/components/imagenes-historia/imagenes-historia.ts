import { Component, DestroyRef, effect, inject, input, output, signal, untracked } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { HistoriaDetalleService } from '../../data/historia-detalle.service';
import { ImagenDeHistoria } from '../../models/historia-detalle.models';

const TAMANO_MAXIMO = 5 * 1024 * 1024;
const MAXIMO = 10;

/**
 * Las imágenes de la historia (capturas, bocetos) junto al enlace de Figma. Cada archivo pasa por el token,
 * así que se pide como Blob y se muestra con una URL local que se libera al cambiar o salir.
 */
@Component({
  selector: 'app-imagenes-historia',
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
            <div class="pie">
              <span class="nombre" [title]="img.nombre">{{ img.nombre }}</span>
              <small>{{ img.tamano }}</small>
              @if (puedeAdjuntar()) {
                @if (quitando() === img.id) {
                  <button type="button" class="confirmar" (click)="quitar(img)" [disabled]="ocupado()">Quitar</button>
                  <button type="button" class="icono" (click)="quitando.set(null)" title="No quitar">
                    <i class="bi bi-x-lg"></i><span class="solo-lectores">No quitar</span>
                  </button>
                } @else {
                  <button type="button" class="icono" (click)="quitando.set(img.id)" [title]="'Quitar ' + img.nombre">
                    <i class="bi bi-trash"></i><span class="solo-lectores">Quitar {{ img.nombre }}</span>
                  </button>
                }
              }
            </div>
          </li>
        }
      </ul>
    }
    @if (puedeAdjuntar() && imagenes().length < maximo) {
      <label class="boton boton-secundario chico subir" [class.deshabilitado]="ocupado()">
        <i class="bi bi-image"></i> {{ ocupado() ? 'Subiendo…' : 'Agregar imagen' }}
        <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" [disabled]="ocupado()"
               (change)="subir($any($event.target))" />
      </label>
      <small class="pista">PNG, JPG, GIF o WebP de hasta 5 MB · máximo {{ maximo }}.</small>
    }
    @if (error()) {
      <small class="mensaje-campo">{{ error() }}</small>
    }
  `,
  styles: `
    :host { display: flex; flex-direction: column; align-items: flex-start; gap: 0.6rem; margin-top: 1rem; }
    .imagenes {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 0.75rem;
      width: 100%;
      margin: 0;
      padding: 0;
      list-style: none;
    }
    li { border: 1px solid var(--borde); border-radius: 10px; overflow: hidden; background: var(--fondo); }
    a, .cargando { display: block; aspect-ratio: 4 / 3; background: var(--fondo-pagina); }
    img { width: 100%; height: 100%; object-fit: cover; display: block; }
    .cargando { display: grid; place-items: center; color: var(--texto-suave); font-size: 1.4rem; }
    .pie { display: flex; align-items: center; gap: 0.4rem; padding: 0.45rem 0.6rem; }
    .nombre { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--tinta); font-size: 0.85rem; }
    small { color: var(--texto-suave); }
    .icono { border: 0; background: none; color: var(--texto-suave); cursor: pointer; padding: 0.15rem; }
    .confirmar {
      border: 1px solid var(--rojo-borde);
      border-radius: 6px;
      background: var(--rojo-suave);
      color: var(--rojo-texto);
      font-size: 0.8rem;
      padding: 0.1rem 0.5rem;
      cursor: pointer;
    }
    .subir { position: relative; cursor: pointer; }
    .subir input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
    .subir.deshabilitado { opacity: 0.6; pointer-events: none; }
    .mensaje-campo { color: var(--rojo-texto); }
  `,
})
export class ImagenesHistoria {
  private readonly service = inject(HistoriaDetalleService);

  readonly proyectoId = input.required<number>();
  readonly historiaId = input.required<number>();
  readonly imagenes = input.required<ImagenDeHistoria[]>();
  readonly puedeAdjuntar = input(false);

  /** Se subió o quitó una: el detalle se vuelve a leer. */
  readonly cambio = output<void>();

  protected readonly maximo = MAXIMO;
  protected readonly urls = signal<Record<number, string>>({});
  protected readonly ocupado = signal(false);
  protected readonly quitando = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    // Cada vez que cambia la lista se piden los archivos que faltan y se liberan los que ya no están.
    effect((onCleanup) => {
      const imagenes = this.imagenes();
      const proyectoId = this.proyectoId();
      const historiaId = this.historiaId();
      const actuales = untracked(() => this.urls());
      const ids = new Set(imagenes.map((i) => i.id));
      const quedan: Record<number, string> = {};
      for (const [id, url] of Object.entries(actuales)) {
        if (ids.has(Number(id))) quedan[Number(id)] = url;
        else URL.revokeObjectURL(url);
      }
      const faltan = imagenes.filter((i) => !quedan[i.id]);
      if (!faltan.length) {
        this.urls.set(quedan);
        return;
      }
      const sub = forkJoin(
        faltan.map((i) => this.service.imagen(proyectoId, historiaId, i.id).pipe(catchError(() => of(null)))),
      ).subscribe((blobs) => {
        const nuevas = { ...quedan };
        blobs.forEach((blob, n) => {
          if (blob) nuevas[faltan[n].id] = URL.createObjectURL(blob);
        });
        this.urls.set(nuevas);
      });
      onCleanup(() => sub.unsubscribe());
    });
    inject(DestroyRef).onDestroy(() => Object.values(this.urls()).forEach((url) => URL.revokeObjectURL(url)));
  }

  protected subir(campo: HTMLInputElement): void {
    const archivo = campo.files?.[0];
    campo.value = '';
    if (!archivo) return;
    if (archivo.size > TAMANO_MAXIMO) {
      this.error.set('La imagen pesa más de 5 MB.');
      return;
    }
    this.ocupado.set(true);
    this.error.set(null);
    this.service.subirImagen(this.proyectoId(), this.historiaId(), archivo).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.cambio.emit();
      },
      error: (e) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos subir la imagen. Intenta de nuevo.'));
      },
    });
  }

  protected quitar(img: ImagenDeHistoria): void {
    this.ocupado.set(true);
    this.error.set(null);
    this.service.quitarImagen(this.proyectoId(), this.historiaId(), img.id).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.quitando.set(null);
        this.cambio.emit();
      },
      error: (e) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos quitar la imagen.'));
      },
    });
  }
}
