import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { TipoProyectoService } from '../../../core/services/tipo-proyecto.service';
import { EstadoProyectoService } from '../../../core/services/estado-proyecto.service';
import { PrioridadProyectoService } from '../../../core/services/prioridad-proyecto.service';
import { ToastService } from '../../../shared/services/toast.service';
import { TipoProyecto, EstadoProyecto, PrioridadProyecto } from '../../../core/models/configuracion.model';

@Component({
  selector: 'app-proyecto-nuevo',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './proyecto-nuevo.component.html',
  styleUrl: './proyecto-nuevo.component.scss',
})
export class ProyectoNuevoComponent implements OnInit {
  private readonly fb                   = inject(FormBuilder);
  private readonly router               = inject(Router);
  private readonly proyectoSvc          = inject(ProyectoService);
  private readonly tipoProyectoSvc      = inject(TipoProyectoService);
  private readonly estadoProyectoSvc    = inject(EstadoProyectoService);
  private readonly prioridadProyectoSvc = inject(PrioridadProyectoService);
  private readonly toast                = inject(ToastService);

  protected readonly guardando           = signal(false);
  protected readonly cargandoCatalogos   = signal(false);
  protected readonly errorCatalogos      = signal<string | null>(null);
  protected readonly tiposProyecto       = signal<TipoProyecto[]>([]);
  protected readonly estadosProyecto     = signal<EstadoProyecto[]>([]);
  protected readonly prioridadesProyecto = signal<PrioridadProyecto[]>([]);

  protected readonly form: FormGroup = this.fb.group({
    nombre:              ['', [Validators.required, Validators.maxLength(120)]],
    cliente:             [''],
    tipoProyectoId:      [{ value: null, disabled: true }, Validators.required],
    descripcion:         [''],
    estadoProyectoId:    [{ value: null, disabled: true }, Validators.required],
    prioridadProyectoId: [{ value: null, disabled: true }, Validators.required],
  });

  ngOnInit(): void {
    this.cargarCatalogos();
  }

  private cargarCatalogos(): void {
    this.cargandoCatalogos.set(true);
    this.errorCatalogos.set(null);

    forkJoin({
      tipos:      this.tipoProyectoSvc.getAll(),
      estados:    this.estadoProyectoSvc.getAll(),
      prioridades: this.prioridadProyectoSvc.getAll(),
    }).subscribe({
      next: ({ tipos, estados, prioridades }) => {
        this.tiposProyecto.set(tipos);
        this.estadosProyecto.set(estados);
        this.prioridadesProyecto.set(prioridades);

        const borrador = estados.find((e) => e.id === 1);
        if (borrador) {
          this.form.get('estadoProyectoId')?.setValue(borrador.id);
        }

        this.form.get('tipoProyectoId')?.enable();
        this.form.get('estadoProyectoId')?.enable();
        this.form.get('prioridadProyectoId')?.enable();
        this.cargandoCatalogos.set(false);
      },
      error: () => {
        this.errorCatalogos.set(
          'No se pudieron cargar los catálogos. Recarga la página e intenta de nuevo.',
        );
        this.cargandoCatalogos.set(false);
      },
    });
  }

  protected reintentarCarga(): void {
    this.cargarCatalogos();
  }

  protected hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  protected cancelar(): void {
    this.router.navigate(['/pro']);
  }

  protected crearProyecto(): void {
    this.form.markAllAsTouched();
    if (!this.form.valid || this.guardando() || this.cargandoCatalogos()) return;

    const raw = this.form.getRawValue();
    const request = {
      nombre:              raw.nombre,
      cliente:             raw.cliente      || null,
      descripcion:         raw.descripcion  || null,
      tipoProyectoId:      Number(raw.tipoProyectoId),
      estadoProyectoId:    Number(raw.estadoProyectoId),
      prioridadProyectoId: Number(raw.prioridadProyectoId),
    };

    this.guardando.set(true);
    this.proyectoSvc.createNuevo(request).subscribe({
      next: (proyecto) => {
        this.guardando.set(false);
        this.toast.success(`"${proyecto.nombre}" fue creado correctamente.`, 'Proyecto creado');
        this.router.navigate(['/pro', proyecto.id]);
      },
      error: (err) => {
        this.guardando.set(false);
        this.toast.error(this.mensajeError(err), 'Error al crear proyecto');
      },
    });
  }

  private mensajeError(err: { status: number; error: unknown }): string {
    if (err.status === 0) {
      return 'No hay conexión con el servidor. Verifica que el backend esté corriendo.';
    }
    const body = err.error as Record<string, unknown> | string | null;
    let msg: string | null = null;
    if (typeof body === 'string' && body.length < 300) {
      msg = body;
    } else if (body && typeof body === 'object') {
      if (typeof body['message'] === 'string')    msg = body['message'];
      else if (typeof body['error'] === 'string') msg = body['error'];
      else if (body['errors'] && typeof body['errors'] === 'object') {
        msg = Object.values(body['errors'] as Record<string, string>).join(' · ');
      }
    }
    if (err.status === 400) return msg ?? 'Error 400: datos rechazados por el servidor.';
    if (err.status === 401) return 'Sesión expirada. Vuelve a iniciar sesión.';
    if (err.status === 403) return 'No tienes permisos para crear proyectos.';
    if (err.status === 422) return msg ?? 'Error 422: validación fallida.';
    if (err.status >= 500)  return msg ?? `Error ${err.status} en el servidor. Contacta al administrador.`;
    return msg ?? `Error inesperado (${err.status}).`;
  }
}
