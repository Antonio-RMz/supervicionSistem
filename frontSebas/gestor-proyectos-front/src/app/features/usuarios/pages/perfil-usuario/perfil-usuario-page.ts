import { Component, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { diaYHora, haceCuanto, hora } from '../../../../shared/utils/fechas';
import { tonoDeAvatar } from '../../../../shared/utils/roles';
import { UsuariosService } from '../../data/usuarios.service';
import { PerfilUsuario } from '../../models/usuarios.models';

/** Detalle de una persona del equipo (PDF pág. 7): en qué trabaja, qué le toca y lo que hizo. */
@Component({
  selector: 'app-perfil-usuario-page',
  imports: [RouterLink, Avatar, EtiquetaEstado, TipoTicket],
  templateUrl: './perfil-usuario-page.html',
  styleUrl: './perfil-usuario-page.scss',
})
export class PerfilUsuarioPage {
  private readonly service = inject(UsuariosService);
  private readonly miId = inject(AuthService).usuario()?.id;

  /** :id de la ruta */
  readonly id = input.required<string>();

  protected readonly perfil = signal<PerfilUsuario | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  protected readonly confirmandoDesactivar = signal(false);
  protected readonly cambiandoEstado = signal(false);
  protected readonly errorEstado = signal<string | null>(null);

  protected readonly soyYo = computed(() => this.perfil()?.id === this.miId);

  protected readonly haceCuanto = haceCuanto;
  protected readonly hora = hora;
  protected readonly diaYHora = diaYHora;

  constructor() {
    toObservable(this.id)
      .pipe(
        tap(() => {
          this.cargando.set(true);
          this.error.set(false);
        }),
        switchMap((id) =>
          this.service.perfil(Number(id)).pipe(
            catchError(() => {
              this.error.set(true);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((perfil) => {
        this.perfil.set(perfil);
        this.cargando.set(false);
      });
  }

  /** Desactivar pide un segundo clic; activar es inmediato. */
  protected cambiarEstado(): void {
    const perfil = this.perfil();
    if (!perfil) return;
    if (perfil.activo && !this.confirmandoDesactivar()) {
      this.confirmandoDesactivar.set(true);
      return;
    }
    this.cambiandoEstado.set(true);
    this.errorEstado.set(null);
    this.service.cambiarEstado(perfil.id, !perfil.activo).subscribe({
      next: () => {
        this.cambiandoEstado.set(false);
        this.confirmandoDesactivar.set(false);
        const activo = !perfil.activo;
        this.perfil.set({ ...perfil, activo, tonoAvatar: tonoDeAvatar(perfil.id, activo) });
      },
      error: (e) => {
        this.cambiandoEstado.set(false);
        this.confirmandoDesactivar.set(false);
        this.errorEstado.set(mensajeDeError(e, 'No pudimos cambiar el estado. Intenta de nuevo.'));
      },
    });
  }
}
