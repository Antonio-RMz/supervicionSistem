import { Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { concatMap, from, Observable, of, toArray } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Modal } from '../../../../shared/components/modal/modal';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { MantenimientoService } from '../../data/mantenimiento.service';
import { PreparacionMantenimiento } from '../../models/mantenimiento.models';

/** Las cuatro severidades como las explica el PDF; Crítica y Alta van por hotfix. */
const SEVERIDADES = [
  { codigo: 'CRITICA', texto: 'Crítica', tono: 'rojo', hotfix: true, detalle: 'El sistema no funciona o se pierden datos' },
  { codigo: 'ALTA', texto: 'Alta', tono: 'naranja', hotfix: true, detalle: 'Una función importante no sirve y no hay alternativa' },
  { codigo: 'MEDIA', texto: 'Media', tono: 'ambar', hotfix: false, detalle: 'Falla, pero hay una forma de seguir trabajando' },
  { codigo: 'BAJA', texto: 'Baja', tono: 'gris', hotfix: false, detalle: 'Detalle visual o de texto' },
];

/** Igual que en el back (NuevaImagen e ImagenesTicketUseCase). */
const TIPOS_IMAGEN = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
const TAMANO_MAXIMO = 5 * 1024 * 1024;
const MAXIMO_IMAGENES = 5;

/** Una imagen elegida, con su URL local para la vista previa. */
interface ImagenElegida {
  archivo: File;
  url: string;
}

/**
 * Reportar falla en producción (PDF pág. 39). Cualquiera del equipo puede reportar: nace como bug Reportado en
 * Mantenimiento con una severidad provisional que el Líder confirma. Lleva descripción, imágenes o las dos:
 * las imágenes se suben en cuanto el bug existe.
 */
@Component({
  selector: 'app-reportar-falla-modal',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './reportar-falla-modal.html',
  styleUrl: './reportar-falla-modal.scss',
})
export class ReportarFallaModal {
  private readonly service = inject(MantenimientoService);

  readonly proyectoId = input.required<number>();
  readonly preparacion = input.required<PreparacionMantenimiento>();

  readonly cerrar = output<void>();
  /** "BUGS-53" del bug creado, si va por hotfix y si alguna imagen no se pudo subir */
  readonly reportado = output<{ codigo: string; hotfix: boolean; imagenesFallidas: boolean }>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    moduloId: [0, Validators.min(1)],
    severidad: ['', Validators.required],
    // Opcional: basta con la descripción, con una imagen o con las dos.
    descripcion: [''],
    historiaOrigenId: [0],
  });

  protected readonly severidades = SEVERIDADES;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly imagenes = signal<ImagenElegida[]>([]);
  protected readonly errorImagen = signal<string | null>(null);
  /** Se intentó enviar sin descripción ni imagen. */
  protected readonly sinEvidencia = signal(false);
  protected readonly maximoImagenes = MAXIMO_IMAGENES;

  constructor() {
    // Las URLs locales de la vista previa se liberan al cerrar.
    inject(DestroyRef).onDestroy(() => this.imagenes().forEach((i) => URL.revokeObjectURL(i.url)));
  }

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  /** "BUGS-53" */
  protected readonly codigo = computed(
    () => `BUGS-${String(this.preparacion().siguientes['BUGS'] ?? 1).padStart(2, '0')}`,
  );

  /** null mientras no se elige severidad. */
  protected readonly hotfix = computed(() => {
    const elegida = SEVERIDADES.find((s) => s.codigo === this.valor().severidad);
    return elegida ? elegida.hotfix : null;
  });

  /** Agrega las imágenes elegidas; las que no son imagen o pesan más de 5 MB se avisan y no entran. */
  protected elegirImagenes(entrada: HTMLInputElement): void {
    const archivos = Array.from(entrada.files ?? []);
    entrada.value = ''; // para poder elegir otra vez el mismo archivo
    this.errorImagen.set(null);
    const nuevas: ImagenElegida[] = [];
    for (const archivo of archivos) {
      if (this.imagenes().length + nuevas.length >= MAXIMO_IMAGENES) {
        this.errorImagen.set(`Máximo ${MAXIMO_IMAGENES} imágenes por reporte.`);
        break;
      }
      if (!TIPOS_IMAGEN.includes(archivo.type)) {
        this.errorImagen.set(`${archivo.name} no es PNG, JPG, GIF o WebP.`);
        continue;
      }
      if (archivo.size > TAMANO_MAXIMO) {
        this.errorImagen.set(`${archivo.name} pesa más de 5 MB.`);
        continue;
      }
      nuevas.push({ archivo, url: URL.createObjectURL(archivo) });
    }
    if (nuevas.length) {
      this.imagenes.update((actuales) => [...actuales, ...nuevas]);
      this.sinEvidencia.set(false);
    }
  }

  protected quitarImagen(imagen: ImagenElegida): void {
    URL.revokeObjectURL(imagen.url);
    this.imagenes.update((actuales) => actuales.filter((i) => i !== imagen));
  }

  protected invalido(campo: 'titulo' | 'moduloId' | 'severidad' | 'descripcion'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    const v = this.form.getRawValue();
    const sinEvidencia = !v.descripcion.trim() && !this.imagenes().length;
    this.sinEvidencia.set(sinEvidencia);
    if (this.form.invalid || sinEvidencia) {
      this.form.markAllAsTouched();
      return;
    }
    const codigo = this.codigo();
    const hotfix = this.hotfix() ?? false;
    this.enviando.set(true);
    this.error.set(null);
    this.service
      .reportarFalla(this.proyectoId(), {
        titulo: v.titulo.trim(),
        moduloId: v.moduloId,
        severidad: v.severidad,
        descripcion: v.descripcion.trim() || null,
        historiaOrigenId: v.historiaOrigenId || null,
      })
      // El bug ya existe: se suben sus imágenes una por una. Si alguna falla, el bug se queda reportado.
      .pipe(switchMap((ticketId) => this.subirImagenes(ticketId)))
      .subscribe({
        next: (imagenesFallidas) => this.reportado.emit({ codigo, hotfix, imagenesFallidas }),
        error: (e) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(e, 'No pudimos reportar la falla. Intenta de nuevo.'));
        },
      });
  }

  /** @return true si alguna imagen no se pudo subir */
  private subirImagenes(ticketId: number): Observable<boolean> {
    const imagenes = this.imagenes();
    if (!imagenes.length) return of(false);
    return from(imagenes).pipe(
      concatMap((i) =>
        this.service.subirImagen(this.proyectoId(), ticketId, i.archivo).pipe(
          map(() => true),
          catchError(() => of(false)),
        ),
      ),
      toArray(),
      map((subidas) => subidas.includes(false)),
    );
  }
}
