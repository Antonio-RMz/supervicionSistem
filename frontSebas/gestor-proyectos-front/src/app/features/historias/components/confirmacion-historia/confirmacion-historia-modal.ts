import { afterNextRender, Component, computed, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { Modal } from '../../../../shared/components/modal/modal';
import { AccionConConfirmacion, DatosAccionHistoria, DetalleHistoria } from '../../models/historia-detalle.models';

/**
 * Las ventanas de las acciones de la historia que piden confirmar o elegir algo (PDF pág. 33):
 * Iniciar, Devolver a desarrollo, Descartar y Mover a otra fase. Todo queda en el historial.
 */
@Component({
  selector: 'app-confirmacion-historia-modal',
  imports: [Modal],
  templateUrl: './confirmacion-historia-modal.html',
  styleUrl: './confirmacion-historia-modal.scss',
})
export class ConfirmacionHistoriaModal {
  readonly accion = input.required<AccionConConfirmacion>();
  readonly historia = input.required<DetalleHistoria>();
  readonly enviando = input(false);

  readonly confirmar = output<DatosAccionHistoria>();
  readonly cancelar = output<void>();

  private readonly primerCampo = viewChild<ElementRef<HTMLElement>>('primero');

  protected readonly motivo = signal('');
  /** Al devolver: reabrir un ticket existente o crear un bug dentro de la historia. */
  protected readonly modo = signal<'reabrir' | 'bug'>('reabrir');
  protected readonly ticketId = signal<number | null>(null);
  protected readonly faseDestinoId = signal<number | null>(null);
  protected readonly intentado = signal(false);

  protected readonly titulo = computed(() => {
    const h = this.historia();
    switch (this.accion()) {
      case 'DEVOLVER':
        return `Devolver ${h.codigo} a desarrollo`;
      case 'DESCARTAR':
        return `Descartar ${h.codigo}`;
      case 'MOVER_DE_FASE':
        return `Mover ${h.codigo} a otra fase`;
      case 'PEDIR_CAMBIOS':
        return `Pedir cambios en ${h.codigo}`;
    }
  });

  /** Con Desarrolladores no hay ticket que reabrir ni bug: regresa a ellos con el motivo. */
  protected readonly conDesarrolladores = computed(() => !!this.historia().desarrollo);

  protected readonly subtitulo = computed(() => {
    if (this.accion() === 'DEVOLVER' && !this.conDesarrolladores()) {
      return 'Elige qué falló para que regrese a la persona correcta.';
    }
    if (this.accion() === 'DEVOLVER' || this.accion() === 'PEDIR_CAMBIOS') {
      return 'Regresa a sus Desarrolladores con el motivo; cada uno vuelve a marcar su parte.';
    }
    return this.historia().titulo;
  });

  protected readonly sinTicketsReabribles = computed(() => !this.historia().ticketsReabribles.length);

  protected readonly faltaMotivo = computed(() => this.intentado() && !this.motivo().trim());
  protected readonly faltaTicket = computed(
    () => this.intentado() && this.modo() === 'reabrir' && this.ticketId() === null,
  );
  protected readonly faltaFase = computed(() => this.intentado() && this.faseDestinoId() === null);

  constructor() {
    // Sin tickets que reabrir, devolver solo puede crear un bug.
    afterNextRender(() => {
      if (this.sinTicketsReabribles()) this.modo.set('bug');
      this.primerCampo()?.nativeElement.focus();
    });
  }

  protected enviar(): void {
    this.intentado.set(true);
    const motivo = this.motivo().trim();
    switch (this.accion()) {
      case 'DESCARTAR':
        if (motivo) this.confirmar.emit({ motivo });
        break;
      case 'PEDIR_CAMBIOS':
        if (motivo) this.confirmar.emit({ motivo });
        break;
      case 'DEVOLVER': {
        const ticketId = this.ticketId();
        if (!motivo) break;
        if (this.conDesarrolladores()) {
          this.confirmar.emit({ motivo });
          break;
        }
        if (this.modo() === 'bug') this.confirmar.emit({ motivo, crearBug: true });
        else if (ticketId !== null) this.confirmar.emit({ motivo, ticketId });
        break;
      }
      case 'MOVER_DE_FASE': {
        const faseDestinoId = this.faseDestinoId();
        if (faseDestinoId !== null) this.confirmar.emit({ faseDestinoId });
        break;
      }
    }
  }

  protected elegirTicket(valor: string): void {
    this.ticketId.set(valor ? Number(valor) : null);
  }

  protected elegirFase(valor: string): void {
    this.faseDestinoId.set(valor ? Number(valor) : null);
  }
}
