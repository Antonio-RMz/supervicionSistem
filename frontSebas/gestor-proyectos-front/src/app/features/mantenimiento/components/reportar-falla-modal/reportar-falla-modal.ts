import { Component, computed, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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

/**
 * Reportar falla en producción (PDF pág. 39). Cualquiera del equipo puede reportar: nace como bug Reportado en
 * Mantenimiento con una severidad provisional que el Líder confirma.
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
  /** "BUGS-53" del bug creado y si va por hotfix */
  readonly reportado = output<{ codigo: string; hotfix: boolean }>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    moduloId: [0, Validators.min(1)],
    severidad: ['', Validators.required],
    descripcion: ['', Validators.required],
    historiaOrigenId: [0],
  });

  protected readonly severidades = SEVERIDADES;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

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

  protected invalido(campo: 'titulo' | 'moduloId' | 'severidad' | 'descripcion'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const codigo = this.codigo();
    const hotfix = this.hotfix() ?? false;
    this.enviando.set(true);
    this.error.set(null);
    this.service
      .reportarFalla(this.proyectoId(), {
        titulo: v.titulo.trim(),
        moduloId: v.moduloId,
        severidad: v.severidad,
        descripcion: v.descripcion.trim(),
        historiaOrigenId: v.historiaOrigenId || null,
      })
      .subscribe({
        next: () => this.reportado.emit({ codigo, hotfix }),
        error: (e) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(e, 'No pudimos reportar la falla. Intenta de nuevo.'));
        },
      });
  }
}
