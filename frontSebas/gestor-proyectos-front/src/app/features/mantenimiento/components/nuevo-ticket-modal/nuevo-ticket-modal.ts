import { Component, computed, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../../shared/components/modal/modal';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ramaSugerida } from '../../../../shared/utils/flujos';
import { MantenimientoService } from '../../data/mantenimiento.service';
import { PreparacionMantenimiento, TIPOS_MANTENIMIENTO } from '../../models/mantenimiento.models';

/**
 * Nuevo ticket de mantenimiento (PDF pág. 38): trabajo fuera de las fases que sale con la siguiente fase,
 * desde develop. Lo crean el Líder o un Desarrollador; lo urgente va por Reportar falla.
 */
@Component({
  selector: 'app-nuevo-ticket-modal',
  imports: [ReactiveFormsModule, Modal, TipoTicket],
  templateUrl: './nuevo-ticket-modal.html',
  styleUrl: './nuevo-ticket-modal.scss',
})
export class NuevoTicketModal {
  private readonly service = inject(MantenimientoService);

  readonly proyectoId = input.required<number>();
  readonly preparacion = input.required<PreparacionMantenimiento>();

  readonly cerrar = output<void>();
  /** "MEJR-14" del ticket creado */
  readonly creado = output<string>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    tipo: ['MEJR'],
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    descripcion: [''],
    moduloId: [0, Validators.min(1)],
    prioridad: ['MEDIA'],
    severidad: ['MEDIA'],
    responsableId: [0],
  });

  protected readonly tipos = TIPOS_MANTENIMIENTO;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  /** "MEJR-14" */
  protected readonly codigo = computed(() => {
    const tipo = this.valor().tipo ?? 'MEJR';
    return `${tipo}-${String(this.preparacion().siguientes[tipo] ?? 1).padStart(2, '0')}`;
  });

  /** "feature/mejr-14-…" o, con título, la rama completa que se creará al iniciar. */
  protected readonly rama = computed(() => {
    const tipo = this.valor().tipo ?? 'MEJR';
    const rama = ramaSugerida(tipo, this.preparacion().siguientes[tipo] ?? 1, this.valor().titulo ?? '');
    return this.valor().titulo?.trim() ? rama : `${rama}-…`;
  });

  protected invalido(campo: 'titulo' | 'moduloId' | 'responsableId'): boolean {
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
    this.enviando.set(true);
    this.error.set(null);
    this.service
      .crearTicket(this.proyectoId(), {
        tipo: v.tipo,
        titulo: v.titulo.trim(),
        descripcion: v.descripcion.trim(),
        moduloId: v.moduloId,
        prioridad: v.prioridad,
        severidad: v.tipo === 'BUGS' ? v.severidad : null,
        responsableId: v.responsableId || null,
      })
      .subscribe({
        next: () => this.creado.emit(codigo),
        error: (e) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(e, 'No pudimos crear el ticket. Intenta de nuevo.'));
        },
      });
  }
}
