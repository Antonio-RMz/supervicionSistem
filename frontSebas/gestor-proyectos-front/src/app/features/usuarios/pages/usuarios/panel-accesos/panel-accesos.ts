import { Component, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';
import { Avatar } from '../../../../../shared/components/avatar/avatar';
import { mensajeDeError } from '../../../../../shared/utils/errores';
import { ordenarRoles } from '../../../../../shared/utils/roles';
import { ProyectoResumen } from '../../../../gestor/models/proyecto.models';
import { UsuariosService } from '../../../data/usuarios.service';
import { Accesos, PermisoExtra, Permiso, Rol, UsuarioFila } from '../../../models/usuarios.models';

/**
 * Panel de la derecha en Usuarios y roles: marca roles y ajusta permisos extra de una persona.
 * Los cambios se quedan aquí hasta "Guardar cambios".
 */
@Component({
  selector: 'app-panel-accesos',
  imports: [Avatar],
  templateUrl: './panel-accesos.html',
  styleUrl: './panel-accesos.scss',
})
export class PanelAccesos {
  private readonly service = inject(UsuariosService);

  readonly usuario = input.required<UsuarioFila>();
  readonly roles = input.required<Rol[]>();
  readonly permisos = input.required<Permiso[]>();
  readonly proyectos = input.required<ProyectoResumen[]>();

  readonly cerrar = output<void>();
  readonly guardado = output<Accesos>();

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly rolesElegidos = signal<string[]>([]);
  protected readonly extras = signal<PermisoExtra[]>([]);

  // "Agregar permiso"
  protected readonly agregando = signal(false);
  protected readonly nuevoPermiso = signal('');
  protected readonly nuevoProyecto = signal<number | null>(null);
  protected readonly nuevoOtorgado = signal(true);

  /** Mismo permiso y mismo alcance que uno de la lista: no se puede agregar dos veces. */
  protected readonly nuevoRepetido = computed(() =>
    this.extras().some((p) => p.permisoCodigo === this.nuevoPermiso() && p.proyectoId === this.nuevoProyecto()),
  );

  constructor() {
    // Cada vez que se abre el panel de otra persona se leen sus accesos guardados.
    toObservable(this.usuario)
      .pipe(
        map((usuario) => usuario.id),
        distinctUntilChanged(),
        tap(() => {
          this.cargando.set(true);
          this.error.set(null);
          this.agregando.set(false);
        }),
        switchMap((id) =>
          this.service.accesos(id).pipe(
            catchError(() => {
              this.error.set('No pudimos cargar sus roles y permisos.');
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((accesos) => {
        if (accesos) this.aplicar(accesos);
        this.cargando.set(false);
      });
  }

  protected tieneRol(codigo: string): boolean {
    return this.rolesElegidos().includes(codigo);
  }

  protected alternarRol(codigo: string, marcado: boolean): void {
    this.rolesElegidos.update((roles) =>
      marcado ? ordenarRoles([...roles, codigo]) : roles.filter((r) => r !== codigo),
    );
  }

  protected quitarExtra(indice: number): void {
    this.extras.update((lista) => lista.filter((_, i) => i !== indice));
  }

  protected abrirAgregar(): void {
    this.nuevoPermiso.set(this.permisos()[0]?.codigo ?? '');
    this.nuevoProyecto.set(null);
    this.nuevoOtorgado.set(true);
    this.agregando.set(true);
  }

  protected agregar(): void {
    const permiso = this.permisos().find((p) => p.codigo === this.nuevoPermiso());
    if (!permiso || this.nuevoRepetido()) return;
    const proyectoId = this.nuevoProyecto();
    this.extras.update((lista) => [
      ...lista,
      {
        permisoCodigo: permiso.codigo,
        permisoNombre: permiso.nombre,
        proyectoId,
        proyectoNombre: this.proyectos().find((p) => p.id === proyectoId)?.nombre ?? 'Todos los proyectos',
        otorgado: this.nuevoOtorgado(),
      },
    ]);
    this.agregando.set(false);
  }

  protected guardar(): void {
    if (!this.rolesElegidos().length) {
      this.error.set('Elige al menos un rol.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .guardarAccesos(this.usuario().id, { roles: this.rolesElegidos(), permisosExtra: this.extras() })
      .subscribe({
        next: (accesos) => {
          this.guardando.set(false);
          this.aplicar(accesos);
          this.guardado.emit(accesos);
        },
        error: (e) => {
          this.guardando.set(false);
          this.error.set(mensajeDeError(e, 'No pudimos guardar los cambios. Intenta de nuevo.'));
        },
      });
  }

  private aplicar(accesos: Accesos): void {
    this.rolesElegidos.set(accesos.roles);
    this.extras.set(accesos.permisosExtra);
  }
}
