import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { catchError, filter, forkJoin, of, switchMap, tap } from 'rxjs';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { HistoriaDetalleService } from '../../../historias/data/historia-detalle.service';
import { AccionDeListaHistoria } from '../../../historias/models/historia-detalle.models';
import { HistoriaDeLista } from '../../../historias/models/listado-historias.models';
import { MotivoModal } from '../../../tickets/components/motivo-modal/motivo-modal';
import { avisoDeMovimiento, MovimientosService } from '../../../tickets/data/movimientos.service';
import { TableroService } from '../../data/tablero.service';
import { Columna, COLUMNAS, COLUMNAS_HISTORIA, MovimientoTarjeta, Tablero, Tarjeta } from '../../models/tablero.models';

interface Filtros {
  historia: string;
  tipo: string;
  responsable: string;
  soloMios: boolean;
}

const SIN_FILTROS: Filtros = { historia: '', tipo: '', responsable: '', soloMios: false };

/**
 * Tablero de una fase (PDF pág. 12). Por defecto muestra las historias, porque QA prueba la historia
 * completa; la vista de tickets sigue para el trabajo del día. Mover un ticket ejecuta la acción del flujo;
 * si no te toca, la tarjeta muestra un candado y quién puede moverla.
 */
@Component({
  selector: 'app-tablero-page',
  imports: [Avatar, TipoTicket, MotivoModal, RouterLink],
  templateUrl: './tablero-page.html',
  styleUrl: './tablero-page.scss',
})
export class TableroPage {
  private readonly service = inject(TableroService);
  private readonly movimientos = inject(MovimientosService);
  private readonly historiasService = inject(HistoriaDetalleService);
  private readonly router = inject(Router);
  protected readonly proyectos = inject(ProyectoActualStore);

  protected readonly tablero = signal<Tablero | null>(null);
  protected readonly vista = signal<'historias' | 'tickets'>('historias');
  private readonly historias = signal<HistoriaDeLista[]>([]);
  /** El botón de cada historia para quien la ve; las que no tienen, no vienen. */
  private readonly accionesHistoria = signal<Map<number, AccionDeListaHistoria>>(new Map());
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  /** La fase elegida vale solo para el proyecto en que se eligió; al cambiar de proyecto vuelve la fase en curso. */
  private readonly faseElegida = signal<{ proyectoId: number; faseId: number } | null>(null);
  private readonly recargas = signal(0);
  protected readonly filtros = signal<Filtros>(SIN_FILTROS);

  protected readonly arrastrando = signal<Tarjeta | null>(null);
  protected readonly sobre = signal<Columna | null>(null);
  protected readonly moviendo = signal<number | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  /** Un rechazo espera su motivo en el modal. */
  protected readonly pidiendoMotivo = signal<{ tarjeta: Tarjeta; movimiento: MovimientoTarjeta } | null>(null);

  protected readonly columnasDef = COLUMNAS;

  /** Las historias de la fase elegida, en la columna de su estado (las descartadas no salen). */
  protected readonly columnasHistoria = computed(() => {
    const faseId = this.tablero()?.faseId;
    const acciones = this.accionesHistoria();
    const soloMias = this.filtros().soloMios;
    const visibles = this.historias().filter(
      (h) => h.faseId === faseId && (!soloMias || acciones.has(h.id)),
    );
    return COLUMNAS_HISTORIA.map((c) => ({
      ...c,
      historias: visibles
        .filter((h) => c.estados.includes(h.estado))
        .map((h) => ({ ...h, accion: acciones.get(h.id) ?? null })),
    }));
  });

  private readonly peticion = computed(() => {
    const proyectoId = this.proyectos.actual()?.id ?? null;
    const elegida = this.faseElegida();
    return {
      proyectoId,
      faseId: elegida?.proyectoId === proyectoId ? elegida.faseId : null,
      recarga: this.recargas(),
    };
  });

  protected readonly faseActual = computed(() => {
    const t = this.tablero();
    return t?.fases.find((f) => f.id === t.faseId) ?? null;
  });

  protected readonly opciones = computed(() => {
    const tarjetas = this.tablero()?.tarjetas ?? [];
    const unicos = (valores: string[]) => [...new Set(valores)].sort();
    return {
      historias: unicos(tarjetas.flatMap((t) => (t.historia ? [t.historia] : []))),
      tipos: unicos(tarjetas.map((t) => t.tipo)),
      responsables: unicos(tarjetas.flatMap((t) => (t.responsable ? [t.responsable.nombre] : []))),
    };
  });

  protected readonly columnas = computed(() => {
    const f = this.filtros();
    const visibles = (this.tablero()?.tarjetas ?? []).filter(
      (t) =>
        (!f.historia || t.historia === f.historia) &&
        (!f.tipo || t.tipo === f.tipo) &&
        (!f.responsable || t.responsable?.nombre === f.responsable) &&
        (!f.soloMios || t.mia),
    );
    return COLUMNAS.map((c) => ({ ...c, tarjetas: visibles.filter((t) => t.columna === c.id) }));
  });

  constructor() {
    toObservable(this.peticion)
      .pipe(
        filter((p): p is { proyectoId: number; faseId: number | null; recarga: number } => p.proyectoId !== null),
        tap(() => {
          this.cargando.set(true);
          this.error.set(false);
        }),
        switchMap((p) =>
          forkJoin({
            tablero: this.service.tablero(p.proyectoId, p.faseId),
            historias: this.historiasService.listar(p.proyectoId).pipe(catchError(() => of(null))),
            acciones: this.historiasService.accionesDeLista(p.proyectoId).pipe(catchError(() => of([]))),
          }).pipe(
            catchError(() => {
              this.error.set(true);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((datos) => {
        this.tablero.set(datos?.tablero ?? null);
        this.historias.set(datos?.historias?.historias ?? []);
        this.accionesHistoria.set(new Map((datos?.acciones ?? []).map((a) => [a.historiaId, a])));
        this.cargando.set(false);
      });
  }

  protected elegirFase(faseId: string): void {
    const proyectoId = this.proyectos.actual()?.id;
    if (!proyectoId) return;
    this.faseElegida.set({ proyectoId, faseId: Number(faseId) });
    this.filtros.set(SIN_FILTROS);
  }

  protected filtrar<K extends keyof Filtros>(campo: K, valor: Filtros[K]): void {
    this.filtros.update((f) => ({ ...f, [campo]: valor }));
  }

  // ─── Arrastrar y soltar ───────────────────────────────────────────────────

  protected puedeArrastrar(tarjeta: Tarjeta): boolean {
    return this.movimientosA(tarjeta).length > 0 && this.moviendo() === null;
  }

  /** Movimientos que cambian la tarjeta de columna (los demás son botón dentro de la tarjeta). */
  protected movimientosA(tarjeta: Tarjeta): MovimientoTarjeta[] {
    return [tarjeta.avance, tarjeta.rechazo].filter(
      (m): m is MovimientoTarjeta => !!m && m.columnaDestino !== tarjeta.columna,
    );
  }

  /** Movimientos que no cambian de columna: Confirmar un bug, Aprobar una mejora... */
  protected enSuColumna(tarjeta: Tarjeta): MovimientoTarjeta[] {
    return [tarjeta.avance].filter((m): m is MovimientoTarjeta => !!m && m.columnaDestino === tarjeta.columna);
  }

  protected aceptaSoltar(columna: Columna): boolean {
    const tarjeta = this.arrastrando();
    return !!tarjeta && this.movimientosA(tarjeta).some((m) => m.columnaDestino === columna);
  }

  protected alEmpezar(evento: DragEvent, tarjeta: Tarjeta): void {
    this.arrastrando.set(tarjeta);
    evento.dataTransfer?.setData('text/plain', tarjeta.codigo);
    if (evento.dataTransfer) evento.dataTransfer.effectAllowed = 'move';
  }

  protected alPasar(evento: DragEvent, columna: Columna): void {
    if (!this.aceptaSoltar(columna)) return;
    evento.preventDefault(); // sin esto el navegador no permite soltar aquí
    this.sobre.set(columna);
  }

  protected alSoltar(evento: DragEvent, columna: Columna): void {
    evento.preventDefault();
    const tarjeta = this.arrastrando();
    this.alTerminar();
    const movimiento = tarjeta && this.movimientosA(tarjeta).find((m) => m.columnaDestino === columna);
    if (tarjeta && movimiento) this.ejecutar(tarjeta, movimiento);
  }

  protected alTerminar(): void {
    this.arrastrando.set(null);
    this.sobre.set(null);
  }

  // ─── Ejecutar la acción ───────────────────────────────────────────────────

  protected ejecutar(tarjeta: Tarjeta, movimiento: MovimientoTarjeta, motivo?: string): void {
    if (movimiento.tipo === 'RECHAZAR' && motivo === undefined) {
      this.pidiendoMotivo.set({ tarjeta, movimiento });
      return;
    }
    this.moviendo.set(tarjeta.id);
    this.aviso.set(null);
    this.movimientos
      .mover({ ticketId: tarjeta.id, tipo: movimiento.tipo, estadoEsperado: tarjeta.estado, motivo })
      .subscribe({
        next: (resultado) => {
          this.moviendo.set(null);
          this.pidiendoMotivo.set(null);
          this.aviso.set({ texto: avisoDeMovimiento(tarjeta.codigo, resultado), error: false });
          this.recargas.update((n) => n + 1);
        },
        error: (e) => {
          this.moviendo.set(null);
          this.pidiendoMotivo.set(null);
          this.aviso.set({ texto: mensajeDeError(e, `No pudimos mover ${tarjeta.codigo}. Intenta de nuevo.`), error: true });
          this.recargas.update((n) => n + 1);
        },
      });
  }

  /** El botón de la historia lleva a su detalle, que pide las confirmaciones (motivo, fase...). */
  protected abrirAccion(historiaId: number, accion: AccionDeListaHistoria): void {
    const proyectoId = this.proyectos.actual()?.id;
    if (proyectoId) {
      this.router.navigate(['/proyectos', proyectoId, 'historias', historiaId], { queryParams: { accion: accion.accion } });
    }
  }

  protected tituloColumna(columna: Columna): string {
    return COLUMNAS.find((c) => c.id === columna)?.titulo ?? '';
  }
}
