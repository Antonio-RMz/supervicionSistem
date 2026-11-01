import {
  Component,
  computed,
  ElementRef,
  HostListener,
  inject,
  output,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { ToastService } from '../../services/toast.service';
import { BreadcrumbService } from '../../services/breadcrumb.service';
import { NotificacionesPanelComponent } from '../notificaciones/notificaciones-panel.component';
import { Notificacion } from '../../models/notificacion.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, NotificacionesPanelComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  toggleSidebar = output<void>();

  private readonly auth       = inject(AuthService);
  private readonly toast      = inject(ToastService);
  private readonly elementRef = inject(ElementRef);
  protected readonly session    = inject(SessionService);
  protected readonly breadcrumb = inject(BreadcrumbService);

  protected readonly iniciales = computed(() => {
    const u = this.session.usuario();
    if (!u) return '';
    return ((u.nombre?.[0] ?? '') + (u.apellido?.[0] ?? '')).toUpperCase();
  });

  // ── Dropdown de usuario ───────────────────────────────────────────────────
  protected readonly dropdownOpen = signal(false);

  protected toggleDropdown(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.dropdownOpen()) {
      document.dispatchEvent(new CustomEvent('appCloseMenus'));
    }
    this.dropdownOpen.update(v => !v);
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: Event): void {
    if (!(this.elementRef.nativeElement as HTMLElement).contains(event.target as Node)) {
      this.dropdownOpen.set(false);
    }
  }

  @HostListener('document:appCloseMenus')
  protected onCloseMenus(): void {
    this.dropdownOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    this.dropdownOpen.set(false);
    this.notifPanelOpen.set(false);
  }

  onLogout(): void {
    this.dropdownOpen.set(false);
    this.auth.logout().subscribe({
      next: () => this.toast.success('Sesión cerrada correctamente'),
    });
  }

  // ── Panel de notificaciones ───────────────────────────────────────────────
  protected readonly notifPanelOpen = signal(false);

  // Mock data — reemplazar por NotificacionesService.getAll() cuando la API esté lista
  protected readonly notificaciones = signal<Notificacion[]>([
    {
      id: 1,
      tipo: 'proyecto',
      titulo: 'Nuevo proyecto creado',
      descripcion: "El proyecto 'Portal de Clientes' fue creado por Jorge Luis.",
      fechaRelativa: 'Hace 5 minutos',
      leida: false,
    },
    {
      id: 2,
      tipo: 'estado',
      titulo: 'Cambio de estado',
      descripcion: "El proyecto 'API Interna' pasó a estado Activo.",
      fechaRelativa: 'Hace 2 horas',
      leida: false,
    },
    {
      id: 3,
      tipo: 'usuario',
      titulo: 'Nuevo miembro asignado',
      descripcion: "María García fue asignada al proyecto 'App Móvil'.",
      fechaRelativa: 'Ayer',
      leida: true,
    },
    {
      id: 4,
      tipo: 'sistema',
      titulo: 'Actualización del sistema',
      descripcion: 'Se realizaron mejoras de rendimiento en el módulo de backlog.',
      fechaRelativa: 'Hace 3 días',
      leida: true,
    },
    {
      id: 5,
      tipo: 'proyecto',
      titulo: 'Proyecto completado',
      descripcion: "El proyecto 'Rediseño Web' fue marcado como Completado.",
      fechaRelativa: 'Hace 1 semana',
      leida: false,
    },
  ]);

  protected readonly hayNoLeidas = computed(() =>
    this.notificaciones().some(n => !n.leida),
  );

  protected toggleNotifPanel(): void {
    // Cierra el dropdown de usuario si estaba abierto
    this.dropdownOpen.set(false);
    this.notifPanelOpen.update(v => !v);
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }
}
