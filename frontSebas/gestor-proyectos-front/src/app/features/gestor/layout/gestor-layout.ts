import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { Avisos } from '../../../shared/components/avisos/avisos';
import { BotonTema } from '../../../shared/components/boton-tema/boton-tema';
import { nombresDeRoles } from '../../../shared/utils/roles';
import { ProyectoActualStore } from '../data/proyecto-actual.store';
import { LogoInova } from '../../../shared/components/logo-inova/logo-inova';
import { Campana } from '../components/campana/campana';

const CLAVE_MENU = 'gestor.menuContraido';

/** Menú lateral (contraíble) + barra superior (selector de proyecto, búsqueda) del Gestor de Proyectos. */
@Component({
  selector: 'app-gestor-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Avisos, BotonTema, LogoInova, Campana],
  templateUrl: './gestor-layout.html',
  styleUrl: './gestor-layout.scss',
  host: { '[class.contraido]': 'contraido()' },
})
export class GestorLayout {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(ProyectoActualStore);
  protected readonly usuario = this.auth.usuario;

  /** "Administrador · Líder" */
  protected readonly roles = computed(() => nombresDeRoles(this.usuario()?.roles ?? []));

  /** Usuarios y roles y Clientes: solo el Administrador. */
  protected readonly esAdmin = computed(() => this.usuario()?.roles.includes('ADMIN') ?? false);


  protected readonly iniciales = computed(() => this.usuario()?.iniciales ?? '');

  /** Solo íconos; se recuerda entre visitas. */
  protected readonly contraido = signal(leerMenu());

  constructor() {
    this.store.cargar();
  }

  protected cambiarProyecto(id: string): void {
    this.store.elegir(Number(id));
  }

  protected salir(): void {
    this.auth.cerrarSesion();
  }

  protected alternarMenu(): void {
    this.contraido.update((c) => !c);
    try {
      localStorage.setItem(CLAVE_MENU, String(this.contraido()));
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
  }
}

function leerMenu(): boolean {
  try {
    return localStorage.getItem(CLAVE_MENU) === 'true';
  } catch {
    return false;
  }
}
