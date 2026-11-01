import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Tono } from '../../../../shared/models/etiqueta.model';
import { diaYMes, fechaCorta, fechaLarga, mesCorto } from '../../../../shared/utils/fechas';
import { PortalClienteService } from '../../data/portal-cliente.service';
import { PortalClienteStore } from '../../data/portal-cliente.store';
import { AvanceProyecto, Fase } from '../../models/portal.models';

@Component({
  selector: 'app-avance-page',
  imports: [RouterLink, BarraAvance, ChipModulo, EtiquetaEstado],
  templateUrl: './avance-page.html',
  styleUrl: './avance-page.scss',
})
export class AvancePage {
  private readonly service = inject(PortalClienteService);
  private readonly store = inject(PortalClienteStore);

  /** Viene de la ruta /portal/proyectos/:id */
  readonly id = input.required<string>();

  protected readonly avance = signal<AvanceProyecto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly fechaCorta = fechaCorta;
  protected readonly fechaLarga = fechaLarga;

  constructor() {
    // Cada vez que cambia el :id (ej. desde el selector de proyecto) se vuelve a pedir el avance.
    toObservable(this.id)
      .pipe(
        tap((id) => {
          this.store.seleccionadoId.set(Number(id));
          this.cargando.set(true);
          this.error.set(null);
        }),
        switchMap((id) =>
          this.service.avance(Number(id)).pipe(
            catchError((e: HttpErrorResponse) => {
              this.error.set(e.status === 404 ? 'No encontramos ese proyecto.' : 'No pudimos cargar el avance.');
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((avance) => {
        this.avance.set(avance);
        this.cargando.set(false);
      });
  }

  protected esActual(fase: Fase, datos: AvanceProyecto): boolean {
    return fase.id === datos.faseActual?.id;
  }

  protected tonoFase(fase: Fase): Tono {
    if (fase.estado === 'ENTREGADA') return 'verde';
    if (fase.estado === 'EN_CURSO') return 'morado';
    return 'gris';
  }

  /** Texto bajo cada fase: "Feb" si ya se entregó, "Entrega 31 jul" si va en curso, "Sep" si viene después. */
  protected fechaFase(fase: Fase): string {
    if (fase.estado === 'ENTREGADA') return mesCorto(fase.fechaFin);
    if (fase.estado === 'PROXIMA') return mesCorto(fase.fechaInicio);
    return `Entrega ${diaYMes(fase.fechaFin)}`;
  }

  /** "v1.3.0" -> "1.3.0" */
  protected versionSinV(version: string | null): string {
    return version?.replace(/^v/, '') ?? '';
  }
}
