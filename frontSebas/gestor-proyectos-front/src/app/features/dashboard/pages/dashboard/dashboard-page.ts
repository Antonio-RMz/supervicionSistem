import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { DashboardService } from '../../data/dashboard.service';
import { Dashboard } from '../../models/dashboard.models';

/**
 * Dashboard del equipo (PDF página 5). Se recarga al cambiar de proyecto en el selector.
 */
@Component({
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly service = inject(DashboardService);
  protected readonly proyectos = inject(ProyectoActualStore);
  protected readonly usuario = inject(AuthService).usuario;

  protected readonly datos = signal<Dashboard | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  protected readonly saludo = computed(() => {
    const d = this.datos();
    if (!d) return '';
    const n = d.pendientesMios;
    if (n === 0) return `No tienes nada pendiente en ${d.proyectoNombre}.`;
    return `Tienes ${n} ${n === 1 ? 'ticket esperando' : 'tickets esperando'} tu acción en ${d.proyectoNombre}.`;
  });

  constructor() {
    toObservable(computed(() => this.proyectos.actual()?.id ?? null))
      .pipe(
        filter((id): id is number => id !== null),
        tap(() => {
          this.cargando.set(true);
          this.error.set(false);
        }),
        switchMap((id) =>
          this.service.dashboard(id).pipe(
            catchError(() => {
              this.error.set(true);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((datos) => {
        this.datos.set(datos);
        this.cargando.set(false);
        this.proyectos.pendientesMios.set(datos?.pendientesMios ?? null);
      });
  }
}
