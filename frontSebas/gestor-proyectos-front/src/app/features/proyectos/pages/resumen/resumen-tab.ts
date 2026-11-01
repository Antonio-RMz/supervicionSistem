import { Component, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { diaYMes, haceCuanto } from '../../../../shared/utils/fechas';
import { DashboardService } from '../../../dashboard/data/dashboard.service';
import { Dashboard } from '../../../dashboard/models/dashboard.models';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';

/**
 * Pestaña Resumen del proyecto (PDF pág. 14). "Te toca a ti" y "Actividad reciente" salen del
 * Dashboard de este mismo proyecto; lo demás, del resumen que ya cargó el layout.
 * "Te toca a ti" solo lleva a cada ticket o historia: la acción se hace ahí.
 */
@Component({
  selector: 'app-resumen-tab',
  imports: [RouterLink, TipoTicket],
  templateUrl: './resumen-tab.html',
  styleUrl: './resumen-tab.scss',
})
export class ResumenTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly dashboardService = inject(DashboardService);

  protected readonly dashboard = signal<Dashboard | null>(null);

  protected readonly haceCuanto = haceCuanto;
  protected readonly diaYMes = diaYMes;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargarDashboard(id));
    });
  }

  private cargarDashboard(id: number): void {
    this.dashboardService.dashboard(id).subscribe({
      next: (d) => this.dashboard.set(d),
      error: () => this.dashboard.set(null),
    });
  }
}
