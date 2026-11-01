import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EstadoBadgeComponent } from '../../shared/components/estado-badge/estado-badge.component';
import { TicketRibbonComponent } from '../../shared/components/ticket-ribbon/ticket-ribbon.component';
import { MisActividadesService } from './mis-actividades.service';
import {
  ActividadTicket,
  ActividadTicketCodificado,
  ActividadTicketResponsable,
  ProyectoActividades,
} from './mis-actividades.models';

interface Opcion { codigo: string; nombre: string; }

function diffDias(iso: string): number {
  const date  = new Date(iso + 'T00:00:00');
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.round((date.getTime() - today.getTime()) / 86400000);
}

@Component({
  selector: 'app-mis-actividades',
  standalone: true,
  imports: [EstadoBadgeComponent, TicketRibbonComponent],
  templateUrl: './mis-actividades.component.html',
  styleUrl:    './mis-actividades.component.scss',
})
export class MisActividadesComponent implements OnInit {
  private readonly router  = inject(Router);
  private readonly service = inject(MisActividadesService);

  // ── Estado de carga ────────────────────────────────────────────────────────
  protected readonly cargando = signal(true);
  protected readonly hayError = signal(false);

  // grupos almacena únicamente response.proyectos (ProyectoActividades[])
  protected readonly grupos = signal<ProyectoActividades[]>([]);

  // ── Filtros ───────────────────────────────────────────────────────────────
  protected readonly filtroTipo      = signal('');
  protected readonly filtroEstado    = signal('');
  protected readonly filtroPrioridad = signal('');
  protected readonly busqueda        = signal('');

  // ── Colapso por proyecto ──────────────────────────────────────────────────
  protected readonly colapsados = signal<Set<number>>(new Set());

  // ── Opciones para los filtros (derivadas de los datos) ────────────────────
  protected readonly opcTipos = computed<Opcion[]>(() => {
    const map = new Map<string, string>();
    for (const g of this.grupos()) {
      const tickets = Array.isArray(g.tickets) ? g.tickets : [];
      for (const t of tickets) map.set(t.tipo.codigo, t.tipo.nombre);
    }
    return [...map.entries()].map(([codigo, nombre]) => ({ codigo, nombre }));
  });

  protected readonly opcEstados = computed<Opcion[]>(() => {
    const map = new Map<string, string>();
    for (const g of this.grupos()) {
      const tickets = Array.isArray(g.tickets) ? g.tickets : [];
      for (const t of tickets) map.set(t.estado.codigo, t.estado.nombre);
    }
    return [...map.entries()].map(([codigo, nombre]) => ({ codigo, nombre }));
  });

  protected readonly opcPrioridades = computed<Opcion[]>(() => {
    const map = new Map<string, string>();
    for (const g of this.grupos()) {
      const tickets = Array.isArray(g.tickets) ? g.tickets : [];
      for (const t of tickets) {
        if (t.prioridad) map.set(t.prioridad.codigo, t.prioridad.nombre);
      }
    }
    return [...map.entries()].map(([codigo, nombre]) => ({ codigo, nombre }));
  });

  // ── Grupos filtrados ──────────────────────────────────────────────────────
  protected readonly gruposFiltrados = computed<ProyectoActividades[]>(() => {
    const grupos = this.grupos();

    if (!Array.isArray(grupos)) {
      return [];
    }

    const tipo = this.filtroTipo();
    const est  = this.filtroEstado();
    const prio = this.filtroPrioridad();
    const busq = this.busqueda().toLowerCase().trim();

    return grupos
      .map(g => ({
        ...g,
        tickets: (Array.isArray(g.tickets) ? g.tickets : []).filter(t => {
          if (tipo && t.tipo.codigo !== tipo)       return false;
          if (est  && t.estado.codigo !== est)      return false;
          if (prio && t.prioridad?.codigo !== prio) return false;
          if (busq) {
            const coincide =
              t.folio.toLowerCase().includes(busq) ||
              t.titulo.toLowerCase().includes(busq) ||
              (t.descripcion?.toLowerCase().includes(busq) ?? false);
            if (!coincide) return false;
          }
          return true;
        }),
      }))
      .filter(g => g.tickets.length > 0);
  });

  protected readonly totalFiltrado = computed(() =>
    this.gruposFiltrados().reduce((s, g) => s + g.tickets.length, 0),
  );

  protected readonly hayFiltros = computed(() =>
    !!this.filtroTipo() || !!this.filtroEstado() ||
    !!this.filtroPrioridad() || !!this.busqueda(),
  );

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void { this.cargar(); }

  // ── Carga ─────────────────────────────────────────────────────────────────
  private cargar(): void {
    this.cargando.set(true);
    this.hayError.set(false);

    this.service.obtenerMisActividades().subscribe({
      next: response => {
        this.grupos.set(
          Array.isArray(response.proyectos) ? response.proyectos : [],
        );
        this.cargando.set(false);
      },
      error: err => {
        console.error('Error al cargar mis actividades', err);
        this.grupos.set([]);
        this.hayError.set(true);
        this.cargando.set(false);
      },
    });
  }

  // ── Handlers ──────────────────────────────────────────────────────────────
  protected reintentar(): void { this.cargar(); }

  protected onFiltroTipo(event: Event): void {
    this.filtroTipo.set((event.target as HTMLSelectElement).value);
  }

  protected onFiltroEstado(event: Event): void {
    this.filtroEstado.set((event.target as HTMLSelectElement).value);
  }

  protected onFiltroPrioridad(event: Event): void {
    this.filtroPrioridad.set((event.target as HTMLSelectElement).value);
  }

  protected onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  protected limpiarFiltros(): void {
    this.filtroTipo.set('');
    this.filtroEstado.set('');
    this.filtroPrioridad.set('');
    this.busqueda.set('');
  }

  protected toggleColapsado(id: number): void {
    const set = new Set(this.colapsados());
    if (set.has(id)) { set.delete(id); } else { set.add(id); }
    this.colapsados.set(set);
  }

  protected estaColapsado(id: number): boolean {
    return this.colapsados().has(id);
  }

  protected verTicket(folio: string): void {
    this.router.navigate(['/tickets', folio]);
  }

  // ── Helpers de display ────────────────────────────────────────────────────
  protected nombreResponsable(r: ActividadTicketResponsable | null): string {
    if (!r) return 'Sin asignar';
    if (r.nombreCompleto) return r.nombreCompleto;
    if (r.apellido)       return `${r.nombre} ${r.apellido}`;
    return r.nombre;
  }

  protected inicialesResponsable(r: ActividadTicketResponsable | null): string {
    if (!r) return '?';
    const full = r.nombreCompleto ?? `${r.nombre} ${r.apellido ?? ''}`;
    return full.trim().split(/\s+/).map(w => w[0]).join('').toUpperCase().substring(0, 2) || '?';
  }

  protected prioCss(p: ActividadTicketCodificado | null): string {
    if (!p) return '';
    const map: Record<string, string> = {
      ALTA: 'prio-alta', MEDIA: 'prio-media', BAJA: 'prio-baja',
      CRITICA: 'prio-alta', NINGUNA: 'prio-baja',
    };
    return map[p.codigo] ?? '';
  }

  protected formatFecha(ticket: ActividadTicket): string {
    const iso = ticket.fechaVencimiento ?? ticket.fechaCreacion;
    if (!iso) return 'Sin fecha';
    const diff = diffDias(iso);
    if (ticket.fechaVencimiento) {
      if (diff < -1)   return `Venció hace ${Math.abs(diff)} días`;
      if (diff === -1) return 'Venció ayer';
      if (diff === 0)  return 'Vence hoy';
      if (diff === 1)  return 'Vence mañana';
      const d = new Date(iso + 'T00:00:00');
      return 'Vence ' + d.toLocaleDateString('es', { day: 'numeric', month: 'short' });
    }
    const d = new Date(iso + 'T00:00:00');
    return 'Creado ' + d.toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  protected fechaCss(ticket: ActividadTicket): string {
    if (!ticket.fechaVencimiento) return '';
    const diff = diffDias(ticket.fechaVencimiento);
    if (diff < 0)   return 'ma-fecha--vencida';
    if (diff === 0) return 'ma-fecha--hoy';
    return '';
  }
}
