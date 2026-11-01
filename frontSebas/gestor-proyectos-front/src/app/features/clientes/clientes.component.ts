import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ClienteService } from '../../core/services/cliente.service';
import { ToastService } from '../../shared/services/toast.service';
import { ClienteRequest, ClienteResponse } from '../../core/models/cliente.model';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss',
})
export class ClientesComponent implements OnInit {
  private readonly clienteSvc = inject(ClienteService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  protected readonly clientes = signal<ClienteResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly mostrarModal = signal(false);
  protected readonly modoEdicion = signal(false);
  protected readonly idSeleccionado = signal<number | null>(null);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    nombre:   ['', [Validators.required]],
    empresa:  [''],
    email:    ['', [Validators.required, Validators.email]],
    telefono: [''],
    estado:   [true],
  });

  ngOnInit(): void {
    this.cargarClientes();
  }

  protected cargarClientes(): void {
    this.loading.set(true);
    this.error.set(null);
    this.clienteSvc.obtenerTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los clientes.');
        this.loading.set(false);
      },
    });
  }

  protected abrirModalCrear(): void {
    this.form.reset({ nombre: '', empresa: '', email: '', telefono: '', estado: true });
    this.modoEdicion.set(false);
    this.idSeleccionado.set(null);
    this.mostrarModal.set(true);
  }

  protected abrirModalEditar(c: ClienteResponse): void {
    this.form.reset({
      nombre:   c.nombre,
      empresa:  c.empresa,
      email:    c.email,
      telefono: c.telefono,
      estado:   c.estado,
    });
    this.modoEdicion.set(true);
    this.idSeleccionado.set(c.id);
    this.mostrarModal.set(true);
  }

  protected guardar(): void {
    if (this.guardando() || this.form.invalid) return;
    this.guardando.set(true);

    const dto = this.form.getRawValue() as ClienteRequest;
    const id = this.idSeleccionado();
    const obs = id
      ? this.clienteSvc.actualizar(id, dto)
      : this.clienteSvc.crear(dto);

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.toast.success(
          id ? 'Cliente actualizado correctamente.' : 'Cliente creado correctamente.',
          id ? 'Cliente actualizado' : 'Cliente creado',
        );
        this.cargarClientes();
      },
      error: (err) => {
        this.guardando.set(false);
        this.toast.error(this.mensajeError(err), 'Error');
      },
    });
  }

  protected eliminar(id: number): void {
    if (!window.confirm('¿Estás seguro de que deseas eliminar este cliente?')) return;
    this.clienteSvc.eliminar(id).subscribe({
      next: () => {
        this.toast.success('El cliente fue eliminado.', 'Cliente eliminado');
        this.cargarClientes();
      },
      error: (err) => {
        this.toast.error(this.mensajeError(err), 'Error al eliminar');
      },
    });
  }

  protected cerrarModal(): void {
    if (this.guardando()) return;
    this.mostrarModal.set(false);
    this.modoEdicion.set(false);
    this.idSeleccionado.set(null);
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
    if (err.status === 409) return 'Ya existe un cliente con ese email.';
    return `Error inesperado (${err.status}).`;
  }
}
