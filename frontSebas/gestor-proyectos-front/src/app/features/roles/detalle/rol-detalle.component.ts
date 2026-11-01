import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RolesService, Permiso, RolPermisoUpdateRequest } from '../roles.service';
import { RolItem } from '../../../core/models/usuario.model';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-rol-detalle',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './rol-detalle.component.html',
  styleUrl: './rol-detalle.component.scss',
})
export class RolDetalleComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly rolesSvc = inject(RolesService);
  private readonly toast = inject(ToastService);

  protected readonly rol = signal<RolItem | null>(null);
  protected readonly cargandoRol = signal(true);
  protected readonly tabActiva = signal<'info' | 'permisos'>('info');

  // ─── Permisos ─────────────────────────────────────────────────────────────
  protected readonly permisos = signal<Permiso[]>([]);
  protected readonly cargandoPermisos = signal(false);
  protected readonly errorPermisos = signal<string | null>(null);
  protected readonly busquedaPermiso = signal('');
  protected readonly pendienteAgregar = signal<Set<number>>(new Set());
  protected readonly pendienteQuitar = signal<Set<number>>(new Set());
  protected readonly guardando = signal(false);

  // ─── Computed ─────────────────────────────────────────────────────────────
  protected readonly permisosFiltrados = computed(() => {
    const term = this.busquedaPermiso().toLowerCase().trim();
    if (!term) return this.permisos();
    return this.permisos().filter(
      (p) =>
        p.nombre.toLowerCase().includes(term) ||
        p.path.toLowerCase().includes(term),
    );
  });

  protected readonly conteoActivos = computed(() => {
    const agregar = this.pendienteAgregar();
    const quitar = this.pendienteQuitar();
    return this.permisos().filter((p) =>
      agregar.has(p.id) ? true : quitar.has(p.id) ? false : p.activo,
    ).length;
  });

  protected readonly hayCambios = computed(
    () => this.pendienteAgregar().size > 0 || this.pendienteQuitar().size > 0,
  );

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    // Router state evita una llamada extra si se viene desde la tabla
    const state = history.state as { rol?: RolItem };
    if (state?.rol && state.rol.id === id) {
      this.rol.set(state.rol);
      this.cargandoRol.set(false);
    } else {
      this.rolesSvc.getAll().subscribe({
        next: (roles) => {
          const found = roles.find((r) => r.id === id);
          found ? this.rol.set(found) : this.router.navigate(['/roles']);
          this.cargandoRol.set(false);
        },
        error: () => this.router.navigate(['/roles']),
      });
    }

    this.cargarPermisos(id);
  }

  // ─── Tabs ─────────────────────────────────────────────────────────────────
  protected cambiarTab(tab: 'info' | 'permisos'): void {
    this.tabActiva.set(tab);
  }

  // ─── Permisos ─────────────────────────────────────────────────────────────
  protected estaActivo(permiso: Permiso): boolean {
    if (this.pendienteAgregar().has(permiso.id)) return true;
    if (this.pendienteQuitar().has(permiso.id)) return false;
    return permiso.activo;
  }

  protected togglePermiso(permiso: Permiso): void {
    const efectivo = this.estaActivo(permiso);
    const agregar = new Set(this.pendienteAgregar());
    const quitar = new Set(this.pendienteQuitar());

    if (efectivo) {
      permiso.activo ? quitar.add(permiso.id) : agregar.delete(permiso.id);
    } else {
      permiso.activo ? quitar.delete(permiso.id) : agregar.add(permiso.id);
    }

    this.pendienteAgregar.set(agregar);
    this.pendienteQuitar.set(quitar);
  }

  protected onBusqueda(event: Event): void {
    this.busquedaPermiso.set((event.target as HTMLInputElement).value);
  }

  protected guardar(): void {
    if (this.guardando() || !this.hayCambios()) return;
    const rol = this.rol();
    if (!rol) return;

    this.guardando.set(true);
    const agregarArr = [...this.pendienteAgregar()];
    const quitarArr = [...this.pendienteQuitar()];
    const agregarSet = new Set(agregarArr);
    const quitarSet = new Set(quitarArr);
    const body: RolPermisoUpdateRequest = { agregar: agregarArr, quitar: quitarArr };

    this.rolesSvc.updatePermisos(rol.id, body).subscribe({
      next: () => {
        this.guardando.set(false);
        this.permisos.update((list) =>
          list.map((p) => ({
            ...p,
            activo: agregarSet.has(p.id) ? true : quitarSet.has(p.id) ? false : p.activo,
          })),
        );
        this.pendienteAgregar.set(new Set());
        this.pendienteQuitar.set(new Set());
        this.toast.success('Permisos del rol actualizados correctamente.', 'Guardado');
      },
      error: () => {
        this.guardando.set(false);
        this.toast.error('No se pudieron guardar los cambios.', 'Error');
      },
    });
  }

  protected reintentarPermisos(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarPermisos(id);
  }

  private cargarPermisos(idRol: number): void {
    this.cargandoPermisos.set(true);
    this.errorPermisos.set(null);
    this.rolesSvc.getPermisos(idRol).subscribe({
      next: (data) => {
        this.permisos.set(data);
        this.cargandoPermisos.set(false);
      },
      error: () => {
        this.errorPermisos.set('No se pudieron cargar los permisos.');
        this.cargandoPermisos.set(false);
      },
    });
  }
}
