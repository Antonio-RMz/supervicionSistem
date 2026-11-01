import { afterNextRender, Component, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { Modal } from '../../../../shared/components/modal/modal';

/** Pide el motivo antes de regresar un ticket ("Rechazar en QA", "Pedir cambios"). */
@Component({
  selector: 'app-motivo-modal',
  imports: [Modal],
  template: `
    <app-modal [titulo]="accion() + ' · ' + codigo()" subtitulo="Quien lo trabaja verá este motivo en el historial."
               (cerrar)="cancelar.emit()">
      <form id="form-motivo" class="formulario" (submit)="$event.preventDefault(); enviar()">
        <label class="campo">
          <span>¿Qué hay que corregir? <b>*</b></span>
          <textarea #texto rows="4" placeholder="Ej. el PDF sale en blanco cuando la factura tiene más de 20 partidas"
                    (input)="motivo.set(texto.value)" [class.con-error]="faltaMotivo()"></textarea>
          @if (faltaMotivo()) {
            <small class="mensaje-campo">Escribe el motivo.</small>
          }
        </label>
      </form>
      <div pie class="pie-modal">
        <div class="botones">
          <button type="button" class="boton boton-secundario" (click)="cancelar.emit()">Cancelar</button>
          <button type="submit" form="form-motivo" class="boton boton-primario" [disabled]="enviando()">
            {{ enviando() ? 'Enviando…' : accion() }}
          </button>
        </div>
      </div>
    </app-modal>
  `,
})
export class MotivoModal {
  readonly codigo = input.required<string>();
  readonly accion = input.required<string>();
  readonly enviando = input(false);

  readonly confirmar = output<string>();
  readonly cancelar = output<void>();

  private readonly texto = viewChild.required<ElementRef<HTMLTextAreaElement>>('texto');

  protected readonly motivo = signal('');
  protected readonly faltaMotivo = signal(false);

  constructor() {
    // El cursor queda listo en el campo al abrir (autofocus no aplica a lo que Angular crea después).
    afterNextRender(() => this.texto().nativeElement.focus());
  }

  protected enviar(): void {
    const motivo = this.motivo().trim();
    this.faltaMotivo.set(!motivo);
    if (motivo) this.confirmar.emit(motivo);
  }
}
