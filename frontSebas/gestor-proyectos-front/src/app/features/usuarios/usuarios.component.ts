import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../core/services/usuario.service';
import { ToastService } from '../../shared/services/toast.service';
import { RolItem, UsuarioListItem, UsuarioRequest } from '../../core/models/usuario.model';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent implements OnInit {
  private readonly usuarioSvc = inject(UsuarioService);
  private readonly toast = inject(ToastService);

  protected readonly pageTitle = 'Usuarios';
  protected readonly pageIcon = 'bi-people-fill';
  protected readonly pageDesc = 'Gestión de usuarios del sistema';

  protected readonly usuarios = signal<UsuarioListItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');
  protected readonly mostrarModal = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly usuarioADesactivar = signal<UsuarioListItem | null>(null);
  protected readonly guardando = signal(false);
  protected readonly roles = signal<RolItem[]>([]);
  protected readonly passTemporal = signal<string | null>(null);
  protected readonly copiado = signal(false);

  protected form: UsuarioRequest = this.formVacio();

  protected readonly esEdicion = computed(() => this.editandoId() !== null);

  protected readonly usuariosFiltrados = computed(() => {
    const term = this.busqueda().toLowerCase().trim();
    if (!term) return this.usuarios();
    return this.usuarios().filter(
      (u) =>
        u.nombre.toLowerCase().includes(term) ||
        u.apellido.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.nombreRol.toLowerCase().includes(term),
    );
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarRoles();
  }

  // ─── Búsqueda ────────────────────────────────────────────────────────────────

  protected onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  // ─── Modal crear / editar ────────────────────────────────────────────────────

  protected abrirModalCrear(): void {
    this.form = this.formVacio();
    this.passTemporal.set(null);
    this.copiado.set(false);
    this.editandoId.set(null);
    this.mostrarModal.set(true);
  }

  protected abrirModalEditar(usuario: UsuarioListItem): void {
    const rolId = this.roles().find((r) => r.nombreRol === usuario.nombreRol)?.id
      ?? this.roles()[0]?.id
      ?? 1;
    this.form = {
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      email: usuario.email,
      idRol: rolId,
    };
    this.passTemporal.set(null);
    this.copiado.set(false);
    this.editandoId.set(usuario.id);
    this.mostrarModal.set(true);

    // La lista devuelve passTemporal: null; el detalle individual sí lo trae.
    this.usuarioSvc.getById(usuario.id).subscribe({
      next: (detalle) => this.passTemporal.set(detalle.passTemporal),
      error: () => {},
    });
  }

  protected cerrarModal(): void {
    if (this.guardando()) return;
    this.mostrarModal.set(false);
    this.editandoId.set(null);
    this.passTemporal.set(null);
    this.copiado.set(false);
  }

  protected copiarPass(): void {
    const pass = this.passTemporal();
    if (!pass) return;
    navigator.clipboard.writeText(pass).then(() => {
      this.copiado.set(true);
      setTimeout(() => this.copiado.set(false), 2500);
    });
  }

  protected guardar(): void {
    if (this.guardando()) return;
    this.guardando.set(true);

    const id = this.editandoId();
    const obs = id
      ? this.usuarioSvc.update(id, this.form)
      : this.usuarioSvc.create(this.form);

    obs.subscribe({
      next: (res) => {
        this.guardando.set(false);
        this.cerrarModal();
        this.toast.success(res.mensaje, id ? 'Usuario actualizado' : 'Usuario creado');
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.toast.error(this.mensajeError(err), 'Error');
      },
    });
  }

  // ─── Desactivar ──────────────────────────────────────────────────────────────

  protected confirmarDesactivar(usuario: UsuarioListItem): void {
    this.usuarioADesactivar.set(usuario);
  }

  protected cancelarDesactivar(): void {
    this.usuarioADesactivar.set(null);
  }

  protected desactivar(usuario: UsuarioListItem): void {
    this.usuarioSvc.delete(usuario.id).subscribe({
      next: () => {
        this.usuarioADesactivar.set(null);
        this.toast.success(
          `${usuario.nombre} ${usuario.apellido} fue desactivado.`,
          'Usuario desactivado',
        );
        this.cargar();
      },
      error: (err) => {
        this.usuarioADesactivar.set(null);
        this.toast.error(this.mensajeError(err), 'Error al desactivar');
      },
    });
  }

  // ─── Carga ───────────────────────────────────────────────────────────────────

  protected reintentar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.usuarioSvc.getAll().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los usuarios.');
        this.loading.set(false);
      },
    });
  }

  // ─── Utilidades ──────────────────────────────────────────────────────────────

  protected formatFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-MX', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  protected rolBadgeClass(rol: string): string {
    const map: Record<string, string> = {
      ADMINISTRADOR:       'badge-admin',
      ANALISTA_COMERCIAL:  'badge-analista',
      'LÍDER_DE_PROYECTO': 'badge-lider',
      DESARROLLADOR:       'badge-dev',
    };
    return map[rol] ?? 'badge-default';
  }

  private formVacio(): UsuarioRequest {
    return { nombre: '', apellido: '', email: '', idRol: this.roles()[1]?.id ?? 1 };
  }

  private cargarRoles(): void {
    this.usuarioSvc.getRoles().subscribe({
      next: (data) => this.roles.set(data),
      error: () => this.toast.warning('No se pudieron cargar los roles.', 'Advertencia'),
    });
  }

  private mensajeError(err: { status: number; error: unknown }): string {
    if (err.status === 0) return 'Sin conexión con el servidor.';
    const body = err.error as Record<string, unknown> | string | null;
    if (typeof body === 'string' && body.length < 300) return body;
    if (body && typeof body === 'object') {
      if (typeof body['message'] === 'string') return body['message'];
      if (typeof body['error'] === 'string') return body['error'];
    }
    if (err.status === 400) return 'Datos inválidos. Revisa el formulario.';
    if (err.status === 403) return 'No tienes permisos para esta acción.';
    if (err.status === 409) return 'Ya existe un usuario con ese email.';
    return `Error inesperado (${err.status}).`;
  }
}
