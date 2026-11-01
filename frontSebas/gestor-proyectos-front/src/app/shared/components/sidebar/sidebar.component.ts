import { Component, computed, inject, input } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';
import { SessionService } from '../../../core/services/session.service';
import { MODULOS_CATALOG } from '../../../core/constants/modulos.catalog';

interface NavItem {
  icon: string;
  label: string;
  route: string;
  action?: 'proyectos';
  separator?: false;
}

interface NavSeparator {
  separator: true;
  label: string;
  proyectoModule?: true;
}

type NavEntry = NavItem | NavSeparator;

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  collapsed  = input(false);
  mobileOpen = input(false);

  protected readonly session = inject(SessionService);
  private  readonly router  = inject(Router);

  // URL actual reactiva: se actualiza en cada NavigationEnd
  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  // "Proyectos" activo cuando estamos en /pro o cualquier submódulo
  protected readonly isProActive = computed(() =>
    this.currentUrl().startsWith('/pro'),
  );

  protected readonly navItems = computed<NavEntry[]>(() => {
    const modulos    = this.session.modulos();
    const proyectoId = this.session.proyectoId();

    // Dashboard siempre visible
    const items: NavEntry[] = [
      { icon: 'bi-grid-1x2-fill', label: 'Dashboard', route: '/dashboard' },
    ];

    if (!modulos.length) return items;

    // Cruzar refs del backend con el catálogo local
    const refs       = new Set(modulos.map(m => m.ref));
    const accesibles = MODULOS_CATALOG.filter(c => refs.has(c.ref));

    // ── 1. PRINCIPAL (siempre visible) ───────────────────────────────────────
    for (const m of accesibles.filter(c => c.tipo === 'PRINCIPAL')) {
      items.push(
        m.ref === 'MOD-PROYECTOS'
          ? { icon: m.icono, label: m.nombre, route: m.ruta, action: 'proyectos' }
          : { icon: m.icono, label: m.nombre, route: m.ruta },
      );
    }

    // Mis actividades: visible para todos los usuarios autenticados
    items.push({ icon: 'bi-lightning-charge-fill', label: 'Mis actividades', route: '/mis-actividades' });

    // ── 2. SOLO_ADMIN (visible si el usuario tiene ese ref) ──────────────────
    const soloAdmin = accesibles.filter(c => c.tipo === 'SOLO_ADMIN');
    if (soloAdmin.length) {
      items.push({ separator: true, label: 'Administración' });
      for (const m of soloAdmin) {
        items.push({ icon: m.icono, label: m.nombre, route: m.ruta });
      }
    }

    // ── 3 & 4. GENERAL y ROL solo cuando hay proyecto activo ─────────────────
    if (proyectoId !== null) {
      const generals = accesibles.filter(c => c.tipo === 'GENERAL');
      if (generals.length) {
        items.push({ separator: true, label: 'General', proyectoModule: true });
        for (const m of generals) {
          const route = m.requiereProyecto ? `/pro/${proyectoId}${m.ruta}` : m.ruta;
          items.push({ icon: m.icono, label: m.nombre, route });
        }
      }

      const roles = accesibles.filter(c => c.tipo === 'ROL');
      if (roles.length) {
        items.push({ separator: true, label: 'Mis herramientas', proyectoModule: true });
        for (const m of roles) {
          const route = m.requiereProyecto ? `/pro/${proyectoId}${m.ruta}` : m.ruta;
          items.push({ icon: m.icono, label: m.nombre, route });
        }
      }
    }

    return items;
  });

  /**
   * Navegación contextual para el ítem "Proyectos":
   *
   * · En submódulo con proyecto activo  → sale al listado /pro y limpia contexto
   * · Fuera de /pro con proyecto activo → va al detalle /pro/:id
   * · Sin proyecto activo               → va al listado /pro
   */
  protected onClickProyectos(): void {
    const url        = this.currentUrl();
    const proyectoId = this.session.proyectoId();

    if (url.startsWith('/pro/') && proyectoId !== null) {
      this.session.deseleccionarProyecto();
      this.router.navigate(['/pro']);
      return;
    }

    if (proyectoId !== null && !url.startsWith('/pro')) {
      this.router.navigate(['/pro', proyectoId]);
      return;
    }

    this.router.navigate(['/pro']);
  }

  isSeparator(item: NavEntry): item is NavSeparator {
    return (item as NavSeparator).separator === true;
  }

  isProyectosItem(item: NavEntry): item is NavItem & { action: 'proyectos' } {
    return !this.isSeparator(item) && (item as NavItem).action === 'proyectos';
  }
}
