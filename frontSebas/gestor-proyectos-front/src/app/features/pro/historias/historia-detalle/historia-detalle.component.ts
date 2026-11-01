import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService }   from '../../../../core/services/session.service';
import { ProyectoService }  from '../../../../core/services/proyecto.service';
import { EstadoBadgeComponent } from '../../../../shared/components/estado-badge/estado-badge.component';
import { ItemListaComponent }   from '../../../../shared/components/item-lista/item-lista.component';
import { ListaItem } from '../../../../shared/models/lista-item.model';

// ─── Interfaces locales ───────────────────────────────────────────────────────

type Prioridad = 'ALTA' | 'MEDIA' | 'BAJA';

interface HistoriaDetalle {
  id: number;
  codigo: string;
  titulo: string;
  subtitulo: string;
  descripcion: string;
  prioridad: Prioridad;
  estado: string;
  faseId: number;
  faseNombre: string;
  puntos: number;
  tags: string[];
}

interface ActividadResumen {
  id: number;
  codigo: string;
  titulo: string;
  estado: string;
  faseId: number;
}

interface Archivo {
  id: number; nombre: string; extension: string; tamanio: string;
}

interface Comentario {
  id: number; autorNombre: string; autorIniciales: string; autorColor: string;
  fechaISO: string; texto: string;
}

// ─── Mock data ────────────────────────────────────────────────────────────────

const PRIORIDAD_COLOR: Record<Prioridad, string> = {
  ALTA: '#dc2626', MEDIA: '#f59e0b', BAJA: '#10b981',
};

const MOCK_HISTORIAS: HistoriaDetalle[] = [
  {
    id: 1, codigo: 'NUC-101', faseId: 1, faseNombre: 'Análisis y Levantamiento',
    titulo: 'Definir alcance y objetivos del sistema', subtitulo: 'Historia de usuario — Análisis',
    descripcion: `Como equipo de proyecto, necesito tener un documento claro que defina el alcance del sistema, los objetivos del negocio y los criterios de éxito.\n\nCriterios de aceptación:\n- Documento de alcance aprobado por el cliente\n- Listado de módulos incluidos y excluidos\n- Hitos y entregables definidos con fechas tentativas`,
    prioridad: 'ALTA', estado: 'COMPLETADA', puntos: 3,
    tags: ['Análisis', 'Documentación'],
  },
  {
    id: 2, codigo: 'NUC-102', faseId: 1, faseNombre: 'Análisis y Levantamiento',
    titulo: 'Levantar requerimientos funcionales con el cliente', subtitulo: 'Historia de usuario — Análisis',
    descripcion: `Como analista, necesito realizar sesiones de levantamiento con el cliente para documentar todos los requerimientos funcionales del sistema.\n\nCriterios de aceptación:\n- Al menos 3 sesiones de levantamiento realizadas\n- Requerimientos validados y firmados por el cliente\n- Casos de uso documentados para cada módulo principal`,
    prioridad: 'ALTA', estado: 'COMPLETADA', puntos: 5,
    tags: ['Análisis', 'Cliente'],
  },
  {
    id: 3, codigo: 'NUC-103', faseId: 1, faseNombre: 'Análisis y Levantamiento',
    titulo: 'Diseñar arquitectura de la solución', subtitulo: 'Historia de usuario — Arquitectura',
    descripcion: `Como arquitecto de software, necesito diseñar la arquitectura técnica de la solución considerando escalabilidad, seguridad y mantenibilidad.\n\nCriterios de aceptación:\n- Diagrama de arquitectura validado por el equipo técnico\n- Stack tecnológico definido y documentado\n- Estrategia de deploy y CI/CD definida`,
    prioridad: 'MEDIA', estado: 'COMPLETADA', puntos: 5,
    tags: ['Arquitectura', 'Backend', 'Frontend'],
  },
  {
    id: 4, codigo: 'NUC-104', faseId: 1, faseNombre: 'Análisis y Levantamiento',
    titulo: 'Validar wireframes con el equipo de diseño', subtitulo: 'Historia de usuario — UX/UI',
    descripcion: `Como diseñador UX, necesito crear y validar los wireframes de las pantallas principales del sistema con el equipo y el cliente.\n\nCriterios de aceptación:\n- Wireframes de todas las pantallas principales creados\n- Validados en revisión con el equipo de desarrollo\n- Aprobados por el cliente`,
    prioridad: 'BAJA', estado: 'COMPLETADA', puntos: 3,
    tags: ['UX', 'Diseño'],
  },
  {
    id: 5, codigo: 'NUC-201', faseId: 2, faseNombre: 'Desarrollo',
    titulo: 'Implementar autenticación JWT con refresh token', subtitulo: 'Historia de usuario — Seguridad',
    descripcion: `Como usuario del sistema, necesito poder iniciar sesión con mis credenciales y mantener mi sesión activa de forma segura.\n\nCriterios de aceptación:\n- Login con usuario y contraseña\n- JWT de acceso con expiración de 1 hora\n- Refresh token con expiración de 7 días\n- Logout que invalida el token\n- Manejo de sesión expirada con redirección al login`,
    prioridad: 'ALTA', estado: 'COMPLETADA', puntos: 5,
    tags: ['Seguridad', 'Backend', 'Frontend'],
  },
  {
    id: 6, codigo: 'NUC-202', faseId: 2, faseNombre: 'Desarrollo',
    titulo: 'Crear módulo de gestión de proyectos (CRUD)', subtitulo: 'Historia de usuario — Proyectos',
    descripcion: `Como líder de proyecto, necesito poder crear, ver, editar y eliminar proyectos con toda su información relevante.\n\nCriterios de aceptación:\n- Formulario de creación con validaciones\n- Listado paginado de proyectos\n- Edición de todos los campos\n- Eliminación con confirmación\n- Control de acceso por rol`,
    prioridad: 'ALTA', estado: 'EN_PROGRESO', puntos: 8,
    tags: ['Backend', 'Frontend', 'CRUD'],
  },
  {
    id: 7, codigo: 'NUC-203', faseId: 2, faseNombre: 'Desarrollo',
    titulo: 'Implementar API de fases e historias', subtitulo: 'Historia de usuario — Fases',
    descripcion: `Como desarrollador, necesito endpoints REST para gestionar fases e historias de usuario de un proyecto.\n\nCriterios de aceptación:\n- CRUD completo de fases por proyecto\n- CRUD completo de historias por fase\n- Relaciones y validaciones correctas\n- Documentación Swagger actualizada`,
    prioridad: 'ALTA', estado: 'EN_PROGRESO', puntos: 8,
    tags: ['Backend', 'API', 'REST'],
  },
  {
    id: 8, codigo: 'NUC-204', faseId: 2, faseNombre: 'Desarrollo',
    titulo: 'Diseñar y crear base de datos relacional', subtitulo: 'Historia de usuario — Base de datos',
    descripcion: `Como arquitecto de datos, necesito diseñar e implementar el esquema de base de datos que soporte todas las entidades del sistema.\n\nCriterios de aceptación:\n- Modelo ER aprobado\n- Script de migración inicial con Liquibase\n- Índices optimizados en columnas de búsqueda frecuente\n- Datos de prueba para desarrollo`,
    prioridad: 'MEDIA', estado: 'COMPLETADA', puntos: 5,
    tags: ['Base de datos', 'PostgreSQL'],
  },
  {
    id: 9, codigo: 'NUC-205', faseId: 2, faseNombre: 'Desarrollo',
    titulo: 'Integrar notificaciones en tiempo real', subtitulo: 'Historia de usuario — Notificaciones',
    descripcion: `Como usuario, necesito recibir notificaciones en tiempo real cuando ocurran cambios importantes en los proyectos que sigo.\n\nCriterios de aceptación:\n- WebSocket activo durante la sesión\n- Notificaciones de cambio de estado de actividades\n- Badge de contador en el header\n- Marcar como leído individualmente o en grupo`,
    prioridad: 'MEDIA', estado: 'PENDIENTE', puntos: 8,
    tags: ['WebSocket', 'Backend', 'Frontend'],
  },
  {
    id: 10, codigo: 'NUC-301', faseId: 3, faseNombre: 'Pruebas',
    titulo: 'Pruebas unitarias de servicios backend', subtitulo: 'Historia de usuario — QA',
    descripcion: `Como desarrollador, necesito escribir pruebas unitarias para todos los servicios del backend con cobertura mínima del 80%.\n\nCriterios de aceptación:\n- Cobertura >= 80% en servicios\n- Tests en módulos de auth, proyectos, fases e historias\n- Pipeline de CI ejecuta tests en cada PR`,
    prioridad: 'ALTA', estado: 'PENDIENTE', puntos: 5,
    tags: ['Testing', 'JUnit', 'Mockito'],
  },
  {
    id: 11, codigo: 'NUC-302', faseId: 3, faseNombre: 'Pruebas',
    titulo: 'Pruebas de integración entre módulos', subtitulo: 'Historia de usuario — QA',
    descripcion: `Como tester, necesito ejecutar pruebas de integración que validen la correcta comunicación entre los módulos del sistema.\n\nCriterios de aceptación:\n- Tests de integración para los flujos críticos\n- Validar auth → proyectos → fases → historias\n- Reporte de resultados en formato JUnit XML`,
    prioridad: 'MEDIA', estado: 'PENDIENTE', puntos: 5,
    tags: ['Testing', 'Integración'],
  },
  {
    id: 12, codigo: 'NUC-401', faseId: 4, faseNombre: 'Implementación',
    titulo: 'Preparar entorno de producción', subtitulo: 'Historia de usuario — DevOps',
    descripcion: `Como DevOps, necesito configurar y aprovisionar el entorno de producción para el despliegue del sistema.\n\nCriterios de aceptación:\n- Servidor configurado con Docker\n- Variables de entorno de producción definidas\n- Certificado SSL instalado\n- Pipeline de deploy a producción funcional\n- Monitoreo básico activo`,
    prioridad: 'ALTA', estado: 'PENDIENTE', puntos: 8,
    tags: ['DevOps', 'Docker', 'Deploy'],
  },
];

const MOCK_ACTIVIDADES: Record<number, ActividadResumen[]> = {
  5: [
    { id: 1, codigo: 'ACT-001', titulo: 'Implementar autenticación JWT con refresh token', estado: 'EN_DESARROLLO', faseId: 2 },
  ],
  6: [
    { id: 2, codigo: 'ACT-002', titulo: 'Crear endpoints CRUD de proyectos', estado: 'POR_HACER', faseId: 2 },
  ],
  7: [
    { id: 5, codigo: 'ACT-005', titulo: 'Implementar módulo de notificaciones en tiempo real', estado: 'LISTO_PARA_INTEGRAR', faseId: 2 },
  ],
  8: [
    { id: 3, codigo: 'ACT-003', titulo: 'Diseñar esquema de base de datos', estado: 'INTEGRADO', faseId: 2 },
    { id: 4, codigo: 'ACT-004', titulo: 'Configurar pipeline de CI/CD con GitHub Actions', estado: 'PR_EN_REVISION', faseId: 2 },
  ],
};

const MOCK_ARCHIVOS: Record<number, Archivo[]> = {
  5: [
    { id: 1, nombre: 'Especificación de seguridad JWT', extension: 'pdf', tamanio: '156 KB' },
  ],
  3: [
    { id: 2, nombre: 'Diagrama de arquitectura v2', extension: 'png', tamanio: '1.4 MB' },
    { id: 3, nombre: 'Documento de arquitectura', extension: 'docx', tamanio: '92 KB' },
  ],
};

const MOCK_COMENTARIOS: Record<number, Comentario[]> = {
  6: [
    { id: 1, autorNombre: 'Laura Gómez', autorIniciales: 'LG', autorColor: '#7209b7',
      fechaISO: '2026-07-05T10:00:00',
      texto: 'Recordar implementar paginación en el listado. El cliente mencionó que habrá muchos proyectos.' },
  ],
  9: [
    { id: 2, autorNombre: 'Jorge Sebastián', autorIniciales: 'JS', autorColor: '#4361ee',
      fechaISO: '2026-07-08T09:00:00',
      texto: 'Bloqueado esperando que infra apruebe la apertura del puerto WebSocket en producción.' },
  ],
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

const ICON_MAP: Record<string, string> = {
  pdf:  'bi-file-pdf-fill',   png:  'bi-file-image-fill',
  jpg:  'bi-file-image-fill', docx: 'bi-file-word-fill',
  doc:  'bi-file-word-fill',  xlsx: 'bi-file-excel-fill',
  zip:  'bi-file-zip-fill',   sql:  'bi-file-code-fill',
};
const COLOR_MAP: Record<string, string> = {
  pdf: '#dc2626', png: '#10b981', jpg: '#10b981', docx: '#2563eb',
  doc: '#2563eb', xlsx: '#16a34a', zip: '#f59e0b', sql: '#6366f1',
};

// Estado de actividad → color del icono en la lista
const ACT_ESTADO_COLOR: Record<string, string> = {
  POR_HACER:           '#9ca3af',
  EN_DESARROLLO:       '#3b82f6',
  REVISION_QA:         '#f59e0b',
  LISTO_PARA_INTEGRAR: '#8b5cf6',
  PR_EN_REVISION:      '#a855f7',
  INTEGRADO:           '#10b981',
};

// ─── Componente ───────────────────────────────────────────────────────────────

@Component({
  selector: 'app-historia-detalle',
  standalone: true,
  imports: [EstadoBadgeComponent, ItemListaComponent],
  templateUrl: './historia-detalle.component.html',
  styleUrl:    './historia-detalle.component.scss',
})
export class HistoriaDetalleComponent implements OnInit {
  protected readonly session     = inject(SessionService);
  private  readonly proyectoSvc = inject(ProyectoService);
  private  readonly route       = inject(ActivatedRoute);
  private  readonly router      = inject(Router);

  protected readonly proyectoId  = signal(0);
  protected readonly historia    = signal<HistoriaDetalle | null>(null);
  protected readonly tags        = signal<string[]>([]);
  protected readonly archivos    = signal<Archivo[]>([]);
  protected readonly comentarios = signal<Comentario[]>([]);

  // Descripción
  protected readonly editandoDesc  = signal(false);
  protected readonly descBorrador  = signal('');

  // Tags
  protected readonly nuevoTag        = signal('');
  protected readonly inputTagVisible = signal(false);

  // Comentarios
  protected readonly textoComentario = signal('');

  private readonly actividadesRaw = signal<ActividadResumen[]>([]);

  protected readonly actividadItems = computed<ListaItem[]>(() =>
    this.actividadesRaw().map(a => ({
      id: a.id, codigo: a.codigo, titulo: a.titulo,
      iconColor: ACT_ESTADO_COLOR[a.estado] ?? '#9ca3af',
    })),
  );

  private readonly rol = computed(() => this.session.usuario()?.rol ?? '');

  protected readonly puedeEditar    = computed(() =>
    this.rol() === 'ADMINISTRADOR' || this.rol() === 'LÍDER_DE_PROYECTO',
  );
  protected readonly puedeSubir     = computed(() => true);
  protected readonly puedeCommentar = computed(() => true);

  protected prioridadColor(p: Prioridad): string { return PRIORIDAD_COLOR[p]; }
  protected prioridadLabel(p: Prioridad): string {
    return { ALTA: 'Alta', MEDIA: 'Media', BAJA: 'Baja' }[p];
  }

  ngOnInit(): void {
    const id         = Number(this.route.snapshot.paramMap.get('id'));
    const historiaId = Number(this.route.snapshot.paramMap.get('historiaId'));
    if (!id || !historiaId) { this.router.navigate(['/pro']); return; }
    this.proyectoId.set(id);

    const activo = this.session.proyectoActivo();
    if (!activo || activo.id !== id) {
      this.proyectoSvc.getById(id).subscribe({
        next:  p => this.session.seleccionarProyecto(p),
        error: () => this.router.navigate(['/pro']),
      });
    }

    const h = MOCK_HISTORIAS.find(x => x.id === historiaId) ?? null;
    this.historia.set(h);
    if (h) this.tags.set([...h.tags]);

    this.actividadesRaw.set(MOCK_ACTIVIDADES[historiaId] ?? []);
    this.archivos.set(MOCK_ARCHIVOS[historiaId] ?? []);
    this.comentarios.set(MOCK_COMENTARIOS[historiaId] ?? []);
  }

  // ── Descripción ──
  protected iniciarEditDesc(): void {
    this.descBorrador.set(this.historia()?.descripcion ?? '');
    this.editandoDesc.set(true);
  }
  protected guardarDesc(): void {
    const h = this.historia();
    if (h) this.historia.set({ ...h, descripcion: this.descBorrador() });
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

  // ── Navegación ──
  protected irAActividad(item: ListaItem): void {
    const act = this.actividadesRaw().find(a => a.id === item.id);
    if (!act) return;
    this.router.navigate(['/pro', this.proyectoId(), 'fases', act.faseId, 'actividades', act.id]);
  }
  protected volverAHistorias(): void {
    this.router.navigate(['/pro', this.proyectoId(), 'historias']);
  }

  // ── Helpers ──
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
}
