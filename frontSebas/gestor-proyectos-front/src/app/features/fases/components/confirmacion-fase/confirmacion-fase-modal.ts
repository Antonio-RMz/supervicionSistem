import { afterNextRender, Component, computed, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { Modal } from '../../../../shared/components/modal/modal';
import { AccionConConfirmacion, DatosAccion, DetalleFase } from '../../models/fase-detalle.models';

/**
 * Las ventanas de las acciones delicadas de la fase (PDF pág. 25): Pausar, Cancelar,
 * Subir a main (hay que escribir la versión) y Revertir (crea un BUGS). Todo queda en el historial.
 */
@Component({
  selector: 'app-confirmacion-fase-modal',
  imports: [Modal],
  templateUrl: './confirmacion-fase-modal.html',
  styleUrl: './confirmacion-fase-modal.scss',
})
export class ConfirmacionFaseModal {
  readonly accion = input.required<AccionConConfirmacion>();
  readonly fase = input.required<DetalleFase>();
  readonly enviando = input(false);

  readonly confirmar = output<DatosAccion>();
  readonly cancelar = output<void>();

  private readonly primerCampo = viewChild<ElementRef<HTMLElement>>('primero');

  protected readonly motivo = signal('');
  protected readonly reanudarEl = signal('');
  protected readonly version = signal('');
  protected readonly asignarBugA = signal<number | null>(null);
  protected readonly intentado = signal(false);

  /** Mañana, el primer día que se puede elegir para reanudar. */
  protected readonly manana = (() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  })();

  protected readonly titulo = computed(() => {
    const f = this.fase();
    switch (this.accion()) {
      case 'PAUSAR':
        return `Pausar Fase ${f.numero}`;
      case 'CANCELAR':
        return `Cancelar Fase ${f.numero}`;
      case 'SUBIR_A_MAIN':
        return `Subir a main y crear tag ${f.versionRelease}`;
      case 'REVERTIR':
        return f.versionAnterior ? `Revertir a ${f.versionAnterior}` : 'Revertir publicación';
    }
  });

  protected readonly subtitulo = computed(() => {
    const repos = this.fase().repositorios;
    switch (this.accion()) {
      case 'PAUSAR':
        return 'Nadie podrá iniciar tickets nuevos en esta fase mientras esté pausada.';
      case 'CANCELAR':
        return 'La fase y todo lo que no esté integrado se marcará como Cancelado.';
      case 'SUBIR_A_MAIN':
        return `Se hará merge de develop a main en ${repos} y se creará el tag.`;
      case 'REVERTIR':
        return `Producción volverá a la versión anterior en ${repos}.`;
    }
  });

  protected readonly versionCoincide = computed(() => this.version().trim() === this.fase().versionRelease);

  protected readonly faltaMotivo = computed(() => this.intentado() && !this.motivo().trim());
  protected readonly faltaAsignar = computed(() => this.intentado() && this.asignarBugA() === null);

  constructor() {
    // El cursor queda listo en el primer campo (autofocus no aplica a lo que Angular crea después).
    afterNextRender(() => this.primerCampo()?.nativeElement.focus());
  }

  protected enviar(): void {
    this.intentado.set(true);
    const motivo = this.motivo().trim();
    switch (this.accion()) {
      case 'PAUSAR':
        if (motivo) this.confirmar.emit({ motivo, reanudarEl: this.reanudarEl() || undefined });
        break;
      case 'CANCELAR':
        if (motivo) this.confirmar.emit({ motivo });
        break;
      case 'SUBIR_A_MAIN':
        if (this.versionCoincide()) this.confirmar.emit({ versionConfirmada: this.version().trim() });
        break;
      case 'REVERTIR': {
        const asignarBugA = this.asignarBugA();
        if (motivo && asignarBugA !== null) this.confirmar.emit({ motivo, asignarBugA });
        break;
      }
    }
  }

  protected elegirResponsable(valor: string): void {
    this.asignarBugA.set(valor ? Number(valor) : null);
  }
}
