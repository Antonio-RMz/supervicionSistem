import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService }  from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { CrearHistoriaModalComponent } from '../tickets/crear-historia-modal/crear-historia-modal.component';

// ─── Types ────────────────────────────────────────────────────────────────────

type Prioridad  = 'ALTA' | 'MEDIA' | 'BAJA';
type EstadoHist = 'PENDIENTE' | 'EN_PROGRESO' | 'COMPLETADA';

interface MiembroEquipo {
  id: number; nombre: string; apellido: string; iniciales: string; color: string;
}

interface HistoriaFila {
  id: number; codigo: string; titulo: string;
  prioridad: Prioridad; estado: EstadoHist;
  faseId: number; faseNombre: string; faseColor: string;
  asignadoId: number | null;
}

interface GrupoFase {
  faseId: number; faseNombre: string; faseColor: string;
  historias: HistoriaFila[];
}

interface FaseSummary { id: number; nombre: string; color: string; }

// ─── Mock data ────────────────────────────────────────────────────────────────

const EQUIPO: MiembroEquipo[] = [
  { id: 1, nombre: 'Jorge',   apellido: 'Sebastián', iniciales: 'JS', color: '#4361ee' },
  { id: 2, nombre: 'Laura',   apellido: 'Gómez',     iniciales: 'LG', color: '#7209b7' },
  { id: 3, nombre: 'Mariana', apellido: 'Torres',    iniciales: 'MT', color: '#0d9488' },
  { id: 4, nombre: 'Carlos',  apellido: 'Mendoza',   iniciales: 'CM', color: '#e67e22' },
];

const HISTORIAS: HistoriaFila[] = [
  { id: 1,  codigo: 'NUC-101', titulo: 'Definir alcance y objetivos del sistema',            prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', asignadoId: 2 },
  { id: 2,  codigo: 'NUC-102', titulo: 'Levantar requerimientos funcionales con el cliente', prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', asignadoId: 4 },
  { id: 3,  codigo: 'NUC-103', titulo: 'Diseñar arquitectura de la solución',                prioridad: 'MEDIA', estado: 'COMPLETADA',  faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', asignadoId: 1 },
  { id: 4,  codigo: 'NUC-104', titulo: 'Validar wireframes con el equipo de diseño',         prioridad: 'BAJA',  estado: 'COMPLETADA',  faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', asignadoId: 4 },
  { id: 5,  codigo: 'NUC-201', titulo: 'Implementar autenticación JWT con refresh token',    prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 2, faseNombre: 'Desarrollo',                faseColor: '#7209b7', asignadoId: 1 },
  { id: 6,  codigo: 'NUC-202', titulo: 'Crear módulo de gestión de proyectos (CRUD)',        prioridad: 'ALTA',  estado: 'EN_PROGRESO', faseId: 2, faseNombre: 'Desarrollo',                faseColor: '#7209b7', asignadoId: 1 },
  { id: 7,  codigo: 'NUC-203', titulo: 'Implementar API de fases e historias',               prioridad: 'ALTA',  estado: 'EN_PROGRESO', faseId: 2, faseNombre: 'Desarrollo',                faseColor: '#7209b7', asignadoId: 1 },
  { id: 8,  codigo: 'NUC-204', titulo: 'Diseñar y crear base de datos relacional',           prioridad: 'MEDIA', estado: 'COMPLETADA',  faseId: 2, faseNombre: 'Desarrollo',                faseColor: '#7209b7', asignadoId: 1 },
  { id: 9,  codigo: 'NUC-205', titulo: 'Integrar notificaciones en tiempo real',             prioridad: 'MEDIA', estado: 'PENDIENTE',   faseId: 2, faseNombre: 'Desarrollo',                faseColor: '#7209b7', asignadoId: null },
  { id: 10, codigo: 'NUC-301', titulo: 'Pruebas unitarias de servicios backend',             prioridad: 'ALTA',  estado: 'PENDIENTE',   faseId: 3, faseNombre: 'Pruebas',                   faseColor: '#0d9488', asignadoId: 3 },
  { id: 11, codigo: 'NUC-302', titulo: 'Pruebas de integración entre módulos',               prioridad: 'MEDIA', estado: 'PENDIENTE',   faseId: 3, faseNombre: 'Pruebas',                   faseColor: '#0d9488', asignadoId: 3 },
  { id: 12, codigo: 'NUC-401', titulo: 'Preparar entorno de producción',                     prioridad: 'ALTA',  estado: 'PENDIENTE',   faseId: 4, faseNombre: 'Implementación',            faseColor: '#e67e22', asignadoId: null },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

const PRIO_COLOR: Record<Prioridad, string> = {
  ALTA: '#dc2626', MEDIA: '#f59e0b', BAJA: '#10b981',
};

const ESTADO_CFG: Record<EstadoHist, { label: string; css: string }> = {
  PENDIENTE:   { label: 'Por hacer',   css: 'est-pendiente' },
  EN_PROGRESO: { label: 'En progreso', css: 'est-progreso'  },
  COMPLETADA:  { label: 'Hecho',       css: 'est-hecho'     },
};

// ─── Componente ───────────────────────────────────────────────────────────────

@Component({
  selector: 'app-historias',
  standalone: true,
  imports: [CrearHistoriaModalComponent],
  templateUrl: './historias.component.html',
  styleUrl:    './historias.component.scss',
})
export class HistoriasComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId = signal(0);

  // ── Datos ──
  protected readonly historias = signal<HistoriaFila[]>([]);
  protected readonly equipo    = signal<MiembroEquipo[]>([]);

  // ── Filtros ──
  protected readonly filtroBusqueda   = signal('');
  protected readonly filtroFaseId     = signal<number | null>(null);
  protected readonly filtroEstado     = signal<EstadoHist | ''>('');
  protected readonly filtroAsignadoId = signal<number | null>(null);

  // ── Modal ──
  protected readonly mostrarModal = signal(false);

  // ── Computed ──
  protected readonly fasesDisponibles = computed<FaseSummary[]>(() => {
    const map = new Map<number, FaseSummary>();
    for (const h of this.historias()) {
      if (!map.has(h.faseId)) {
        map.set(h.faseId, { id: h.faseId, nombre: h.faseNombre, color: h.faseColor });
      }
    }
    return [...map.values()].sort((a, b) => a.id - b.id);
  });

  protected readonly gruposFiltrados = computed<GrupoFase[]>(() => {
    const q        = this.filtroBusqueda().toLowerCase().trim();
    const faseId   = this.filtroFaseId();
    const estado   = this.filtroEstado();
    const asignado = this.filtroAsignadoId();

    let lista = this.historias();
    if (q)              lista = lista.filter(h =>
      h.codigo.toLowerCase().includes(q) || h.titulo.toLowerCase().includes(q));
    if (faseId !== null) lista = lista.filter(h => h.faseId === faseId);
    if (estado)          lista = lista.filter(h => h.estado === estado);
    if (asignado !== null) lista = lista.filter(h => h.asignadoId === asignado);

    const grupos = new Map<number, GrupoFase>();
    for (const h of lista) {
      if (!grupos.has(h.faseId)) {
        grupos.set(h.faseId, {
          faseId: h.faseId, faseNombre: h.faseNombre, faseColor: h.faseColor, historias: [],
        });
      }
      grupos.get(h.faseId)!.historias.push(h);
    }
    return [...grupos.values()].sort((a, b) => a.faseId - b.faseId);
  });

  protected readonly totalHistorias  = computed(() => this.historias().length);
  protected readonly totalFiltradas  = computed(() =>
    this.gruposFiltrados().reduce((n, g) => n + g.historias.length, 0));
  protected readonly hayFiltrosActivos = computed(() =>
    !!this.filtroBusqueda().trim() || this.filtroFaseId() !== null ||
    !!this.filtroEstado()           || this.filtroAsignadoId() !== null);
  protected readonly hayResultados   = computed(() => this.gruposFiltrados().length > 0);

  private readonly rol = computed(() => this.session.usuario()?.rol ?? '');
  protected readonly puedeCrear = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/pro']); return; }
    this.proyectoId.set(id);

    const activo = this.session.proyectoActivo();
    if (!activo || activo.id !== id) {
      this.proyectoSvc.getById(id).subscribe({
        next:  p => this.session.seleccionarProyecto(p),
        error: () => this.router.navigate(['/pro']),
      });
    }

    this.historias.set(HISTORIAS);
    this.equipo.set(EQUIPO);
  }

  // ── Filtros ──
  protected setFiltroFase(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroFaseId.set(v ? Number(v) : null);
  }
  protected setFiltroEstado(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroEstado.set(v as EstadoHist | '');
  }
  protected setFiltroAsignado(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroAsignadoId.set(v ? Number(v) : null);
  }
  protected limpiarFiltros(): void {
    this.filtroBusqueda.set('');
    this.filtroFaseId.set(null);
    this.filtroEstado.set('');
    this.filtroAsignadoId.set(null);
  }

  // ── Modal ──
  protected onHistoriaCreada(): void {
    this.mostrarModal.set(false);
  }

  // ── Navegación ──
  protected irADetalle(id: number): void {
    this.router.navigate(['/pro', this.proyectoId(), 'historias', id]);
  }
  protected volver(): void {
    this.router.navigate(['/pro', this.proyectoId()]);
  }

  // ── Helpers de vista ──
  protected getAsignado(id: number | null): MiembroEquipo | null {
    return id !== null ? (this.equipo().find(u => u.id === id) ?? null) : null;
  }
  protected prioColor(p: Prioridad):  string { return PRIO_COLOR[p]; }
  protected estadoClass(e: string):   string { return ESTADO_CFG[e as EstadoHist]?.css  ?? ''; }
  protected estadoLabel(e: string):   string { return ESTADO_CFG[e as EstadoHist]?.label ?? e; }
}
