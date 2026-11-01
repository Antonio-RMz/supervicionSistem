import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { BotonTema } from '../../../shared/components/boton-tema/boton-tema';
import { PortalClienteStore } from '../data/portal-cliente.store';

/** Barra superior del portal + el contenido de cada pantalla. */
@Component({
  selector: 'app-portal-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, BotonTema],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.scss',
})
export class PortalLayout {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly store = inject(PortalClienteStore);

  protected readonly usuario = this.auth.usuario;

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  /** En la pantalla Avance se muestra el selector de proyecto y se marca "Avance" en el menú. */
  protected readonly enAvance = computed(() => /^\/portal\/proyectos\/\d+/.test(this.url()));

  constructor() {
    this.store.cargar().catch(() => undefined);
  }

  protected cambiarProyecto(id: string): void {
    this.router.navigate(['/portal/proyectos', id]);
  }

  protected salir(): void {
    this.auth.cerrarSesion();
  }
}
