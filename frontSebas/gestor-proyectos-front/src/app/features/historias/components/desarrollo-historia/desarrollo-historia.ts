import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { HistoriaDetalleService } from '../../data/historia-detalle.service';
import { DetalleHistoria } from '../../models/historia-detalle.models';

/**
 * Pestaña Desarrollo de una historia con Desarrolladores (sin Actividades como ticket):
 * quiénes la desarrollan y si ya terminaron su parte, y "Lo que hice".
 */
@Component({
  selector: 'app-desarrollo-historia',
  imports: [Avatar],
  templateUrl: './desarrollo-historia.html',
  styleUrl: './desarrollo-historia.scss',
})
export class DesarrolloHistoria {
  private readonly service = inject(HistoriaDetalleService);

  readonly historia = input.required<DetalleHistoria>();
  /** Algo cambió: el detalle se recarga. */
  readonly cambio = output<void>();
  readonly error = output<string>();

  protected readonly desarrollo = computed(() => this.historia().desarrollo!);
  protected readonly enDesarrollo = computed(() => this.historia().estado === 'EN_DESARROLLO');
  protected readonly hechos = computed(() => this.desarrollo().trabajo.filter((t) => t.hecho).length);

  protected readonly nuevo = signal('');
  protected readonly repositorio = signal<number | null>(null);
  protected readonly enviando = signal<string | null>(null);

  protected marcarMiParte(terminado: boolean): void {
    const h = this.historia();
    this.enviar('parte', this.service.marcarMiParte(h.proyectoId, h.id, terminado), 'No pudimos marcar tu parte.');
  }

  protected agregar(): void {
    const texto = this.nuevo().trim();
    if (!texto) return;
    const h = this.historia();
    this.enviar('agregar', this.service.agregarTrabajo(h.proyectoId, h.id, texto, this.repositorio()),
      'No pudimos agregarlo.', () => this.nuevo.set(''));
  }

  protected marcar(trabajoId: number, hecho: boolean): void {
    const h = this.historia();
    this.enviar(`t-${trabajoId}`, this.service.marcarTrabajo(h.proyectoId, h.id, trabajoId, hecho), 'No pudimos marcarlo.');
  }

  protected quitar(trabajoId: number): void {
    const h = this.historia();
    this.enviar(`q-${trabajoId}`, this.service.quitarTrabajo(h.proyectoId, h.id, trabajoId), 'No pudimos quitarlo.');
  }

  private enviar(clave: string, peticion: Observable<void>, generico: string, alTerminar?: () => void): void {
    this.enviando.set(clave);
    peticion.subscribe({
      next: () => {
        this.enviando.set(null);
        alTerminar?.();
        this.cambio.emit();
      },
      error: (e) => {
        this.enviando.set(null);
        this.error.emit(mensajeDeError(e, generico));
        this.cambio.emit();
      },
    });
  }
}
