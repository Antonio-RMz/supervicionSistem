import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { ProyectoService } from '../../core/services/proyecto.service';
import { ProyectoRequest, ProyectoResponse } from '../../core/models/proyecto.model';

@Component({
  selector: 'app-pro',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './pro.component.html',
  styleUrl: './pro.component.scss',
})
export class ProComponent implements OnInit, OnDestroy {
  protected readonly session = inject(SessionService);
  private readonly proyectoSvc = inject(ProyectoService);
  private readonly router = inject(Router);

  protected readonly proyectos = signal<ProyectoResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly menuAbiertoId = signal<number | null>(null);
  protected readonly eliminandoId = signal<number | null>(null);
  protected readonly mostrarModal = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly guardando = signal(false);

  protected form: ProyectoRequest = this.formVacio();

  protected readonly esAdmin = computed(
    () => this.session.usuario()?.rol === 'ADMINISTRADOR',
  );
  protected readonly proyectoActivoId = computed(
    () => this.session.proyectoActivo()?.id ?? null,
  );
  protected readonly esEdicion = computed(() => this.editandoId() !== null);

  private readonly onClickOutside = (e: MouseEvent): void => {
    if (!(e.target as HTMLElement).closest('.menu-contextual')) {
      this.menuAbiertoId.set(null);
    }
  };

  ngOnInit(): void {
    // Entrar a la lista de proyectos siempre limpia el contexto de proyecto activo.
    // Garantiza que el sidebar no quede "pegado" al volver desde un submódulo via URL.
    this.session.deseleccionarProyecto();
    this.cargar();
    document.addEventListener('click', this.onClickOutside);
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.onClickOutside);
  }

  // ─── Estado ──────────────────────────────────────────────────────────────────

  protected estadoClass(estado: string): string {
    const map: Record<string, string> = {
      'ACTIVO':      'estado-activo',
      'EN_PROGRESO': 'estado-progreso',
      'EN_PAUSA':    'estado-pausa',
      'CERRADO':     'estado-cerrado',
      'PENDIENTE':   'estado-pendiente',
      'CANCELADO':   'estado-cerrado',
    };
    return map[estado] ?? 'estado-activo';
  }

  protected estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      'ACTIVO':      'Activo',
      'EN_PROGRESO': 'En progreso',
      'EN_PAUSA':    'En pausa',
      'CERRADO':     'Cerrado',
      'PENDIENTE':   'Pendiente',
      'CANCELADO':   'Cancelado',
    };
    return map[estado] ?? estado;
  }

  // ─── Selección de proyecto (sin navegación — ya estamos en /pro) ──────────────

  protected reintentar(): void {
    this.cargar();
  }

  protected seleccionar(proyecto: ProyectoResponse): void {
    this.router.navigate(['/pro', proyecto.id]);
  }

  // ─── Menú contextual ─────────────────────────────────────────────────────────

  protected toggleMenu(id: number, event: Event): void {
    event.stopPropagation();
    this.menuAbiertoId.update((cur) => (cur === id ? null : id));
  }

  // ─── Modal crear / editar ────────────────────────────────────────────────────

  protected abrirModalCrear(event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/pro/nuevo']);
  }

  protected abrirModalEditar(proyecto: ProyectoResponse, event: Event): void {
    event.stopPropagation();
    this.menuAbiertoId.set(null);
    this.editandoId.set(proyecto.id);
    this.form = {
      nombre:      proyecto.nombre,
      descripcion: proyecto.descripcion,
      progreso:    proyecto.progreso,
      estado:      proyecto.estado,
      categoria:   proyecto.categoria,
    };
    this.mostrarModal.set(true);
  }

  protected cerrarModal(): void {
    if (this.guardando()) return;
    this.mostrarModal.set(false);
    this.editandoId.set(null);
  }

  protected guardar(): void {
    if (this.guardando()) return;
    this.guardando.set(true);

    const id = this.editandoId();
    const obs = id
      ? this.proyectoSvc.update(id, this.form)
      : this.proyectoSvc.create(this.form);

    obs.subscribe({
      next: (p) => {
        this.proyectos.update((list) =>
          id ? list.map((x) => (x.id === id ? p : x)) : [...list, p],
        );
        this.guardando.set(false);
        this.cerrarModal();
      },
      error: () => this.guardando.set(false),
    });
  }

  // ─── Eliminar ────────────────────────────────────────────────────────────────

  protected confirmarEliminar(id: number, event: Event): void {
    event.stopPropagation();
    this.menuAbiertoId.set(null);
    this.eliminandoId.set(id);
  }

  protected cancelarEliminar(): void {
    this.eliminandoId.set(null);
  }

  protected eliminar(id: number): void {
    this.proyectoSvc.delete(id).subscribe({
      next: () => {
        this.proyectos.update((list) => list.filter((p) => p.id !== id));
        if (this.proyectoActivoId() === id) this.session.deseleccionarProyecto();
        this.eliminandoId.set(null);
      },
      error: () => this.eliminandoId.set(null),
    });
  }

  // ─── Carga ───────────────────────────────────────────────────────────────────

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.proyectoSvc.getAll().subscribe({
      next: (data) => { this.proyectos.set(data); this.loading.set(false); },
      error: () => { this.error.set('No se pudieron cargar los proyectos.'); this.loading.set(false); },
    });
  }

  private formVacio(): ProyectoRequest {
    return { nombre: '', descripcion: null, progreso: 0, estado: 'ACTIVO', categoria: null };
  }
}
