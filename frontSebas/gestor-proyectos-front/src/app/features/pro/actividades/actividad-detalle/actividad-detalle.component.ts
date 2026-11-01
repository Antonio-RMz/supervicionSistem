import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService }   from '../../../../core/services/session.service';
import { ProyectoService }  from '../../../../core/services/proyecto.service';
import { EstadoBadgeComponent } from '../../../../shared/components/estado-badge/estado-badge.component';
import {
  Actividad, BitacoraEntrada, EstadoActividad, UsuarioInvolucrado,
} from '../../../../core/models/actividad.model';

// ─── Types ────────────────────────────────────────────────────────────────────

type Variante = 'primary' | 'success' | 'warning' | 'danger';

interface TransicionEstado {
  accion: string;
  estadoDestino: EstadoActividad;
  variante: Variante;
  requiereComentario?: true;
  requierePrUrl?: true;
}

interface Archivo {
  id: number; nombre: string; extension: string; tamanio: string;
}

interface Comentario {
  id: number; autorNombre: string; autorIniciales: string; autorColor: string;
  fechaISO: string; texto: string;
}

// ─── Constantes ───────────────────────────────────────────────────────────────

const ESTADO_LABEL: Record<EstadoActividad, string> = {
  POR_HACER:           'Por hacer',
  EN_DESARROLLO:       'En desarrollo',
  REVISION_QA:         'Revisión de QA',
  LISTO_PARA_INTEGRAR: 'Listo para integrar',
  PR_EN_REVISION:      'PR en revisión',
  INTEGRADO:           'Integrado',
};

const ROL_LABEL: Record<string, string> = {
  ANALISTA_COMERCIAL:   'Analista Comercial',
  'LÍDER_DE_PROYECTO':  'Líder de Proyecto',
  DESARROLLADOR:        'Desarrollador',
  TESTER:               'Tester',
  ADMINISTRADOR:        'Administrador',
};

// ─── Máquina de estados ───────────────────────────────────────────────────────

const TRANSICIONES: Partial<Record<EstadoActividad, Partial<Record<string, TransicionEstado[]>>>> = {
  POR_HACER: {
    DESARROLLADOR:       [{ accion: 'Iniciar actividad',      estadoDestino: 'EN_DESARROLLO', variante: 'primary' }],
    'LÍDER_DE_PROYECTO': [{ accion: 'Iniciar actividad',      estadoDestino: 'EN_DESARROLLO', variante: 'primary' }],
    ADMINISTRADOR:       [{ accion: 'Iniciar actividad',      estadoDestino: 'EN_DESARROLLO', variante: 'primary' }],
  },
  EN_DESARROLLO: {
    DESARROLLADOR:  [{ accion: 'Enviar a revisión QA', estadoDestino: 'REVISION_QA', variante: 'warning' }],
    ADMINISTRADOR:  [{ accion: 'Enviar a revisión QA', estadoDestino: 'REVISION_QA', variante: 'warning' }],
  },
  REVISION_QA: {
    TESTER: [
      { accion: 'Rechazar',   estadoDestino: 'EN_DESARROLLO',       variante: 'danger',  requiereComentario: true },
      { accion: 'Aprobar QA', estadoDestino: 'LISTO_PARA_INTEGRAR', variante: 'success' },
    ],
    ADMINISTRADOR: [
      { accion: 'Rechazar',   estadoDestino: 'EN_DESARROLLO',       variante: 'danger',  requiereComentario: true },
      { accion: 'Aprobar QA', estadoDestino: 'LISTO_PARA_INTEGRAR', variante: 'success' },
    ],
  },
  LISTO_PARA_INTEGRAR: {
    DESARROLLADOR:       [{ accion: 'Enviar a PR', estadoDestino: 'PR_EN_REVISION', variante: 'primary', requierePrUrl: true }],
    'LÍDER_DE_PROYECTO': [{ accion: 'Enviar a PR', estadoDestino: 'PR_EN_REVISION', variante: 'primary', requierePrUrl: true }],
    ADMINISTRADOR:       [{ accion: 'Enviar a PR', estadoDestino: 'PR_EN_REVISION', variante: 'primary', requierePrUrl: true }],
  },
  PR_EN_REVISION: {
    'LÍDER_DE_PROYECTO': [
      { accion: 'Rechazar PR',        estadoDestino: 'LISTO_PARA_INTEGRAR', variante: 'danger',  requiereComentario: true },
      { accion: 'Aprobar e integrar', estadoDestino: 'INTEGRADO',           variante: 'success' },
    ],
    ADMINISTRADOR: [
      { accion: 'Rechazar PR',        estadoDestino: 'LISTO_PARA_INTEGRAR', variante: 'danger',  requiereComentario: true },
      { accion: 'Aprobar e integrar', estadoDestino: 'INTEGRADO',           variante: 'success' },
    ],
  },
  INTEGRADO: {},
};

// ─── Mock data ────────────────────────────────────────────────────────────────

const MOCK_ACTIVIDADES: Actividad[] = [
  {
    id: 1, codigo: 'ACT-001',
    titulo: 'Implementar autenticación JWT con refresh token',
    descripcion: `Implementar el sistema de autenticación basado en JWT.\n\nRequisitos técnicos:\n- Access token: expiración 1 hora\n- Refresh token: expiración 7 días, almacenado en HttpOnly cookie\n- Interceptor Angular para adjuntar el Bearer token en cada petición\n- Manejo de 401 con renovación automática o redirect al login\n\nReferencia: RFC 7519 (JWT), OWASP Authentication Cheat Sheet.`,
    estado: 'EN_DESARROLLO', faseId: 2, faseNombre: 'Desarrollo', prUrl: null,
  },
  {
    id: 2, codigo: 'ACT-002',
    titulo: 'Crear endpoints CRUD de proyectos',
    descripcion: 'Implementar los endpoints REST para la gestión completa de proyectos (crear, leer, actualizar, eliminar). Incluir validaciones de permisos por rol.',
    estado: 'POR_HACER', faseId: 2, faseNombre: 'Desarrollo', prUrl: null,
  },
  {
    id: 3, codigo: 'ACT-003',
    titulo: 'Diseñar esquema de base de datos',
    descripcion: 'Diseño del modelo relacional con las entidades principales: Proyecto, Fase, Historia, Actividad, Usuario. Incluye script de migración con Liquibase.',
    estado: 'INTEGRADO', faseId: 2, faseNombre: 'Desarrollo', prUrl: 'https://github.com/org/repo/pull/42',
  },
  {
    id: 4, codigo: 'ACT-004',
    titulo: 'Configurar pipeline de CI/CD con GitHub Actions',
    descripcion: 'Configurar GitHub Actions para compilar, ejecutar tests y desplegar automáticamente en el entorno de staging tras cada merge a la rama develop.',
    estado: 'PR_EN_REVISION', faseId: 2, faseNombre: 'Desarrollo', prUrl: 'https://github.com/org/repo/pull/58',
  },
  {
    id: 5, codigo: 'ACT-005',
    titulo: 'Implementar módulo de notificaciones en tiempo real',
    descripcion: 'Integrar notificaciones en tiempo real usando WebSockets (STOMP sobre SockJS) para alertar sobre cambios de estado en actividades.',
    estado: 'LISTO_PARA_INTEGRAR', faseId: 2, faseNombre: 'Desarrollo', prUrl: null,
  },
  {
    id: 6, codigo: 'ACT-006',
    titulo: 'Pruebas de carga con JMeter',
    descripcion: 'Ejecutar pruebas de carga sobre los endpoints críticos para validar la capacidad bajo 500 usuarios concurrentes durante 10 minutos.',
    estado: 'REVISION_QA', faseId: 3, faseNombre: 'Pruebas', prUrl: null,
  },
];

const MOCK_USUARIOS: Record<number, UsuarioInvolucrado[]> = {
  1: [
    { id: 1, nombre: 'Jorge',   apellido: 'Sebastián', iniciales: 'JS', color: '#4361ee', rolProyecto: 'DESARROLLADOR' },
    { id: 2, nombre: 'Laura',   apellido: 'Gómez',     iniciales: 'LG', color: '#7209b7', rolProyecto: 'LÍDER_DE_PROYECTO' },
  ],
  3: [
    { id: 1, nombre: 'Jorge',   apellido: 'Sebastián', iniciales: 'JS', color: '#4361ee', rolProyecto: 'DESARROLLADOR' },
    { id: 2, nombre: 'Laura',   apellido: 'Gómez',     iniciales: 'LG', color: '#7209b7', rolProyecto: 'LÍDER_DE_PROYECTO' },
    { id: 3, nombre: 'Mariana', apellido: 'Torres',    iniciales: 'MT', color: '#0d9488', rolProyecto: 'TESTER' },
  ],
  4: [
    { id: 1, nombre: 'Jorge',   apellido: 'Sebastián', iniciales: 'JS', color: '#4361ee', rolProyecto: 'DESARROLLADOR' },
    { id: 2, nombre: 'Laura',   apellido: 'Gómez',     iniciales: 'LG', color: '#7209b7', rolProyecto: 'LÍDER_DE_PROYECTO' },
  ],
};

const MOCK_BITACORA: Record<number, BitacoraEntrada[]> = {
  1: [
    { id: 1, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'POR_HACER', estadoNuevo: 'EN_DESARROLLO', fechaISO: '2026-07-10T09:00:00' },
  ],
  3: [
    { id: 1, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'POR_HACER', estadoNuevo: 'EN_DESARROLLO', fechaISO: '2026-06-20T08:30:00' },
    { id: 2, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'EN_DESARROLLO', estadoNuevo: 'REVISION_QA', fechaISO: '2026-06-24T17:00:00' },
    { id: 3, usuarioNombre: 'Mariana Torres',  usuarioIniciales: 'MT', usuarioColor: '#0d9488',
      estadoAnterior: 'REVISION_QA', estadoNuevo: 'LISTO_PARA_INTEGRAR', fechaISO: '2026-06-25T11:00:00' },
    { id: 4, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'LISTO_PARA_INTEGRAR', estadoNuevo: 'PR_EN_REVISION', fechaISO: '2026-06-26T10:00:00', comentario: 'PR #42 abierto' },
    { id: 5, usuarioNombre: 'Laura Gómez',     usuarioIniciales: 'LG', usuarioColor: '#7209b7',
      estadoAnterior: 'PR_EN_REVISION', estadoNuevo: 'INTEGRADO', fechaISO: '2026-06-27T14:00:00' },
  ],
  4: [
    { id: 1, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'POR_HACER', estadoNuevo: 'EN_DESARROLLO', fechaISO: '2026-07-01T09:00:00' },
    { id: 2, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'EN_DESARROLLO', estadoNuevo: 'REVISION_QA', fechaISO: '2026-07-05T18:00:00' },
    { id: 3, usuarioNombre: 'Mariana Torres',  usuarioIniciales: 'MT', usuarioColor: '#0d9488',
      estadoAnterior: 'REVISION_QA', estadoNuevo: 'LISTO_PARA_INTEGRAR', fechaISO: '2026-07-07T10:00:00' },
    { id: 4, usuarioNombre: 'Jorge Sebastián', usuarioIniciales: 'JS', usuarioColor: '#4361ee',
      estadoAnterior: 'LISTO_PARA_INTEGRAR', estadoNuevo: 'PR_EN_REVISION', fechaISO: '2026-07-08T11:00:00', comentario: 'PR #58 enviado para revisión' },
  ],
};

const MOCK_ARCHIVOS: Record<number, Archivo[]> = {
  1: [
    { id: 1, nombre: 'Diagrama de flujo JWT',       extension: 'png', tamanio: '320 KB' },
    { id: 2, nombre: 'Especificación de seguridad', extension: 'pdf', tamanio: '156 KB' },
  ],
  3: [
    { id: 3, nombre: 'Modelo relacional v2.0',       extension: 'pdf', tamanio: '448 KB' },
    { id: 4, nombre: 'Script de migración inicial',  extension: 'sql', tamanio: '12 KB'  },
  ],
};

const MOCK_COMENTARIOS: Record<number, Comentario[]> = {
  1: [
    { id: 1, autorNombre: 'Laura Gómez',     autorIniciales: 'LG', autorColor: '#7209b7',
      fechaISO: '2026-07-10T10:00:00',
      texto: 'Recordar implementar el flujo de renovación del token antes de enviarlo a QA.' },
    { id: 2, autorNombre: 'Jorge Sebastián', autorIniciales: 'JS', autorColor: '#4361ee',
      fechaISO: '2026-07-11T09:30:00',
      texto: 'Confirmado. El interceptor ya detecta el 401; falta el flujo automático de renovación.' },
  ],
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

const ICON_MAP: Record<string, string> = {
  pdf:  'bi-file-pdf-fill',   png:  'bi-file-image-fill',
  jpg:  'bi-file-image-fill', jpeg: 'bi-file-image-fill',
  svg:  'bi-file-image-fill', doc:  'bi-file-word-fill',
  docx: 'bi-file-word-fill',  xls:  'bi-file-excel-fill',
  xlsx: 'bi-file-excel-fill', zip:  'bi-file-zip-fill',
  rar:  'bi-file-zip-fill',   sql:  'bi-file-code-fill',
  ts:   'bi-file-code-fill',  js:   'bi-file-code-fill',
};

const COLOR_MAP: Record<string, string> = {
  pdf:  '#dc2626', png:  '#10b981', jpg:  '#10b981', jpeg: '#10b981',
  svg:  '#10b981', doc:  '#2563eb', docx: '#2563eb', xls:  '#16a34a',
  xlsx: '#16a34a', zip:  '#f59e0b', rar:  '#f59e0b', sql:  '#6366f1',
  ts:   '#3b82f6', js:   '#eab308',
};

// ─── Componente ───────────────────────────────────────────────────────────────

@Component({
  selector: 'app-actividad-detalle',
  standalone: true,
  imports: [EstadoBadgeComponent],
  templateUrl: './actividad-detalle.component.html',
  styleUrl:    './actividad-detalle.component.scss',
})
export class ActividadDetalleComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId = signal(0);
  protected readonly faseId     = signal(0);
  protected readonly actividad  = signal<Actividad | null>(null);
  protected readonly tags       = signal<string[]>([]);
  protected readonly archivos   = signal<Archivo[]>([]);
  protected readonly comentarios = signal<Comentario[]>([]);
  protected readonly usuarios   = signal<UsuarioInvolucrado[]>([]);
  protected readonly bitacora   = signal<BitacoraEntrada[]>([]);

  // Descripción
  protected readonly editandoDesc  = signal(false);
  protected readonly descBorrador  = signal('');

  // Tags
  protected readonly nuevoTag        = signal('');
  protected readonly inputTagVisible = signal(false);

  // Comentarios
  protected readonly textoComentario = signal('');

  // Máquina de estados
  protected readonly transicionPendiente = signal<TransicionEstado | null>(null);
  protected readonly motivoRechazo       = signal('');
  protected readonly prUrlDraft          = signal('');

  private readonly rol = computed(() => this.session.usuario()?.rol ?? '');

  protected readonly transicionesDisponibles = computed<TransicionEstado[]>(() => {
    const act = this.actividad();
    if (!act) return [];
    return TRANSICIONES[act.estado]?.[this.rol()] ?? [];
  });

  protected readonly puedeConfirmar = computed(() => {
    const t = this.transicionPendiente();
    if (!t) return false;
    if (t.requiereComentario && !this.motivoRechazo().trim()) return false;
    if (t.requierePrUrl && !this.prUrlDraft().trim()) return false;
    return true;
  });

  protected readonly puedeEditar    = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO',
  );
  protected readonly puedeSubir     = computed(() => true);
  protected readonly puedeCommentar = computed(() => true);

  protected estadoLabel(e: EstadoActividad): string { return ESTADO_LABEL[e] ?? e; }
  protected rolLabel(r: string):             string { return ROL_LABEL[r] ?? r; }

  ngOnInit(): void {
    const id          = Number(this.route.snapshot.paramMap.get('id'));
    const faseId      = Number(this.route.snapshot.paramMap.get('faseId'));
    const actividadId = Number(this.route.snapshot.paramMap.get('actividadId'));

    if (!id || !faseId || !actividadId) { this.router.navigate(['/pro']); return; }

    this.proyectoId.set(id);
    this.faseId.set(faseId);

    const activo = this.session.proyectoActivo();
    if (!activo || activo.id !== id) {
      this.proyectoSvc.getById(id).subscribe({
        next:  p => this.session.seleccionarProyecto(p),
        error: () => this.router.navigate(['/pro']),
      });
    }

    const act = MOCK_ACTIVIDADES.find(a => a.id === actividadId) ?? null;
    this.actividad.set(act);
    if (act) this.tags.set(['Backend', 'Spring Boot']);

    this.usuarios.set(MOCK_USUARIOS[actividadId]   ?? []);
    this.bitacora.set(MOCK_BITACORA[actividadId]   ?? []);
    this.archivos.set(MOCK_ARCHIVOS[actividadId]   ?? []);
    this.comentarios.set(MOCK_COMENTARIOS[actividadId] ?? []);
  }

  // ── Descripción ──
  protected iniciarEditDesc(): void {
    this.descBorrador.set(this.actividad()?.descripcion ?? '');
    this.editandoDesc.set(true);
  }
  protected guardarDesc(): void {
    const a = this.actividad();
    if (a) this.actividad.set({ ...a, descripcion: this.descBorrador() });
    this.editandoDesc.set(false);
  }
  protected cancelarDesc(): void { this.editandoDesc.set(false); }

  // ── Tags ──
  protected eliminarTag(tag: string): void {
    this.tags.update(ts => ts.filter(t => t !== tag));
  }
  protected agregarTag(): void {
    const t = this.nuevoTag().trim();
    if (t && !this.tags().includes(t)) this.tags.update(ts => [...ts, t]);
    this.nuevoTag.set('');
    this.inputTagVisible.set(false);
  }
  protected onTagKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter')  { event.preventDefault(); this.agregarTag(); }
    if (event.key === 'Escape') { this.inputTagVisible.set(false); this.nuevoTag.set(''); }
  }

  // ── Máquina de estados ──
  protected iniciarTransicion(t: TransicionEstado): void {
    if (t.requiereComentario || t.requierePrUrl) {
      this.motivoRechazo.set('');
      this.prUrlDraft.set('');
      this.transicionPendiente.set(t);
    } else {
      this.aplicarTransicion(t, '', '');
    }
  }
  protected confirmarTransicion(): void {
    const t = this.transicionPendiente();
    if (!t || !this.puedeConfirmar()) return;
    this.aplicarTransicion(t, this.motivoRechazo(), this.prUrlDraft());
    this.transicionPendiente.set(null);
  }
  protected cancelarTransicion(): void {
    this.transicionPendiente.set(null);
    this.motivoRechazo.set('');
    this.prUrlDraft.set('');
  }

  private aplicarTransicion(t: TransicionEstado, motivo: string, prUrl: string): void {
    const a = this.actividad();
    if (!a) return;
    const u = this.session.usuario();
    const nombre   = u ? `${u.nombre} ${u.apellido ?? ''}`.trim() : 'Sistema';
    const iniciales = u
      ? (u.nombre.charAt(0) + (u.apellido?.charAt(0) ?? '')).toUpperCase()
      : 'SI';

    const entrada: BitacoraEntrada = {
      id: Date.now(),
      usuarioNombre:    nombre,
      usuarioIniciales: iniciales,
      usuarioColor:     '#4361ee',
      estadoAnterior:   a.estado,
      estadoNuevo:      t.estadoDestino,
      fechaISO:         new Date().toISOString(),
      ...(motivo.trim() ? { comentario: motivo.trim() } : {}),
    };
    this.bitacora.update(b => [...b, entrada]);
    this.actividad.set({ ...a, estado: t.estadoDestino, prUrl: prUrl.trim() || a.prUrl });
  }

  // ── Comentarios ──
  protected enviarComentario(): void {
    const texto = this.textoComentario().trim();
    if (!texto) return;
    const u = this.session.usuario();
    const nuevo: Comentario = {
      id: Date.now(),
      autorNombre:    u ? `${u.nombre} ${u.apellido ?? ''}`.trim() : 'Yo',
      autorIniciales: u ? (u.nombre.charAt(0) + (u.apellido?.charAt(0) ?? '')).toUpperCase() : 'YO',
      autorColor:     '#4361ee',
      fechaISO:       new Date().toISOString(),
      texto,
    };
    this.comentarios.update(cs => [...cs, nuevo]);
    this.textoComentario.set('');
  }
  protected onComentarioKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) this.enviarComentario();
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
  protected iconoArchivo(ext: string): string { return ICON_MAP[ext.toLowerCase()] ?? 'bi-file-earmark-fill'; }
  protected colorArchivo(ext: string): string { return COLOR_MAP[ext.toLowerCase()] ?? '#6c757d'; }

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

  protected volverAFase(): void {
    this.router.navigate(['/pro', this.proyectoId(), 'fases', this.faseId()]);
  }
}
