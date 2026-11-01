import {
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TicketRibbonComponent } from '../../../shared/components/ticket-ribbon/ticket-ribbon.component';
import { CrearFaseModalComponent } from './crear-fase-modal/crear-fase-modal.component';
import { CrearHistoriaModalComponent } from './crear-historia-modal/crear-historia-modal.component';
import { ToastService } from '../../../shared/services/toast.service';
import { forkJoin } from 'rxjs';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import {
  ProyectoDetalleResponse,
  ProyectoResponse,
  TicketProyectoResumenResponse,
} from '../../../core/models/proyecto.model';

// ─── Tipo ────────────────────────────────────────────────────────────────────

interface TipoCfg {
  code: string;
  label: string;
  icon: string;
  color: string;
  badge: string;
  bgLight: string;
}

const TIPOS_MAP: Record<string, TipoCfg> = {
  FASE: { code: 'FASE', label: 'Fase',          icon: 'bi-layers',          color: '#7c3aed', badge: 'FASE', bgLight: '#ede9fe' },
  HIST: { code: 'HIST', label: 'Historia',      icon: 'bi-journal-text',    color: '#2563eb', badge: 'HIST', bgLight: '#dbeafe' },
  ACT:  { code: 'ACT',  label: 'Actividad',     icon: 'bi-check2-square',   color: '#059669', badge: 'ACTV', bgLight: '#d1fae5' },
  BUG:  { code: 'BUG',  label: 'Bug',           icon: 'bi-bug',             color: '#dc2626', badge: 'BUGS', bgLight: '#fee2e2' },
  MEJ:  { code: 'MEJ',  label: 'Mejora',        icon: 'bi-arrow-up-circle', color: '#d97706', badge: 'MEJR', bgLight: '#fef3c7' },
  TEC:  { code: 'TEC',  label: 'Técnica',       icon: 'bi-gear',            color: '#475569', badge: 'TECN', bgLight: '#f1f5f9' },
  INV:  { code: 'INV',  label: 'Investigación', icon: 'bi-search',          color: '#0891b2', badge: 'INVS', bgLight: '#ecfeff' },
  DOC:  { code: 'DOC',  label: 'Documentación', icon: 'bi-file-text',       color: '#1e40af', badge: 'DOCT', bgLight: '#eff6ff' },
};

const DEFAULT_TIPO: TipoCfg = {
  code: '', label: 'Ticket', icon: 'bi-ticket', color: '#6c757d', badge: 'TICK', bgLight: '#f8f9fa',
};

const AVATAR_COLORS = ['#4361ee', '#7209b7', '#f72585', '#0d9488', '#e67e22', '#2ecc71', '#e74c3c'];

// ─── Chips de tipo ───────────────────────────────────────────────────────────

interface TipoChip { code: string | null; label: string; icon: string; color: string; }

const TIPOS_CHIPS: TipoChip[] = [
  { code: null,   label: 'Todos',         icon: 'bi-list-task',       color: '#6c757d' },
  { code: 'FASE', label: 'Fases',         icon: 'bi-layers',          color: '#7c3aed' },
  { code: 'HIST', label: 'Historias',     icon: 'bi-journal-text',    color: '#2563eb' },
  { code: 'ACT',  label: 'Actividades',   icon: 'bi-check2-square',   color: '#059669' },
  { code: 'BUG',  label: 'Bugs',          icon: 'bi-bug',             color: '#dc2626' },
  { code: 'MEJ',  label: 'Mejoras',       icon: 'bi-arrow-up-circle', color: '#d97706' },
  { code: 'TEC',  label: 'Técnicas',      icon: 'bi-gear',            color: '#475569' },
  { code: 'INV',  label: 'Investigación', icon: 'bi-search',          color: '#0891b2' },
  { code: 'DOC',  label: 'Documentación', icon: 'bi-file-text',       color: '#1e40af' },
];

// ─── Opciones de orden ───────────────────────────────────────────────────────

const ORDEN_OPCIONES = [
  { value: 'fechaCreacion',     label: 'Más recientes'  },
  { value: 'fechaCreacion_asc', label: 'Más antiguos'   },
  { value: 'prioridad',         label: 'Prioridad'      },
  { value: 'fechaVencimiento',  label: 'Fecha límite'   },
] as const;

// ─── Opciones crear ──────────────────────────────────────────────────────────

const OPCIONES_CREAR = [
  { tipo: 'FASE', label: 'Nueva fase',            icon: 'bi-layers',          color: '#7c3aed' },
  { tipo: 'HIST', label: 'Nueva historia',        icon: 'bi-journal-text',    color: '#2563eb' },
  { tipo: 'ACT',  label: 'Nueva actividad',       icon: 'bi-check2-square',   color: '#059669' },
  { tipo: 'BUG',  label: 'Nuevo bug',             icon: 'bi-bug',             color: '#dc2626' },
  { tipo: 'MEJ',  label: 'Nueva mejora',          icon: 'bi-arrow-up-circle', color: '#d97706' },
  { tipo: 'TEC',  label: 'Nueva tarea técnica',   icon: 'bi-gear',            color: '#475569' },
  { tipo: 'INV',  label: 'Nueva investigación',   icon: 'bi-search',          color: '#0891b2' },
  { tipo: 'DOC',  label: 'Nueva documentación',   icon: 'bi-file-text',       color: '#1e40af' },
];

const TIPOS_POR_NIVEL: Record<'fases' | 'historias' | 'actividades', string[]> = {
  fases:       ['FASE'],
  historias:   ['HIST'],
  actividades: ['ACT', 'BUG', 'MEJ', 'TEC', 'INV', 'DOC'],
};

@Component({
  selector: 'app-proyecto-tickets',
  standalone: true,
  imports: [RouterLink, TicketRibbonComponent, CrearFaseModalComponent, CrearHistoriaModalComponent],
  templateUrl: './proyecto-tickets.component.html',
  styleUrl: './proyecto-tickets.component.scss',
})
export class ProyectoTicketsComponent implements OnInit, OnDestroy {
  private readonly proyectoSvc = inject(ProyectoService);
  private readonly route       = inject(ActivatedRoute);
  private readonly router      = inject(Router);
  private readonly location    = inject(Location);
  private readonly elementRef  = inject(ElementRef);
  private readonly toast       = inject(ToastService);
  protected readonly session   = inject(SessionService);

  // ─── Datos ───────────────────────────────────────────────────────────────────
  readonly proyecto     = signal<ProyectoResponse | null>(null);
  readonly detalle      = signal<ProyectoDetalleResponse | null>(null);
  readonly tickets      = signal<TicketProyectoResumenResponse[]>([]);
  readonly loadingInit  = signal(true);
  readonly loadingGrid  = signal(false);
  readonly error        = signal<string | null>(null);

  // ─── Paginación ──────────────────────────────────────────────────────────────
  readonly paginaActual    = signal(0);
  readonly totalElementos  = signal(0);
  readonly totalPaginas    = signal(0);
  readonly PAGE_SIZE       = 24;

  // ─── Filtros ─────────────────────────────────────────────────────────────────
  readonly filtroTipoCodigo    = signal<string | null>(null);
  readonly filtroTipoId        = signal<number | null>(null);
  readonly filtroEstadoId      = signal<number | null>(null);
  readonly filtroEstadoNombre  = signal<string | null>(null);
  readonly filtroPrioridadId   = signal<number | null>(null);
  readonly filtroPrioridadNombre = signal<string | null>(null);
  readonly busqueda            = signal('');
  readonly ordenarPor          = signal('fechaCreacion');

  // ─── Catálogos dinámicos ─────────────────────────────────────────────────────
  readonly estadosDisponibles     = signal<{ id: number; nombre: string }[]>([]);
  readonly prioridadesDisponibles = signal<{ id: number; nombre: string }[]>([]);

  // ─── UI ──────────────────────────────────────────────────────────────────────
  readonly crearMenuOpen       = signal(false);
  readonly dropdownEstadoOpen  = signal(false);
  readonly dropdownPrioOpen    = signal(false);
  readonly dropdownOrdenOpen   = signal(false);
  menuAbiertoId: number | null = null;

  // ─── Navegación jerárquica ────────────────────────────────────────────────
  readonly todosLosTickets    = signal<TicketProyectoResumenResponse[]>([]);
  readonly nivelActual        = signal<'fases' | 'historias' | 'actividades'>('fases');
  readonly faseSeleccionada   = signal<TicketProyectoResumenResponse | null>(null);
  readonly historiaSeleccionada = signal<TicketProyectoResumenResponse | null>(null);
  readonly animClass          = signal<string>('');

  protected readonly tiposChips    = TIPOS_CHIPS;
  protected readonly ordenOpciones = ORDEN_OPCIONES;
  protected readonly opcionesCrear = OPCIONES_CREAR;

  // ─── Computed ────────────────────────────────────────────────────────────────
  readonly metricas = computed(() => this.detalle()?.metricas ?? null);

  readonly paginaInicio = computed(() =>
    this.totalElementos() === 0 ? 0 : this.paginaActual() * this.PAGE_SIZE + 1,
  );
  readonly paginaFin = computed(() =>
    Math.min((this.paginaActual() + 1) * this.PAGE_SIZE, this.totalElementos()),
  );

  readonly paginasVisibles = computed<(number | null)[]>(() => {
    const total  = this.totalPaginas();
    const actual = this.paginaActual();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const pages: (number | null)[] = [0];
    if (actual > 2)         pages.push(null);
    for (let i = Math.max(1, actual - 1); i <= Math.min(total - 2, actual + 1); i++) pages.push(i);
    if (actual < total - 3) pages.push(null);
    if (total > 1)          pages.push(total - 1);
    return pages;
  });

  readonly sortLabel = computed(() =>
    ORDEN_OPCIONES.find(o => o.value === this.ordenarPor())?.label ?? 'Ordenar',
  );

  readonly hayFiltrosActivos = computed(() =>
    this.filtroTipoCodigo() !== null ||
    this.filtroEstadoId()   !== null ||
    this.filtroPrioridadId() !== null ||
    this.busqueda() !== '',
  );

  readonly tarjetasFases = computed(() =>
    this.todosLosTickets().filter(t => t.tipo?.codigo === 'FASE'),
  );

  readonly tarjetasHistorias = computed(() => {
    const fase = this.faseSeleccionada();
    if (!fase) return [];
    return this.todosLosTickets().filter(
      t => t.tipo?.codigo === 'HIST' && t.folioTicketPadre === fase.folio,
    );
  });

  readonly tarjetasActividades = computed(() => {
    const hist = this.historiaSeleccionada();
    if (!hist) return [];
    const tiposHoja = new Set(['ACT', 'BUG', 'MEJ', 'TEC', 'INV', 'DOC']);
    return this.todosLosTickets().filter(
      t => t.folioTicketPadre === hist.folio && tiposHoja.has(t.tipo?.codigo ?? ''),
    );
  });

  readonly tarjetasActuales = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')      return this.tarjetasFases();
    if (n === 'historias')  return this.tarjetasHistorias();
    return this.tarjetasActividades();
  });

  readonly modalCrearFaseOpen      = signal(false);
  readonly modalCrearHistoriaOpen  = signal(false);

  readonly opcionesCrearActuales = computed(() => {
    const permitidos = TIPOS_POR_NIVEL[this.nivelActual()];
    const esAdmin    = this.session.usuario()?.rol === 'ADMINISTRADOR';
    return OPCIONES_CREAR.filter(op => {
      if (!permitidos.includes(op.tipo)) return false;
      if (op.tipo === 'FASE' && !esAdmin) return false;
      return true;
    });
  });

  readonly backLabel = computed(() => {
    const nivel = this.nivelActual();
    if (nivel === 'fases')     return this.proyecto()?.nombre ?? 'Proyecto';
    if (nivel === 'historias') return 'Todas las fases';
    return `Historias de ${this.faseSeleccionada()?.folio ?? ''}`;
  });

  readonly nivelTitulo = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')     return 'Tickets';
    if (n === 'historias') return 'Historias';
    return 'Tickets de trabajo';
  });

  readonly nivelDescripcion = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')     return 'Visualiza y gestiona todas las fases del proyecto.';
    if (n === 'historias') return 'Gestiona las historias pertenecientes a esta fase.';
    return 'Gestiona las actividades, bugs, mejoras, tareas técnicas, investigación y documentación de esta historia.';
  });

  readonly nivelIcono = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')     return 'bi bi-kanban';
    if (n === 'historias') return 'bi bi-journal-text';
    return 'bi bi-list-check';
  });

  readonly nivelIconoBg = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')     return '#ede9fe';
    if (n === 'historias') return '#dbeafe';
    return '#d1fae5';
  });

  readonly nivelIconoColor = computed(() => {
    const n = this.nivelActual();
    if (n === 'fases')     return '#7c3aed';
    if (n === 'historias') return '#2563eb';
    return '#059669';
  });

  protected proyectoId  = 0;
  private busquedaTimer: ReturnType<typeof setTimeout> | null = null;
  private tipoIdMap     = new Map<string, number>();
  private estadoMap     = new Map<string, { id: number; nombre: string }>();
  private prioridadMap  = new Map<string, { id: number; nombre: string }>();

  // ─── Lifecycle ───────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.proyectoId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.proyectoId) { this.router.navigate(['/pro']); return; }

    const tipoQuery = this.route.snapshot.queryParamMap.get('tipo');
    if (tipoQuery) this.filtroTipoCodigo.set(tipoQuery);

    const activo = this.session.proyectoActivo();
    if (activo?.id === this.proyectoId) this.proyecto.set(activo);

    this.loadingInit.set(true);
    this.loadingGrid.set(true);

    forkJoin({
      proyecto: this.proyectoSvc.getById(this.proyectoId),
      detalle:  this.proyectoSvc.obtenerDetalle(this.proyectoId),
      pagina:   this.proyectoSvc.obtenerTickets(this.proyectoId, {
        pagina: 0,
        tamanio: 500,
        idTipoTicket: null,
        ordenarPor: this.resolverCampoOrden(),
        direccion: this.resolverDireccion(),
      }),
    }).subscribe({
      next: ({ proyecto, detalle, pagina }) => {
        this.proyecto.set(proyecto);
        this.session.seleccionarProyecto(proyecto);
        this.detalle.set(detalle);
        this.todosLosTickets.set(pagina.contenido);
        this.tickets.set(pagina.contenido);
        this.totalElementos.set(pagina.totalElementos);
        this.totalPaginas.set(pagina.totalPaginas);
        this.buildCatalog([...detalle.ticketsRecientes, ...pagina.contenido]);
        const tc = this.filtroTipoCodigo();
        if (tc) this.filtroTipoId.set(this.tipoIdMap.get(tc) ?? null);
        this.loadingInit.set(false);
        this.loadingGrid.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el proyecto.');
        this.loadingInit.set(false);
        this.loadingGrid.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    if (this.busquedaTimer) clearTimeout(this.busquedaTimer);
  }

  // ─── Carga de tickets ────────────────────────────────────────────────────────

  private cargarTickets(): void {
    this.loadingGrid.set(true);
    this.menuAbiertoId = null;

    this.proyectoSvc.obtenerTickets(this.proyectoId, {
      pagina: 0,
      tamanio: 500,
      idTipoTicket: null,
      ordenarPor: this.resolverCampoOrden(),
      direccion: this.resolverDireccion(),
    }).subscribe({
      next: pagina => {
        this.todosLosTickets.set(pagina.contenido);
        this.tickets.set(pagina.contenido);
        this.totalElementos.set(pagina.totalElementos);
        this.totalPaginas.set(pagina.totalPaginas);
        this.buildCatalog(pagina.contenido);
        this.loadingGrid.set(false);
      },
      error: () => {
        this.loadingGrid.set(false);
      },
    });
  }

  private buildCatalog(tickets: TicketProyectoResumenResponse[]): void {
    for (const t of tickets) {
      if (t.tipo?.id && t.tipo.codigo) {
        this.tipoIdMap.set(t.tipo.codigo, t.tipo.id);
      }
      if (t.estado?.id && t.estado.nombre) {
        this.estadoMap.set(t.estado.nombre, { id: t.estado.id, nombre: t.estado.nombre });
      }
      if (t.prioridad?.id && t.prioridad.nombre) {
        this.prioridadMap.set(t.prioridad.nombre, { id: t.prioridad.id, nombre: t.prioridad.nombre });
      }
    }
    this.estadosDisponibles.set([...this.estadoMap.values()]);
    this.prioridadesDisponibles.set([...this.prioridadMap.values()]);
  }

  private resolverCampoOrden(): string {
    const o = this.ordenarPor();
    if (o === 'fechaCreacion_asc') return 'fechaCreacion';
    return o;
  }

  private resolverDireccion(): 'ASC' | 'DESC' {
    return this.ordenarPor() === 'fechaCreacion_asc' ? 'ASC' : 'DESC';
  }

  // ─── Filtros ─────────────────────────────────────────────────────────────────

  protected setFiltroTipo(codigo: string | null): void {
    this.filtroTipoCodigo.set(codigo);
    this.filtroTipoId.set(codigo ? (this.tipoIdMap.get(codigo) ?? null) : null);
    this.paginaActual.set(0);
    this.cargarTickets();
  }

  protected setFiltroEstado(id: number | null, nombre: string | null): void {
    this.filtroEstadoId.set(id);
    this.filtroEstadoNombre.set(nombre);
    this.dropdownEstadoOpen.set(false);
    this.paginaActual.set(0);
    this.cargarTickets();
  }

  protected setFiltroPrioridad(id: number | null, nombre: string | null): void {
    this.filtroPrioridadId.set(id);
    this.filtroPrioridadNombre.set(nombre);
    this.dropdownPrioOpen.set(false);
    this.paginaActual.set(0);
    this.cargarTickets();
  }

  protected setOrden(value: string): void {
    this.ordenarPor.set(value);
    this.dropdownOrdenOpen.set(false);
    this.paginaActual.set(0);
    this.cargarTickets();
  }

  protected onBusqueda(valor: string): void {
    if (this.busquedaTimer) clearTimeout(this.busquedaTimer);
    this.busquedaTimer = setTimeout(() => {
      this.busqueda.set(valor.trim());
      this.paginaActual.set(0);
      this.cargarTickets();
    }, 380);
  }

  protected limpiarFiltros(): void {
    this.filtroTipoCodigo.set(null);
    this.filtroTipoId.set(null);
    this.filtroEstadoId.set(null);
    this.filtroEstadoNombre.set(null);
    this.filtroPrioridadId.set(null);
    this.filtroPrioridadNombre.set(null);
    this.busqueda.set('');
    this.paginaActual.set(0);
    this.cargarTickets();
  }

  // ─── Paginación ──────────────────────────────────────────────────────────────

  protected irAPagina(pagina: number): void {
    const max = this.totalPaginas() - 1;
    if (pagina < 0 || pagina > max) return;
    this.paginaActual.set(pagina);
    this.cargarTickets();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // ─── Dropdowns ───────────────────────────────────────────────────────────────

  protected toggleCrear(event: MouseEvent): void {
    event.stopPropagation();
    const next = !this.crearMenuOpen();
    this.cerrarDropdowns();
    this.crearMenuOpen.set(next);
  }

  protected toggleDropdownEstado(event: MouseEvent): void {
    event.stopPropagation();
    const next = !this.dropdownEstadoOpen();
    this.cerrarDropdowns();
    this.dropdownEstadoOpen.set(next);
  }

  protected toggleDropdownPrio(event: MouseEvent): void {
    event.stopPropagation();
    const next = !this.dropdownPrioOpen();
    this.cerrarDropdowns();
    this.dropdownPrioOpen.set(next);
  }

  protected toggleDropdownOrden(event: MouseEvent): void {
    event.stopPropagation();
    const next = !this.dropdownOrdenOpen();
    this.cerrarDropdowns();
    this.dropdownOrdenOpen.set(next);
  }

  protected toggleMenu(id: number, event: MouseEvent): void {
    event.stopPropagation();
    this.cerrarDropdowns();
    this.menuAbiertoId = this.menuAbiertoId === id ? null : id;
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: Event): void {
    if (!(this.elementRef.nativeElement as HTMLElement).contains(event.target as Node)) {
      this.cerrarDropdowns();
      this.menuAbiertoId = null;
    }
  }

  private cerrarDropdowns(): void {
    this.crearMenuOpen.set(false);
    this.dropdownEstadoOpen.set(false);
    this.dropdownPrioOpen.set(false);
    this.dropdownOrdenOpen.set(false);
  }

  // ─── Navegación jerárquica ────────────────────────────────────────────────────

  protected seleccionarFase(fase: TicketProyectoResumenResponse): void {
    this.transicionANivel(() => {
      this.faseSeleccionada.set(fase);
      this.historiaSeleccionada.set(null);
      this.nivelActual.set('historias');
    });
  }

  protected seleccionarHistoria(historia: TicketProyectoResumenResponse): void {
    this.transicionANivel(() => {
      this.historiaSeleccionada.set(historia);
      this.nivelActual.set('actividades');
    });
  }

  protected volverAFases(): void {
    this.transicionANivel(() => {
      this.nivelActual.set('fases');
      this.faseSeleccionada.set(null);
      this.historiaSeleccionada.set(null);
    });
  }

  protected volverAHistorias(): void {
    this.transicionANivel(() => {
      this.nivelActual.set('historias');
      this.historiaSeleccionada.set(null);
    });
  }

  protected explorar(ticket: TicketProyectoResumenResponse): void {
    const nivel = this.nivelActual();
    if (nivel === 'fases')          this.seleccionarFase(ticket);
    else if (nivel === 'historias') this.seleccionarHistoria(ticket);
  }

  protected esNavegable(codigo: string | null | undefined): boolean {
    return codigo === 'FASE' || codigo === 'HIST';
  }

  private transicionANivel(cambio: () => void): void {
    this.animClass.set('tc-nivel--exit');
    setTimeout(() => {
      cambio();
      this.animClass.set('tc-nivel--enter');
      setTimeout(() => this.animClass.set(''), 280);
    }, 160);
  }

  // ─── Crear ticket ────────────────────────────────────────────────────────────

  protected crearTicket(tipo: string): void {
    this.cerrarDropdowns();
    if (tipo === 'FASE') {
      this.modalCrearFaseOpen.set(true);
    } else if (tipo === 'HIST') {
      this.modalCrearHistoriaOpen.set(true);
    }
  }

  protected onFaseCreada(fase: TicketProyectoResumenResponse): void {
    this.todosLosTickets.update(lista => [...lista, fase]);
    this.modalCrearFaseOpen.set(false);
  }

  protected onHistoriaCreada(): void {
    this.modalCrearHistoriaOpen.set(false);
    this.cargarTickets();
  }

  // ─── Navegación ──────────────────────────────────────────────────────────────

  protected verDetalle(ticket: TicketProyectoResumenResponse): void {
    this.router.navigate(['/tickets', ticket.folio]);
  }

  protected copiarEnlace(folio: string, event: MouseEvent): void {
    event.stopPropagation();
    navigator.clipboard.writeText(`${window.location.origin}/tickets/${folio}`).catch(() => {});
    this.menuAbiertoId = null;
  }

  protected volver(): void {
    this.location.back();
  }

  protected volverContextual(): void {
    const nivel = this.nivelActual();
    if (nivel === 'fases')          this.location.back();
    else if (nivel === 'historias') this.volverAFases();
    else                            this.volverAHistorias();
  }

  // ─── Helpers de presentación ──────────────────────────────────────────────────

  protected tipoCfg(code: string | null | undefined): TipoCfg {
    return TIPOS_MAP[code ?? ''] ?? DEFAULT_TIPO;
  }

  protected tipoCount(code: string | null): number {
    const m = this.metricas();
    if (!m) return 0;
    const map: Record<string, number> = {
      FASE: m.totalFases,
      HIST: m.totalHistorias,
      ACT:  m.totalActividades,
      BUG:  m.totalBugs,
      MEJ:  m.totalMejoras,
      TEC:  m.totalTareasTecnicas,
      INV:  m.totalInvestigaciones,
      DOC:  m.totalDocumentaciones,
    };
    return code ? (map[code] ?? 0) : m.totalTickets;
  }

  protected avatarColor(id: number): string {
    return AVATAR_COLORS[id % AVATAR_COLORS.length];
  }

  protected formatFecha(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    const d = new Date(fecha);
    if (isNaN(d.getTime())) return fecha;
    return d.toLocaleDateString('es-MX', { day: '2-digit', month: 'short' });
  }

  protected inferirProgreso(ticket: TicketProyectoResumenResponse): number {
    const n = (ticket.estado?.nombre ?? '').toLowerCase();
    if (n.includes('finaliz') || n.includes('complet') || n.includes('done')) return 100;
    if (n.includes('qa')      || n.includes('prueba'))                         return 85;
    if (n.includes('revis'))                                                    return 70;
    if (n.includes('prog')    || n.includes('desar'))                          return 40;
    return 0;
  }

  protected estadoCss(nombre: string | null | undefined): string {
    const n = (nombre ?? '').toLowerCase();
    if (n.includes('prog') || n.includes('desar'))                              return 'est-prog';
    if (n.includes('finaliz') || n.includes('complet') || n.includes('done'))  return 'est-done';
    if (n.includes('revis'))                                                    return 'est-revi';
    if (n.includes('qa') || n.includes('prueba'))                              return 'est-qa';
    if (n.includes('cancel'))                                                   return 'est-canc';
    if (n.includes('bloq'))                                                     return 'est-bloq';
    return 'est-pend';
  }

  protected prioridadCss(nombre: string | null | undefined): string {
    const n = (nombre ?? '').toLowerCase();
    if (n.includes('crít') || n.includes('crit'))    return 'prio-critica';
    if (n.includes('alta') || n.includes('high'))    return 'prio-alta';
    if (n.includes('media') || n.includes('medium')) return 'prio-media';
    return 'prio-baja';
  }

  protected prioridadIcon(nombre: string | null | undefined): string {
    const n = (nombre ?? '').toLowerCase();
    if (n.includes('crít') || n.includes('crit'))    return 'bi-arrow-up-circle-fill';
    if (n.includes('alta') || n.includes('high'))    return 'bi-arrow-up';
    if (n.includes('media') || n.includes('medium')) return 'bi-dash';
    return 'bi-arrow-down';
  }

  protected estaVencido(ticket: TicketProyectoResumenResponse): boolean {
    if (!ticket.fechaVencimiento) return false;
    const n = (ticket.estado?.nombre ?? '').toLowerCase();
    if (n.includes('finaliz') || n.includes('cancel')) return false;
    return new Date(ticket.fechaVencimiento) < new Date();
  }

  protected progresoCssByEstado(ticket: TicketProyectoResumenResponse): string {
    const pct = this.inferirProgreso(ticket);
    if (pct === 100) return 'prog-done';
    if (pct >= 50)   return 'prog-mid';
    if (pct > 0)     return 'prog-low';
    return 'prog-none';
  }

  protected readonly skeletons = Array.from({ length: 8 }, (_, i) => i);
}
