import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService }  from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge/estado-badge.component';
import { EstadoActividad } from '../../../core/models/actividad.model';

// ─── Types ────────────────────────────────────────────────────────────────────

interface MiembroEquipo {
  id: number; nombre: string; apellido: string; iniciales: string; color: string;
}

interface ActividadFila {
  id: number; codigo: string; titulo: string; estado: EstadoActividad;
  faseId: number; faseNombre: string; faseColor: string;
  historiaId: number; historiaCodigo: string;
  asignadoId: number | null;
}

interface FaseSummary     { id: number; nombre: string; color: string; }
interface HistoriaSummary { id: number; codigo: string; titulo: string; faseId: number; }

// ─── Constantes ───────────────────────────────────────────────────────────────

const ESTADO_DOT: Record<EstadoActividad, string> = {
  POR_HACER:           '#9ca3af',
  EN_DESARROLLO:       '#3b82f6',
  REVISION_QA:         '#f59e0b',
  LISTO_PARA_INTEGRAR: '#8b5cf6',
  PR_EN_REVISION:      '#a855f7',
  INTEGRADO:           '#10b981',
};

// ─── Mock data ────────────────────────────────────────────────────────────────

const EQUIPO: MiembroEquipo[] = [
  { id: 1, nombre: 'Jorge',   apellido: 'Sebastián', iniciales: 'JS', color: '#4361ee' },
  { id: 2, nombre: 'Laura',   apellido: 'Gómez',     iniciales: 'LG', color: '#7209b7' },
  { id: 3, nombre: 'Mariana', apellido: 'Torres',    iniciales: 'MT', color: '#0d9488' },
  { id: 4, nombre: 'Carlos',  apellido: 'Mendoza',   iniciales: 'CM', color: '#e67e22' },
];

const HISTORIAS_PROYECTO: HistoriaSummary[] = [
  { id: 1,  codigo: 'NUC-101', titulo: 'Definir alcance y objetivos del sistema',            faseId: 1 },
  { id: 2,  codigo: 'NUC-102', titulo: 'Levantar requerimientos funcionales con el cliente', faseId: 1 },
  { id: 3,  codigo: 'NUC-103', titulo: 'Diseñar arquitectura de la solución',                faseId: 1 },
  { id: 5,  codigo: 'NUC-201', titulo: 'Implementar autenticación JWT con refresh token',    faseId: 2 },
  { id: 6,  codigo: 'NUC-202', titulo: 'Crear módulo de gestión de proyectos (CRUD)',        faseId: 2 },
  { id: 7,  codigo: 'NUC-203', titulo: 'Implementar API de fases e historias',               faseId: 2 },
  { id: 8,  codigo: 'NUC-204', titulo: 'Diseñar y crear base de datos relacional',           faseId: 2 },
  { id: 9,  codigo: 'NUC-205', titulo: 'Integrar notificaciones en tiempo real',             faseId: 2 },
  { id: 10, codigo: 'NUC-301', titulo: 'Pruebas unitarias de servicios backend',             faseId: 3 },
  { id: 11, codigo: 'NUC-302', titulo: 'Pruebas de integración entre módulos',               faseId: 3 },
  { id: 12, codigo: 'NUC-401', titulo: 'Preparar entorno de producción',                     faseId: 4 },
];

const ACTIVIDADES: ActividadFila[] = [
  // Fase 1 — Análisis y Levantamiento
  { id:  1, codigo: 'ACT-001', titulo: 'Entrevistas con stakeholders',                    estado: 'INTEGRADO',           faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', historiaId:  1, historiaCodigo: 'NUC-101', asignadoId: 2 },
  { id:  2, codigo: 'ACT-002', titulo: 'Documentar requerimientos funcionales',           estado: 'INTEGRADO',           faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', historiaId:  2, historiaCodigo: 'NUC-102', asignadoId: 4 },
  { id:  3, codigo: 'ACT-003', titulo: 'Definir diagramas de arquitectura C4',            estado: 'INTEGRADO',           faseId: 1, faseNombre: 'Análisis y Levantamiento', faseColor: '#4361ee', historiaId:  3, historiaCodigo: 'NUC-103', asignadoId: 1 },
  // Fase 2 — Desarrollo
  { id:  4, codigo: 'ACT-004', titulo: 'Implementar autenticación JWT con refresh token', estado: 'INTEGRADO',           faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  5, historiaCodigo: 'NUC-201', asignadoId: 1 },
  { id:  5, codigo: 'ACT-005', titulo: 'Crear endpoints CRUD de proyectos',               estado: 'INTEGRADO',           faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  6, historiaCodigo: 'NUC-202', asignadoId: 1 },
  { id:  6, codigo: 'ACT-006', titulo: 'Implementar gestión de fases y tablero Kanban',   estado: 'PR_EN_REVISION',      faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  6, historiaCodigo: 'NUC-202', asignadoId: 1 },
  { id:  7, codigo: 'ACT-007', titulo: 'Crear API REST de historias de usuario',          estado: 'LISTO_PARA_INTEGRAR', faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  7, historiaCodigo: 'NUC-203', asignadoId: 1 },
  { id:  8, codigo: 'ACT-008', titulo: 'Diseñar esquema de base de datos relacional',     estado: 'INTEGRADO',           faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  8, historiaCodigo: 'NUC-204', asignadoId: 1 },
  { id:  9, codigo: 'ACT-009', titulo: 'Migrar migraciones a PostgreSQL 15',              estado: 'REVISION_QA',         faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  8, historiaCodigo: 'NUC-204', asignadoId: 1 },
  { id: 10, codigo: 'ACT-010', titulo: 'Implementar módulo de notificaciones push',       estado: 'EN_DESARROLLO',       faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  9, historiaCodigo: 'NUC-205', asignadoId: null },
  { id: 11, codigo: 'ACT-011', titulo: 'Configurar WebSockets para tiempo real',          estado: 'POR_HACER',           faseId: 2, faseNombre: 'Desarrollo', faseColor: '#7209b7', historiaId:  9, historiaCodigo: 'NUC-205', asignadoId: null },
  // Fase 3 — Pruebas
  { id: 12, codigo: 'ACT-012', titulo: 'Pruebas unitarias de autenticación',              estado: 'POR_HACER',           faseId: 3, faseNombre: 'Pruebas', faseColor: '#0d9488', historiaId: 10, historiaCodigo: 'NUC-301', asignadoId: 3 },
  { id: 13, codigo: 'ACT-013', titulo: 'Pruebas de integración de proyectos',             estado: 'POR_HACER',           faseId: 3, faseNombre: 'Pruebas', faseColor: '#0d9488', historiaId: 11, historiaCodigo: 'NUC-302', asignadoId: 3 },
  // Fase 4 — Implementación
  { id: 14, codigo: 'ACT-014', titulo: 'Configurar servidor de producción en AWS',        estado: 'POR_HACER',           faseId: 4, faseNombre: 'Implementación', faseColor: '#e67e22', historiaId: 12, historiaCodigo: 'NUC-401', asignadoId: null },
  { id: 15, codigo: 'ACT-015', titulo: 'Configurar pipeline de CI/CD con GitHub Actions', estado: 'POR_HACER',           faseId: 4, faseNombre: 'Implementación', faseColor: '#e67e22', historiaId: 12, historiaCodigo: 'NUC-401', asignadoId: 4 },
];

// ─── Componente ───────────────────────────────────────────────────────────────

@Component({
  selector: 'app-actividades',
  standalone: true,
  imports: [EstadoBadgeComponent],
  templateUrl: './actividades.component.html',
  styleUrl:    './actividades.component.scss',
})
export class ActividadesComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId = signal(0);

  // ── Datos ──
  protected readonly actividades       = signal<ActividadFila[]>([]);
  protected readonly equipo            = signal<MiembroEquipo[]>([]);
  protected readonly historiasProyecto = signal<HistoriaSummary[]>([]);

  // ── Filtros ──
  protected readonly filtroBusqueda = signal('');
  protected readonly filtroFaseId   = signal<number | null>(null);
  protected readonly filtroEstado   = signal<EstadoActividad | ''>('');

  // ── Modal ──
  protected readonly mostrarModal       = signal(false);
  protected readonly nuevaActTitulo     = signal('');
  protected readonly nuevaActFaseId     = signal<number | null>(null);
  protected readonly nuevaActHistoriaId = signal<number | null>(null);

  // ── Computed base: solo las actividades asignadas al usuario de sesión ──
  private readonly misActividades = computed<ActividadFila[]>(() => {
    const uid = this.session.usuario()?.id ?? -1;
    return this.actividades().filter(a => a.asignadoId === uid);
  });

  // ── Fases disponibles calculadas de las actividades del usuario ──
  protected readonly fasesDisponibles = computed<FaseSummary[]>(() => {
    const map = new Map<number, FaseSummary>();
    for (const a of this.misActividades()) {
      if (!map.has(a.faseId))
        map.set(a.faseId, { id: a.faseId, nombre: a.faseNombre, color: a.faseColor });
    }
    return [...map.values()].sort((a, b) => a.id - b.id);
  });

  // ── Lista final con filtros de búsqueda, fase y estado ──
  protected readonly actividadesFiltradas = computed<ActividadFila[]>(() => {
    const q      = this.filtroBusqueda().toLowerCase().trim();
    const faseId = this.filtroFaseId();
    const estado = this.filtroEstado();

    let lista = this.misActividades();
    if (q)               lista = lista.filter(a =>
      a.codigo.toLowerCase().includes(q) ||
      a.titulo.toLowerCase().includes(q)  ||
      a.historiaCodigo.toLowerCase().includes(q));
    if (faseId !== null) lista = lista.filter(a => a.faseId === faseId);
    if (estado)          lista = lista.filter(a => a.estado === estado);
    return lista;
  });

  protected readonly totalPropias      = computed(() => this.misActividades().length);
  protected readonly totalFiltradas    = computed(() => this.actividadesFiltradas().length);
  protected readonly totalIntegradas   = computed(() =>
    this.misActividades().filter(a => a.estado === 'INTEGRADO').length);
  protected readonly hayFiltrosActivos = computed(() =>
    !!this.filtroBusqueda().trim() || this.filtroFaseId() !== null || !!this.filtroEstado());

  protected readonly historiasModal = computed<HistoriaSummary[]>(() => {
    const faseId = this.nuevaActFaseId();
    if (faseId === null) return [];
    return this.historiasProyecto().filter(h => h.faseId === faseId);
  });

  private   readonly rol        = computed(() => this.session.usuario()?.rol ?? '');
  protected readonly puedeCrear = computed(() =>
    ['ADMINISTRADOR', 'LÍDER_DE_PROYECTO', 'DESARROLLADOR'].includes(this.rol()));
  protected readonly puedeGuardar = computed(() =>
    !!this.nuevaActTitulo().trim() &&
    this.nuevaActFaseId() !== null  &&
    this.nuevaActHistoriaId() !== null);

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

    this.actividades.set(ACTIVIDADES);
    this.equipo.set(EQUIPO);
    this.historiasProyecto.set(HISTORIAS_PROYECTO);
  }

  // ── Filtros ──
  protected setFiltroFase(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroFaseId.set(v ? Number(v) : null);
  }
  protected setFiltroEstado(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroEstado.set(v as EstadoActividad | '');
  }
  protected limpiarFiltros(): void {
    this.filtroBusqueda.set('');
    this.filtroFaseId.set(null);
    this.filtroEstado.set('');
  }

  // ── Modal ──
  protected setNuevaFase(event: Event): void {
    this.nuevaActFaseId.set(Number((event.target as HTMLSelectElement).value) || null);
    this.nuevaActHistoriaId.set(null);
  }
  protected setNuevaHistoria(event: Event): void {
    this.nuevaActHistoriaId.set(Number((event.target as HTMLSelectElement).value) || null);
  }
  protected crearActividad(): void {
    const titulo     = this.nuevaActTitulo().trim();
    const faseId     = this.nuevaActFaseId();
    const historiaId = this.nuevaActHistoriaId();
    if (!titulo || faseId === null || historiaId === null) return;

    const faseRef  = this.actividades().find(a => a.faseId === faseId);
    const historia = this.historiasProyecto().find(h => h.id === historiaId)!;
    const nextId   = Math.max(0, ...this.actividades().map(a => a.id)) + 1;

    this.actividades.update(as => [...as, {
      id: nextId,
      codigo: `ACT-${String(nextId).padStart(3, '0')}`,
      titulo, estado: 'POR_HACER',
      faseId,
      faseNombre: faseRef?.faseNombre ?? '',
      faseColor:  faseRef?.faseColor  ?? '#4361ee',
      historiaId, historiaCodigo: historia.codigo,
      asignadoId: this.session.usuario()?.id ?? null,
    }]);
    this.cerrarModal();
  }
  protected cerrarModal(): void {
    this.mostrarModal.set(false);
    this.nuevaActTitulo.set('');
    this.nuevaActFaseId.set(null);
    this.nuevaActHistoriaId.set(null);
  }

  // ── Navegación ──
  protected irADetalle(a: ActividadFila): void {
    this.router.navigate(['/pro', this.proyectoId(), 'fases', a.faseId, 'actividades', a.id]);
  }
  protected volver(): void {
    this.router.navigate(['/pro', this.proyectoId()]);
  }

  // ── Helpers ──
  protected getAsignado(id: number | null): MiembroEquipo | null {
    return id !== null ? (this.equipo().find(u => u.id === id) ?? null) : null;
  }
  protected estadoDot(estado: EstadoActividad): string { return ESTADO_DOT[estado]; }
}
