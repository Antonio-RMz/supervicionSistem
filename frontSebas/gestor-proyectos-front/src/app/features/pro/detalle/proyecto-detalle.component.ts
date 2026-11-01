import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import {
  ProyectoDetalleResponse,
  ProyectoResponse,
  TicketProyectoResumenResponse,
} from '../../../core/models/proyecto.model';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge/estado-badge.component';

// ─── Tab ─────────────────────────────────────────────────────────────────────
type TabId = 'resumen' | 'bugs' | 'documentos' | 'actividad';

const ROL_LABEL: Record<string, string> = {
  ANALISTA_COMERCIAL:  'Analista Comercial',
  'LÍDER_DE_PROYECTO': 'Líder de Proyecto',
  DESARROLLADOR:       'Desarrollador',
  TESTER:              'Tester',
  ADMINISTRADOR:       'Administrador',
};

const ROL_COLOR_CLASS: Record<string, string> = {
  ANALISTA_COMERCIAL:  'rol-analista',
  'LÍDER_DE_PROYECTO': 'rol-lider',
  DESARROLLADOR:       'rol-dev',
  TESTER:              'rol-tester',
  ADMINISTRADOR:       'rol-admin',
};

@Component({
  selector: 'app-proyecto-detalle',
  standalone: true,
  imports: [RouterLink, EstadoBadgeComponent],
  templateUrl: './proyecto-detalle.component.html',
  styleUrl:    './proyecto-detalle.component.scss',
})
export class ProyectoDetalleComponent implements OnInit {
  protected readonly session    = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyecto = signal<ProyectoResponse | null>(null);
  protected readonly detalle  = signal<ProyectoDetalleResponse | null>(null);
  protected readonly loading  = signal(true);
  protected readonly error    = signal<string | null>(null);

  protected readonly rol          = computed(() => this.session.usuario()?.rol ?? null);
  protected readonly puedeEditar  = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO',
  );
  protected readonly rolLabel      = computed(() => ROL_LABEL[this.rol() ?? ''] ?? this.rol() ?? '');
  protected readonly rolColorClass = computed(() => ROL_COLOR_CLASS[this.rol() ?? ''] ?? '');

  // ── Tabs ──────────────────────────────────────────────────────────────────
  protected readonly tabActual = signal<TabId>('resumen');

  protected readonly tabs: { id: TabId; label: string; icon: string }[] = [
    { id: 'resumen',    label: 'Resumen',    icon: 'bi-grid-1x2'      },
    { id: 'bugs',       label: 'Bugs',       icon: 'bi-bug'           },
    { id: 'documentos', label: 'Documentos', icon: 'bi-file-earmark'  },
    { id: 'actividad',  label: 'Actividad',  icon: 'bi-clock-history' },
  ];

  // ── Actividad reciente ────────────────────────────────────────────────────
  protected readonly actividadReciente = computed(() => this.detalle()?.actividadReciente ?? []);

  // ── Bugs (tickets de tipo BUG/BUGS) ──────────────────────────────────────
  protected readonly bugsFiltrados = computed(() =>
    (this.detalle()?.ticketsRecientes ?? []).filter(t => {
      const cod = t.tipo?.codigo?.toUpperCase() ?? '';
      return cod === 'BUG' || cod === 'BUGS';
    }),
  );

  private proyectoId = 0;

  ngOnInit(): void {
    this.proyectoId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.proyectoId) { this.router.navigate(['/pro']); return; }

    const activo = this.session.proyectoActivo();
    if (activo?.id === this.proyectoId) {
      this.proyecto.set(activo);
    }

    this.cargar(this.proyectoId);
  }

  private cargar(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      proyecto: this.proyectoSvc.getById(id),
      detalle:  this.proyectoSvc.obtenerDetalle(id),
    }).subscribe({
      next: ({ proyecto, detalle }) => {
        this.proyecto.set(proyecto);
        this.session.seleccionarProyecto(proyecto);
        this.detalle.set(detalle);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el proyecto.');
        this.loading.set(false);
      },
    });
  }

  protected irAEditar(): void {
    if (this.proyectoId) this.router.navigate(['/pro', this.proyectoId, 'editar']);
  }

  protected irATickets(tipo: string | null): void {
    const extras = tipo ? { queryParams: { tipo } } : {};
    this.router.navigate(['/pro', this.proyectoId, 'tickets'], extras);
  }

  protected irAEquipo(): void {
    this.router.navigate(['/pro', this.proyectoId, 'equipo']);
  }

  protected irANuevoTicket(): void {
    this.router.navigate(['/pro', this.proyectoId, 'tickets'], { queryParams: { nuevo: true } });
  }

  protected irADetalleTicket(ticket: TicketProyectoResumenResponse): void {
    this.router.navigate(['/tickets', ticket.folio]);
  }

  protected getTipoClass(tipoCode: string): string {
    const map: Record<string, string> = {
      HIST: 'tipo-hist', ACT: 'tipo-act', BUG: 'tipo-bug',
      MEJ:  'tipo-mej',  TEC: 'tipo-tec', INV: 'tipo-inv',
      DOC:  'tipo-doc',  FASE: 'tipo-fase',
    };
    return map[tipoCode] ?? '';
  }

  protected getPrioridadClass(nombre: string): string {
    const map: Record<string, string> = { Alta: 'pri-alta', Media: 'pri-media', Baja: 'pri-baja' };
    return map[nombre] ?? '';
  }

  protected getEstadoClass(nombre: string): string {
    const map: Record<string, string> = {
      'En progreso': 'est-progreso',
      'Completado':  'est-completado',
      'Pendiente':   'est-pendiente',
      'Abierto':     'est-abierto',
      'Cerrado':     'est-cerrado',
      'En revisión': 'est-revision',
    };
    return map[nombre] ?? '';
  }

  protected formatDate(iso: string | null | undefined): string {
    if (!iso) return 'Por definir';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    const M = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    return `${d.getDate()} ${M[d.getMonth()]} ${d.getFullYear()}`;
  }
}
