import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { ORDEN_ESTADOS, textoEstado } from '../../../../shared/utils/tickets';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { MisTicketsService } from '../../data/mis-tickets.service';
import { ColumnaTablero, Filtros, SIN_FILTROS, TicketMio } from '../../models/mis-tickets.models';

type Vista = 'lista' | 'tablero';

const CLAVE_VISTA = 'misTickets.vista';
const ORDEN_PRIORIDAD = ['ALTA', 'MEDIA', 'BAJA'];

/**
 * Mis tickets (PDF pág. 11) del proyecto elegido en el selector de arriba: solo lo que espera una acción mía.
 * Es solo una vista: cada acción se hace al entrar al ticket o la historia.
 */
@Component({
  selector: 'app-mis-tickets-page',
  imports: [RouterLink, Avatar, EtiquetaEstado, TipoTicket],
  templateUrl: './mis-tickets-page.html',
  styleUrl: './mis-tickets-page.scss',
})
export class MisTicketsPage {
  private readonly service = inject(MisTicketsService);
  protected readonly proyectos = inject(ProyectoActualStore);

  protected readonly tickets = signal<TicketMio[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  protected readonly filtros = signal<Filtros>(SIN_FILTROS);
  protected readonly vista = signal<Vista>(leerVista());

  /** Los que esperan una acción mía, antes de los filtros: de aquí salen las opciones de cada filtro. */
  protected readonly meTocan = computed(() => this.tickets().filter((t) => t.meToca));

  protected readonly opciones = computed(() => {
    const lista = this.meTocan();
    const unicos = (valores: string[]) => [...new Set(valores)];
    return {
      tipos: unicos(lista.map((t) => t.tipo)).sort(),
      estados: unicos(lista.map((t) => t.estado))
        .sort((a, b) => ORDEN_ESTADOS.indexOf(a) - ORDEN_ESTADOS.indexOf(b))
        .map((estado) => ({ valor: estado, texto: textoEstado(estado) })),
      prioridades: unicos(lista.map((t) => t.prioridad)).sort(
        (a, b) => ORDEN_PRIORIDAD.indexOf(a) - ORDEN_PRIORIDAD.indexOf(b),
      ),
      fases: unicos(lista.map((t) => t.fase)).sort(),
      historias: unicos(lista.flatMap((t) => (t.historia ? [t.historia] : []))).sort(),
    };
  });

  protected readonly visibles = computed(() => {
    const f = this.filtros();
    const texto = f.texto.trim().toLowerCase();
    return this.meTocan().filter(
      (t) =>
        (!texto || t.codigo.toLowerCase().includes(texto) || t.titulo.toLowerCase().includes(texto)) &&
        (!f.tipo || t.tipo === f.tipo) &&
        (!f.estado || t.estado === f.estado) &&
        (!f.prioridad || t.prioridad === f.prioridad) &&
        (!f.fase || t.fase === f.fase) &&
        (!f.historia || t.historia === f.historia),
    );
  });

  protected readonly hayFiltros = computed(() => JSON.stringify(this.filtros()) !== JSON.stringify(SIN_FILTROS));

  /** Vista de tablero: una columna por estado, en el orden en que avanza el trabajo. */
  protected readonly columnas = computed<ColumnaTablero[]>(() => {
    const porEstado = new Map<string, TicketMio[]>();
    for (const ticket of this.visibles()) {
      porEstado.set(ticket.estado, [...(porEstado.get(ticket.estado) ?? []), ticket]);
    }
    return [...porEstado.keys()]
      .sort((a, b) => ORDEN_ESTADOS.indexOf(a) - ORDEN_ESTADOS.indexOf(b))
      .map((estado) => ({ estado, titulo: textoEstado(estado), tickets: porEstado.get(estado)! }));
  });

  constructor() {
    const proyectoId = computed(() => this.proyectos.actual()?.id ?? null);
    // Al cambiar de proyecto se limpian los filtros.
    toObservable(proyectoId)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.filtros.set(SIN_FILTROS));

    toObservable(proyectoId)
      .pipe(
        filter((id): id is number => id !== null),
        tap(() => {
          this.cargando.set(true);
          this.error.set(false);
        }),
        switchMap((id) =>
          this.service.listar(id).pipe(
            catchError(() => {
              this.error.set(true);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((tickets) => {
        this.tickets.set(tickets ?? []);
        this.cargando.set(false);
        // El número del menú lateral (Mis tickets) es "Me toca".
        if (tickets) this.proyectos.pendientesMios.set(tickets.filter((t) => t.meToca).length);
      });
  }

  protected filtrar(campo: keyof Filtros, valor: string): void {
    this.filtros.update((f) => ({ ...f, [campo]: valor }));
  }

  protected limpiarFiltros(): void {
    this.filtros.set(SIN_FILTROS);
  }

  protected cambiarVista(vista: Vista): void {
    this.vista.set(vista);
    try {
      localStorage.setItem(CLAVE_VISTA, vista);
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
  }

  protected textoPrioridad(prioridad: string): string {
    return prioridad.charAt(0) + prioridad.slice(1).toLowerCase();
  }
}

function leerVista(): Vista {
  try {
    return localStorage.getItem(CLAVE_VISTA) === 'tablero' ? 'tablero' : 'lista';
  } catch {
    return 'lista';
  }
}
