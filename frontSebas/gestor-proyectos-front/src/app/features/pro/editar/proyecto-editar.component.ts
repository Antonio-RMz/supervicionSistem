import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { TipoProyectoService } from '../../../core/services/tipo-proyecto.service';
import { EstadoProyectoService } from '../../../core/services/estado-proyecto.service';
import { PrioridadProyectoService } from '../../../core/services/prioridad-proyecto.service';
import { ToastService } from '../../../shared/services/toast.service';
import { TipoProyecto, EstadoProyecto, PrioridadProyecto } from '../../../core/models/configuracion.model';
import { ProyectoGeneralTabComponent } from './tabs/general/proyecto-general-tab.component';
import { ProyectoConfiguracionTabComponent } from './tabs/configuracion/proyecto-configuracion-tab.component';
import { ProyectoEquipoTabComponent } from './tabs/equipo/proyecto-equipo-tab.component';
import { ProyectoExtrasTabComponent } from './tabs/extras/proyecto-extras-tab.component';

const URL_PATTERN = /^https?:\/\/.+/;

type TabId = 'general' | 'configuracion' | 'equipo' | 'extras';

@Component({
  selector: 'app-proyecto-editar',
  standalone: true,
  imports: [
    RouterLink,
    ProyectoGeneralTabComponent,
    ProyectoConfiguracionTabComponent,
    ProyectoEquipoTabComponent,
    ProyectoExtrasTabComponent,
  ],
  templateUrl: './proyecto-editar.component.html',
  styleUrl: './proyecto-editar.component.scss',
})
export class ProyectoEditarComponent implements OnInit {
  private readonly fb                   = inject(FormBuilder);
  private readonly router               = inject(Router);
  private readonly route                = inject(ActivatedRoute);
  private readonly proyectoSvc          = inject(ProyectoService);
  private readonly tipoProyectoSvc      = inject(TipoProyectoService);
  private readonly estadoProyectoSvc    = inject(EstadoProyectoService);
  private readonly prioridadProyectoSvc = inject(PrioridadProyectoService);
  private readonly toast                = inject(ToastService);

  protected proyectoId = 0;

  protected readonly tabActiva           = signal<TabId>('general');
  protected readonly cargando            = signal(true);
  protected readonly errorCarga          = signal<string | null>(null);
  protected readonly cargandoCatalogos   = signal(false);
  protected readonly guardando           = signal(false);
  protected readonly nombreProyecto      = signal('');
  protected readonly tiposProyecto       = signal<TipoProyecto[]>([]);
  protected readonly estadosProyecto     = signal<EstadoProyecto[]>([]);
  protected readonly prioridadesProyecto = signal<PrioridadProyecto[]>([]);

  protected readonly tabs = [
    { id: 'general'       as TabId, label: 'General',       icon: 'bi-file-text-fill' },
    { id: 'configuracion' as TabId, label: 'Configuración', icon: 'bi-gear-fill'      },
    { id: 'equipo'        as TabId, label: 'Equipo',        icon: 'bi-people-fill'    },
    { id: 'extras'        as TabId, label: 'Extras',        icon: 'bi-puzzle-fill'    },
  ] as const;

  protected readonly form: FormGroup = this.fb.group({
    // ── Pestaña General ──────────────────────────────────────────────────
    nombre:              ['', [Validators.required, Validators.maxLength(120)]],
    descripcion:         [''],
    cliente:             [''],
    tipoProyectoId:      [{ value: '', disabled: true }, Validators.required],
    estadoProyectoId:    [{ value: '', disabled: true }, Validators.required],
    prioridadProyectoId: [{ value: '', disabled: true }, Validators.required],
    // ── Pestaña Configuración ────────────────────────────────────────────
    icono:   [''],
    repoUrl: ['', [Validators.pattern(URL_PATTERN)]],
    appUrl:  ['', [Validators.pattern(URL_PATTERN)]],
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/pro']); return; }
    this.proyectoId = id;
    this.cargarTodo(id);
  }

  private cargarTodo(id: number): void {
    this.cargando.set(true);
    this.cargandoCatalogos.set(true);
    this.errorCarga.set(null);

    forkJoin({
      proyecto:    this.proyectoSvc.getById(id),
      tipos:       this.tipoProyectoSvc.getAll(),
      estados:     this.estadoProyectoSvc.getAll(),
      prioridades: this.prioridadProyectoSvc.getAll(),
    }).subscribe({
      next: ({ proyecto, tipos, estados, prioridades }) => {
        // Poblar catálogos y habilitar selects
        this.tiposProyecto.set(tipos);
        this.estadosProyecto.set(estados);
        this.prioridadesProyecto.set(prioridades);
        this.form.get('tipoProyectoId')?.enable();
        this.form.get('estadoProyectoId')?.enable();
        this.form.get('prioridadProyectoId')?.enable();

        // Rellenar formulario con datos del proyecto
        this.nombreProyecto.set(proyecto.nombre);
        this.form.patchValue({
          nombre:              proyecto.nombre,
          descripcion:         proyecto.descripcion         ?? '',
          cliente:             proyecto.cliente             ?? '',
          tipoProyectoId:      proyecto.tipoProyectoId      ?? '',
          estadoProyectoId:    proyecto.estadoProyectoId    ?? '',
          prioridadProyectoId: proyecto.prioridadProyectoId ?? '',
          icono:               proyecto.icono   ?? '',
          repoUrl:             proyecto.repoUrl ?? '',
          appUrl:              proyecto.appUrl  ?? '',
        });

        this.cargando.set(false);
        this.cargandoCatalogos.set(false);
      },
      error: () => {
        this.errorCarga.set('No se pudo cargar la información del proyecto.');
        this.cargando.set(false);
        this.cargandoCatalogos.set(false);
      },
    });
  }

  protected activarTab(tab: TabId): void {
    this.tabActiva.set(tab);
  }

  protected cancelar(): void {
    this.router.navigate(['/pro', this.proyectoId]);
  }

  protected guardarCambios(): void {
    this.form.markAllAsTouched();

    if (!this.form.valid) {
      // Redirige a General donde están los campos obligatorios
      this.tabActiva.set('general');
      return;
    }
    if (this.guardando() || this.cargando()) return;

    const raw = this.form.getRawValue();
    const request = {
      nombre:              raw.nombre,
      descripcion:         raw.descripcion  || null,
      cliente:             raw.cliente      || null,
      tipoProyectoId:      Number(raw.tipoProyectoId),
      estadoProyectoId:    Number(raw.estadoProyectoId),
      prioridadProyectoId: Number(raw.prioridadProyectoId),
      icono:   raw.icono   || null,
      repoUrl: raw.repoUrl || null,
      appUrl:  raw.appUrl  || null,
    };

    this.guardando.set(true);
    this.proyectoSvc.updateEditar(this.proyectoId, request).subscribe({
      next: () => {
        this.guardando.set(false);
        this.toast.success('El proyecto fue actualizado correctamente.', 'Proyecto actualizado');
        this.router.navigate(['/pro', this.proyectoId]);
      },
      error: (err) => {
        this.guardando.set(false);
        this.toast.error(this.mensajeError(err), 'Error al guardar');
      },
    });
  }

  private mensajeError(err: { status: number; error: unknown }): string {
    if (err.status === 0) return 'No hay conexión con el servidor.';
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
    if (err.status === 403) return 'No tienes permisos para editar este proyecto.';
    if (err.status === 422) return msg ?? 'Error 422: validación fallida.';
    if (err.status >= 500)  return msg ?? `Error ${err.status} en el servidor. Contacta al administrador.`;
    return msg ?? `Error inesperado (${err.status}).`;
  }
}
