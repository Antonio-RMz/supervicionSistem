import { inject, Injectable, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Permiso, Rol } from '../models/usuarios.models';
import { UsuariosService } from './usuarios.service';

/** Roles y permisos que existen. Casi nunca cambian: se piden una vez y se reusan. */
@Injectable({ providedIn: 'root' })
export class CatalogoAccesosStore {
  private readonly service = inject(UsuariosService);
  private cargado = false;

  readonly roles = signal<Rol[]>([]);
  readonly permisos = signal<Permiso[]>([]);
  readonly error = signal(false);

  cargar(): void {
    if (this.cargado) return;
    this.cargado = true;
    this.error.set(false);
    forkJoin({ roles: this.service.roles(), permisos: this.service.permisos() }).subscribe({
      next: ({ roles, permisos }) => {
        this.roles.set(roles);
        this.permisos.set(permisos);
      },
      error: () => {
        this.cargado = false;
        this.error.set(true);
      },
    });
  }
}
