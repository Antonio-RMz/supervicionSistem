import { Component, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { Modal } from '../../../../shared/components/modal/modal';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { diaYMes } from '../../../../shared/utils/fechas';
import { FaseDetalleService } from '../../../fases/data/fase-detalle.service';
import { DetalleFase } from '../../../fases/models/fase-detalle.models';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';
import { ReleasesService } from '../../data/releases.service';
import { ReleasePublicado, ReleasesProyecto } from '../../models/releases.models';

/**
 * Pestaña Releases (PDF pág. 36): lo que espera entrar a develop, el próximo release de fase, el parche en
 * preparación y lo publicado. Integrar, publicar y revertir se hacen en sus propias vistas; aquí se reúnen.
 */
@Component({
  selector: 'app-releases-tab',
  imports: [RouterLink, BarraAvance, Modal, TipoTicket],
  templateUrl: './releases-tab.html',
  styleUrl: './releases-tab.scss',
})
export class ReleasesTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly service = inject(ReleasesService);
  private readonly fases = inject(FaseDetalleService);

  protected readonly releases = signal<ReleasesProyecto | null>(null);
  /** La fase del próximo release, con su avance y criterios */
  protected readonly proxima = signal<DetalleFase | null>(null);
  /** Si quien lo ve puede revertir la versión en producción (lo decide la fase: PDF pág. 25) */
  protected readonly puedeRevertir = signal(false);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El parche cuyas notas se están viendo */
  protected readonly notas = signal<ReleasePublicado | null>(null);

  protected readonly diaYMes = diaYMes;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargar(id));
    });
  }

  private cargar(id: number): void {
    this.service.releases(id).subscribe({
      next: (r) => {
        this.releases.set(r);
        this.cargando.set(false);
        if (r.proximaFaseId) {
          this.fases.detalle(id, r.proximaFaseId).subscribe({ next: (f) => this.proxima.set(f), error: () => this.proxima.set(null) });
        }
        const actual = r.publicados.find((p) => p.enProduccion && p.esFase && p.faseId);
        if (actual) {
          this.fases.detalle(id, actual.faseId!).subscribe({
            next: (f) => this.puedeRevertir.set(f.acciones.some((a) => a.accion === 'REVERTIR')),
            error: () => this.puedeRevertir.set(false),
          });
        }
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar los releases.'));
        this.cargando.set(false);
      },
    });
  }
}
