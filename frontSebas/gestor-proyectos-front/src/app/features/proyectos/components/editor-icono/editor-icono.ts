import { Component, computed, inject, input, signal } from '@angular/core';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { IconosProyectoService } from '../../data/iconos-proyecto.service';
import { IconoProyecto } from '../icono-proyecto/icono-proyecto';

const TIPOS = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
const TAMANO_MAXIMO = 1024 * 1024;

/** En Editar proyecto: subir, cambiar o quitar el ícono. Sin ícono se muestran las iniciales. */
@Component({
  selector: 'app-editor-icono',
  imports: [IconoProyecto],
  template: `
    <div class="editor">
      <app-icono-proyecto [proyectoId]="proyectoId()" [codigo]="codigo()" [tamano]="64" />
      <div class="lado">
        <strong>Ícono del proyecto</strong>
        <small>PNG, JPG, GIF o WebP de hasta 1 MB; mejor cuadrado. Sin ícono se usan las iniciales.</small>
        <div class="botones">
          <label class="boton boton-secundario chico" [class.deshabilitado]="ocupado()">
            <i class="bi bi-upload"></i> {{ tieneIcono() ? 'Cambiar' : 'Subir ícono' }}
            <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden
                   [disabled]="ocupado()" (change)="elegir($event)" />
          </label>
          @if (tieneIcono()) {
            <button type="button" class="boton boton-secundario chico" [disabled]="ocupado()" (click)="quitar()">
              <i class="bi bi-trash"></i> Quitar
            </button>
          }
        </div>
        @if (error()) {
          <small class="error" role="alert">{{ error() }}</small>
        }
      </div>
    </div>
  `,
  styles: `
    .editor { display: flex; align-items: flex-start; gap: 0.9rem; }
    .lado { display: flex; flex-direction: column; gap: 0.35rem; min-width: 0; }
    strong { color: var(--tinta); }
    small { color: var(--texto-suave); font-size: 0.82rem; }
    .botones { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.2rem; }
    label.boton { cursor: pointer; }
    .deshabilitado { opacity: 0.6; pointer-events: none; }
    .error { color: var(--rojo-texto); }
  `,
})
export class EditorIcono {
  private readonly iconos = inject(IconosProyectoService);

  readonly proyectoId = input.required<number>();
  readonly codigo = input.required<string>();

  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly tieneIcono = computed(() => !!this.iconos.icono(this.proyectoId())());

  protected elegir(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const archivo = campo.files?.[0];
    campo.value = '';
    if (!archivo) return;
    if (!TIPOS.includes(archivo.type)) {
      this.error.set('Solo se suben imágenes PNG, JPG, GIF o WebP.');
      return;
    }
    if (archivo.size > TAMANO_MAXIMO) {
      this.error.set('El ícono pesa más de 1 MB.');
      return;
    }
    this.ejecutar(this.iconos.subir(this.proyectoId(), archivo), 'No pudimos subir el ícono.');
  }

  protected quitar(): void {
    this.ejecutar(this.iconos.quitar(this.proyectoId()), 'No pudimos quitar el ícono.');
  }

  private ejecutar(peticion: ReturnType<IconosProyectoService['quitar']>, generico: string): void {
    this.ocupado.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: () => this.ocupado.set(false),
      error: (e) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(e, generico));
      },
    });
  }
}
