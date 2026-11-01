import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { FaseDetalleService } from '../../data/fase-detalle.service';
import { AccionDeLista } from '../../models/fase-detalle.models';

/**
 * Las fases en las que a esta persona le toca hacer algo (Iniciar, Subir a main, Desplegar...), solo para verlas:
 * la acción se hace en el detalle de la fase. Va en el Dashboard y en Mis tickets; si no hay ninguna, no pinta nada.
 */
@Component({
  selector: 'app-fases-pendientes',
  host: { '[class.sin-fases]': '!acciones().length' },
  imports: [RouterLink, EtiquetaEstado, TipoTicket],
  templateUrl: './fases-pendientes.html',
  styleUrl: './fases-pendientes.scss',
})
export class FasesPendientes {
  private readonly service = inject(FaseDetalleService);

  readonly proyectoId = input.required<number>();
  /** Con título se pinta como tarjeta aparte (Mis tickets); sin él, solo las filas (dentro de "Te toca a ti"). */
  readonly titulo = input('');

  /** Cuántas fases le tocan, para que la página sepa si su lista de al lado queda vacía. */
  readonly cargadas = output<number>();

  protected readonly acciones = signal<AccionDeLista[]>([]);

  constructor() {
    effect(() => {
      const id = this.proyectoId();
      untracked(() => this.cargar(id));
    });
  }

  private cargar(proyectoId: number): void {
    this.service.accionesDeLista(proyectoId).subscribe({
      next: (acciones) => {
        this.acciones.set(acciones);
        this.cargadas.emit(acciones.length);
      },
      error: () => {
        this.acciones.set([]);
        this.cargadas.emit(0);
      },
    });
  }
}
