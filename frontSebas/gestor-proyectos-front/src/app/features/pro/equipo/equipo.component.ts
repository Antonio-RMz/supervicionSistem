import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, distinctUntilChanged, filter, finalize, map, switchMap, catchError } from 'rxjs';
import { SessionService } from '../../../core/services/session.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { MiembroProyectoResponse } from '../../../core/models/proyecto.model';

const AVATAR_COLORS = [
  '#4361ee', '#7209b7', '#f72585', '#0d9488',
  '#e67e22', '#2ecc71', '#e74c3c', '#3498db',
];

@Component({
  selector: 'app-equipo',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './equipo.component.html',
  styleUrl: './equipo.component.scss',
})
export class EquipoComponent {
  protected readonly session      = inject(SessionService);
  private readonly proyectoSvc   = inject(ProyectoService);
  private readonly route         = inject(ActivatedRoute);
  private readonly destroyRef    = inject(DestroyRef);

  readonly equipo         = signal<MiembroProyectoResponse[]>([]);
  readonly cargandoEquipo = signal(false);
  readonly errorEquipo    = signal<string | null>(null);
  readonly idProyecto     = signal<number | null>(null);

  constructor() {
    this.route.paramMap
      .pipe(
        map(params => Number(params.get('id'))),
        filter(id => Number.isInteger(id) && id > 0),
        distinctUntilChanged(),
        switchMap(id => {
          this.idProyecto.set(id);
          this.cargandoEquipo.set(true);
          this.errorEquipo.set(null);
          this.equipo.set([]);

          const activo = this.session.proyectoActivo();
          if (!activo || activo.id !== id) {
            this.proyectoSvc.getById(id).subscribe({
              next: p => this.session.seleccionarProyecto(p),
              error: () => {},
            });
          }

          return this.proyectoSvc.obtenerEquipo(id).pipe(
            finalize(() => this.cargandoEquipo.set(false)),
            catchError(error => {
              this.equipo.set([]);
              if (error.status === 403) {
                this.errorEquipo.set('No tienes permiso para consultar este proyecto.');
              } else if (error.status === 404) {
                this.errorEquipo.set('El proyecto no existe.');
              } else {
                this.errorEquipo.set('No se pudo cargar el equipo del proyecto.');
              }
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(equipo => this.equipo.set(equipo ?? []));
  }

  protected obtenerNombreCompleto(miembro: MiembroProyectoResponse): string {
    if (miembro.nombreCompleto?.trim()) return miembro.nombreCompleto;
    return `${miembro.nombre ?? ''} ${miembro.apellido ?? ''}`.trim();
  }

  protected obtenerIniciales(miembro: MiembroProyectoResponse): string {
    if (miembro.iniciales?.trim()) return miembro.iniciales;
    const nombre   = miembro.nombre?.trim()   ?? '';
    const apellido = miembro.apellido?.trim() ?? '';
    return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase();
  }

  protected obtenerRol(miembro: MiembroProyectoResponse): string {
    return miembro.nombreRol ?? 'Miembro';
  }

  protected obtenerColorAvatar(miembro: MiembroProyectoResponse): string {
    return AVATAR_COLORS[miembro.idUsuario % AVATAR_COLORS.length];
  }
}
