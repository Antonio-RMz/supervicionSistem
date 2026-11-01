import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../../../core/services/session.service';
import { ProyectoService } from '../../../../core/services/proyecto.service';
import { Fase, Historia, Prioridad } from '../../../../core/models/proyecto.model';
import { ListaItem } from '../../../../shared/models/lista-item.model';
import { ItemListaComponent } from '../../../../shared/components/item-lista/item-lista.component';
import { EstadoBadgeComponent } from '../../../../shared/components/estado-badge/estado-badge.component';

// ─── Interfaces locales ───────────────────────────────────────────────────────

interface FaseDetalle extends Fase {
  subtitulo: string;
  detallesTecnicos: string;
  tags: string[];
}

interface Archivo {
  id: number;
  nombre: string;
  extension: string;
  tamanio: string;
}

interface Comentario {
  id: number;
  autorNombre: string;
  autorIniciales: string;
  autorColor: string;
  fechaISO: string;
  texto: string;
}

// ─── Mocks ────────────────────────────────────────────────────────────────────

const PRIORIDAD_COLOR: Record<Prioridad, string> = {
  ALTA: '#dc2626', MEDIA: '#f59e0b', BAJA: '#10b981',
};

const MOCK_FASES: FaseDetalle[] = [
  {
    id: 1, numero: 1, nombre: 'Análisis y Levantamiento',
    subtitulo: 'Relevamiento de requerimientos y definición de alcance',
    descripcion: null,
    detallesTecnicos: `Se realizó el levantamiento de requerimientos funcionales y no funcionales con el cliente.\n\nSe definió la arquitectura de la solución basada en microservicios con Spring Boot en el backend y Angular 21 en el frontend. La base de datos elegida fue PostgreSQL 15.\n\nDecisiones clave:\n- Autenticación JWT con refresh token (7 días)\n- API RESTful con versionado en la URL (/api/v1)\n- Documentación de endpoints con Swagger/OpenAPI`,
    tags: ['Análisis', 'Documentación', 'Arquitectura'],
    color: '#4361ee', progreso: 100,
    historiasCompletadas: 4, historiasTotal: 4,
    actividadesCompletadas: 12, actividadesTotal: 12, estado: 'COMPLETADA',
  },
  {
    id: 2, numero: 2, nombre: 'Desarrollo',
    subtitulo: 'Implementación de backend y frontend',
    descripcion: null,
    detallesTecnicos: `Fase de desarrollo activa. Se trabaja en paralelo en backend (Spring Boot) y frontend (Angular 21).\n\nBackend: se implementaron los módulos de autenticación, proyectos y fases. Pendiente el módulo de notificaciones en tiempo real.\n\nFrontend: componentes standalone con signals. Actualmente en construcción la vista de detalle de fases.`,
    tags: ['Backend', 'Frontend', 'Spring Boot', 'Angular'],
    color: '#7209b7', progreso: 65,
    historiasCompletadas: 2, historiasTotal: 5,
    actividadesCompletadas: 9, actividadesTotal: 16, estado: 'EN_PROGRESO',
  },
  {
    id: 3, numero: 3, nombre: 'Pruebas',
    subtitulo: 'QA y pruebas de regresión',
    descripcion: null,
    detallesTecnicos: 'Fase de pruebas pendiente de iniciar.',
    tags: ['QA', 'Testing'],
    color: '#0d9488', progreso: 0,
    historiasCompletadas: 0, historiasTotal: 2,
    actividadesCompletadas: 0, actividadesTotal: 10, estado: 'PENDIENTE',
  },
  {
    id: 4, numero: 4, nombre: 'Implementación',
    subtitulo: 'Puesta en producción',
    descripcion: null,
    detallesTecnicos: 'Fase pendiente.',
    tags: ['DevOps', 'Deploy'],
    color: '#e67e22', progreso: 0,
    historiasCompletadas: 0, historiasTotal: 1,
    actividadesCompletadas: 0, actividadesTotal: 6, estado: 'PENDIENTE',
  },
];

const MOCK_HISTORIAS: Historia[] = [
  { id: 1,  codigo: 'NUC-101', titulo: 'Definir alcance y objetivos del sistema',            prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 1 },
  { id: 2,  codigo: 'NUC-102', titulo: 'Levantar requerimientos funcionales con el cliente', prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 1 },
  { id: 3,  codigo: 'NUC-103', titulo: 'Diseñar arquitectura de la solución',                prioridad: 'MEDIA', estado: 'COMPLETADA',  faseId: 1 },
  { id: 4,  codigo: 'NUC-104', titulo: 'Validar wireframes con el equipo de diseño',         prioridad: 'BAJA',  estado: 'COMPLETADA',  faseId: 1 },
  { id: 5,  codigo: 'NUC-201', titulo: 'Implementar autenticación JWT con refresh token',    prioridad: 'ALTA',  estado: 'COMPLETADA',  faseId: 2 },
  { id: 6,  codigo: 'NUC-202', titulo: 'Crear módulo de gestión de proyectos (CRUD)',        prioridad: 'ALTA',  estado: 'EN_PROGRESO', faseId: 2 },
  { id: 7,  codigo: 'NUC-203', titulo: 'Implementar API de fases e historias',               prioridad: 'ALTA',  estado: 'EN_PROGRESO', faseId: 2 },
  { id: 8,  codigo: 'NUC-204', titulo: 'Diseñar y crear base de datos relacional',           prioridad: 'MEDIA', estado: 'COMPLETADA',  faseId: 2 },
  { id: 9,  codigo: 'NUC-205', titulo: 'Integrar notificaciones en tiempo real',             prioridad: 'MEDIA', estado: 'PENDIENTE',   faseId: 2 },
  { id: 10, codigo: 'NUC-301', titulo: 'Pruebas unitarias de servicios backend',             prioridad: 'ALTA',  estado: 'PENDIENTE',   faseId: 3 },
  { id: 11, codigo: 'NUC-302', titulo: 'Pruebas de integración entre módulos',               prioridad: 'MEDIA', estado: 'PENDIENTE',   faseId: 3 },
  { id: 12, codigo: 'NUC-401', titulo: 'Preparar entorno de producción',                     prioridad: 'ALTA',  estado: 'PENDIENTE',   faseId: 4 },
];

const MOCK_ARCHIVOS: Record<number, Archivo[]> = {
  1: [
    { id: 1, nombre: 'Acta de requerimientos v1.2',  extension: 'pdf', tamanio: '248 KB' },
    { id: 2, nombre: 'Diagrama de arquitectura',      extension: 'png', tamanio: '1.4 MB' },
    { id: 3, nombre: 'Especificación funcional',      extension: 'docx', tamanio: '92 KB' },
  ],
  2: [
    { id: 4, nombre: 'Diagrama de base de datos',    extension: 'pdf', tamanio: '320 KB' },
    { id: 5, nombre: 'Colección de APIs Postman',    extension: 'zip', tamanio: '15 KB' },
  ],
};

const MOCK_COMENTARIOS: Record<number, Comentario[]> = {
  1: [
    { id: 1, autorNombre: 'Laura Gómez',  autorIniciales: 'LG', autorColor: '#7209b7', fechaISO: '2026-07-01T10:30:00', texto: 'Fase completada exitosamente. Los wireframes fueron aprobados por el cliente sin observaciones.' },
    { id: 2, autorNombre: 'Carlos Mendoza', autorIniciales: 'CM', autorColor: '#4361ee', fechaISO: '2026-07-03T14:15:00', texto: 'Documentación subida al repositorio. Se adjunta el acta de cierre de la fase.' },
  ],
  2: [
    { id: 3, autorNombre: 'Laura Gómez', autorIniciales: 'LG', autorColor: '#7209b7', fechaISO: '2026-07-08T09:00:00', texto: 'Bloqueados en la integración con el servicio de notificaciones. Esperamos respuesta del equipo de infraestructura.' },
  ],
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

const ICON_MAP: Record<string, string> = {
  pdf: 'bi-file-pdf-fill', png: 'bi-file-image-fill', jpg: 'bi-file-image-fill',
  jpeg: 'bi-file-image-fill', svg: 'bi-file-image-fill', doc: 'bi-file-word-fill',
  docx: 'bi-file-word-fill', xls: 'bi-file-excel-fill', xlsx: 'bi-file-excel-fill',
  zip: 'bi-file-zip-fill', rar: 'bi-file-zip-fill',
};

const COLOR_MAP: Record<string, string> = {
  pdf: '#dc2626', png: '#10b981', jpg: '#10b981', jpeg: '#10b981', svg: '#10b981',
  doc: '#2563eb', docx: '#2563eb', xls: '#16a34a', xlsx: '#16a34a',
  zip: '#f59e0b', rar: '#f59e0b',
};

// ─── Componente ───────────────────────────────────────────────────────────────

@Component({
  selector: 'app-fase-detalle',
  standalone: true,
  imports: [ItemListaComponent, EstadoBadgeComponent],
  templateUrl: './fase-detalle.component.html',
  styleUrl: './fase-detalle.component.scss',
})
export class FaseDetalleComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId = signal(0);
  protected readonly fase       = signal<FaseDetalle | null>(null);
  protected readonly tags       = signal<string[]>([]);
  protected readonly archivos   = signal<Archivo[]>([]);
  protected readonly comentarios = signal<Comentario[]>([]);

  // Estados de edición
  protected readonly editandoDesc  = signal(false);
  protected readonly descBorrador  = signal('');
  protected readonly nuevoTag      = signal('');
  protected readonly inputTagVisible = signal(false);
  protected readonly textoComentario = signal('');

  private readonly historias = signal<Historia[]>([]);

  protected readonly items = computed<ListaItem[]>(() =>
    this.historias().map(h => ({
      id: h.id, codigo: h.codigo, titulo: h.titulo,
      iconColor: PRIORIDAD_COLOR[h.prioridad],
    })),
  );

  private readonly rol = computed(() => this.session.usuario()?.rol ?? '');

  protected readonly puedeEditar = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO',
  );
  // Todos los roles con acceso al proyecto pueden subir archivos y comentar
  protected readonly puedeSubir    = computed(() => true);
  protected readonly puedeCommentar = computed(() => true);

  ngOnInit(): void {
    const id     = Number(this.route.snapshot.paramMap.get('id'));
    const faseId = Number(this.route.snapshot.paramMap.get('faseId'));
    if (!id || !faseId) { this.router.navigate(['/pro']); return; }

    this.proyectoId.set(id);

    const activo = this.session.proyectoActivo();
    if (!activo || activo.id !== id) {
      this.proyectoSvc.getById(id).subscribe({
        next:  p => this.session.seleccionarProyecto(p),
        error: () => this.router.navigate(['/pro']),
      });
    }

    const fase = MOCK_FASES.find(f => f.id === faseId) ?? null;
    this.fase.set(fase);
    if (fase) this.tags.set([...fase.tags]);

    this.historias.set(MOCK_HISTORIAS.filter(h => h.faseId === faseId));
    this.archivos.set(MOCK_ARCHIVOS[faseId] ?? []);
    this.comentarios.set(MOCK_COMENTARIOS[faseId] ?? []);
  }

  // ── Descripción ──
  protected iniciarEditDesc(): void {
    this.descBorrador.set(this.fase()?.detallesTecnicos ?? '');
    this.editandoDesc.set(true);
  }

  protected guardarDesc(): void {
    const f = this.fase();
    if (f) this.fase.set({ ...f, detallesTecnicos: this.descBorrador() });
    this.editandoDesc.set(false);
  }

  protected cancelarDesc(): void {
    this.editandoDesc.set(false);
  }

  // ── Tags ──
  protected eliminarTag(tag: string): void {
    this.tags.update(ts => ts.filter(t => t !== tag));
  }

  protected agregarTag(): void {
    const t = this.nuevoTag().trim();
    if (t && !this.tags().includes(t)) {
      this.tags.update(ts => [...ts, t]);
    }
    this.nuevoTag.set('');
    this.inputTagVisible.set(false);
  }

  protected onTagKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') { event.preventDefault(); this.agregarTag(); }
    if (event.key === 'Escape') { this.inputTagVisible.set(false); this.nuevoTag.set(''); }
  }

  // ── Comentarios ──
  protected enviarComentario(): void {
    const texto = this.textoComentario().trim();
    if (!texto) return;
    const usuario = this.session.usuario();
    const nuevo: Comentario = {
      id: Date.now(),
      autorNombre: usuario?.nombre ?? 'Yo',
      autorIniciales: (usuario?.nombre ?? 'Y').charAt(0).toUpperCase(),
      autorColor: '#4361ee',
      fechaISO: new Date().toISOString(),
      texto,
    };
    this.comentarios.update(cs => [...cs, nuevo]);
    this.textoComentario.set('');
  }

  protected onComentarioKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      this.enviarComentario();
    }
  }

  // ── Archivos ──
  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const ext = file.name.split('.').pop() ?? 'otro';
    const tamanio = file.size < 1024 * 1024
      ? `${(file.size / 1024).toFixed(0)} KB`
      : `${(file.size / 1024 / 1024).toFixed(1)} MB`;
    this.archivos.update(as => [...as, { id: Date.now(), nombre: file.name, extension: ext, tamanio }]);
    input.value = '';
  }

  protected eliminarArchivo(id: number): void {
    this.archivos.update(as => as.filter(a => a.id !== id));
  }

  // ── Helpers de vista ──
  protected iconoArchivo(ext: string): string {
    return ICON_MAP[ext.toLowerCase()] ?? 'bi-file-earmark-fill';
  }

  protected colorArchivo(ext: string): string {
    return COLOR_MAP[ext.toLowerCase()] ?? '#6c757d';
  }

  protected tiempoRelativo(fechaISO: string): string {
    const diff = Date.now() - new Date(fechaISO).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1)  return 'hace un momento';
    if (mins < 60) return `hace ${mins} min`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24)  return `hace ${hrs} h`;
    const days = Math.floor(hrs / 24);
    if (days < 30) return `hace ${days} día${days > 1 ? 's' : ''}`;
    return new Date(fechaISO).toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  protected irAHistoria(item: ListaItem): void {
    this.router.navigate(['/pro', this.proyectoId(), 'historias', item.id]);
  }

  protected volverAFases(): void {
    this.router.navigate(['/pro', this.proyectoId(), 'fases']);
  }
}
