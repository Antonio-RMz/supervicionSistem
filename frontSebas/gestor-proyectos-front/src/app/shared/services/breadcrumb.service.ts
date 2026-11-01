import { computed, inject, Injectable } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { SessionService } from '../../core/services/session.service';

export interface BreadcrumbItem {
  label:       string;
  routerLink?: string | any[];
}

// Etiquetas de sub-rutas de /pro/:id
const PRO_SUB: Record<string, string> = {
  fases:       'Fases',
  actividades: 'Actividades',
  backlog:     'Backlog',
  historias:   'Historias',
  sprint:      'Sprint Planning',
  equipo:      'Equipo',
  tickets:     'Tickets',
  editar:      'Editar',
};

// Etiquetas de rutas de primer nivel
const TOP_LABELS: Record<string, string> = {
  clientes:          'Clientes',
  usuarios:          'Usuarios',
  roles:             'Roles',
  permisos:          'Permisos',
  'mis-actividades': 'Mis actividades',
  configuracion:     'Configuración',
  sol:               'Solicitudes',
  his:               'Historial',
  qa:                'QA',
  sesiones:          'Sesiones',
};

@Injectable({ providedIn: 'root' })
export class BreadcrumbService {
  private readonly router  = inject(Router);
  private readonly session = inject(SessionService);

  // Señal que cambia cada vez que el router termina una navegación
  private readonly navEnd = toSignal(
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)),
    { initialValue: null },
  );

  // Computed completamente reactivo: se recalcula en cada navegación
  // y también cuando cambia el proyecto activo en la sesión
  readonly items = computed<BreadcrumbItem[]>(() => {
    this.navEnd(); // dependencia de navegación
    const proy = this.session.proyectoActivo(); // dependencia del proyecto activo
    return this.build(this.router.url, proy);
  });

  private build(
    url:  string,
    proy: { id: number; nombre: string } | null,
  ): BreadcrumbItem[] {
    const path     = url.split('?')[0];
    const segments = path.split('/').filter(Boolean);

    if (!segments.length || segments[0] === 'dashboard') return [];

    if (segments[0] === 'pro')     return this.buildPro(segments, proy);
    if (segments[0] === 'tickets') return this.buildTicketDetalle(segments, proy);

    const topLabel = TOP_LABELS[segments[0]];
    if (topLabel) {
      const base  = `/${segments[0]}`;
      const items: BreadcrumbItem[] = [
        { label: topLabel, routerLink: segments.length > 1 ? base : undefined },
      ];
      if (segments.length > 1) items.push({ label: segments[1] });
      return items;
    }

    return [];
  }

  private buildPro(
    segments: string[],
    proy:     { id: number; nombre: string } | null,
  ): BreadcrumbItem[] {
    const items: BreadcrumbItem[] = [{ label: 'Proyectos', routerLink: '/pro' }];
    if (segments.length === 1) return items;

    // /pro/nuevo
    if (segments[1] === 'nuevo') {
      items.push({ label: 'Nuevo Proyecto' });
      return items;
    }

    // /pro/:id  — nombre del proyecto desde la sesión
    const proyId = segments[1];
    items.push({
      label:      proy?.nombre ?? 'Proyecto',
      routerLink: segments.length > 2 ? ['/pro', proyId] : undefined,
    });
    if (segments.length === 2) return items;

    // /pro/:id/:sub  (tickets, fases, historias, editar…)
    const sub      = segments[2];
    const subLabel = PRO_SUB[sub] ?? sub;
    items.push({
      label:      subLabel,
      routerLink: segments.length > 3 ? ['/pro', proyId, sub] : undefined,
    });
    if (segments.length === 3) return items;

    // /pro/:id/:sub/:subId  (p.ej. fases/:faseId)
    const subId = segments[3];
    items.push({
      label:      subId,
      routerLink: segments.length > 4 ? ['/pro', proyId, sub, subId] : undefined,
    });
    if (segments.length === 4) return items;

    // /pro/:id/:sub/:subId/:deepSub  (p.ej. fases/:faseId/actividades)
    const deepSub = segments[4];
    items.push({
      label:      PRO_SUB[deepSub] ?? deepSub,
      routerLink: segments.length > 5 ? ['/pro', proyId, sub, subId, deepSub] : undefined,
    });
    if (segments.length > 5) items.push({ label: segments[5] });

    return items;
  }

  private buildTicketDetalle(
    segments: string[],
    proy:     { id: number; nombre: string } | null,
  ): BreadcrumbItem[] {
    if (segments.length < 2) return [];
    const items: BreadcrumbItem[] = [{ label: 'Proyectos', routerLink: '/pro' }];
    if (proy) {
      items.push({ label: proy.nombre,   routerLink: ['/pro', proy.id] });
      items.push({ label: 'Tickets',     routerLink: ['/pro', proy.id, 'tickets'] });
    }
    items.push({ label: segments[1] }); // folio — página actual, sin enlace
    return items;
  }
}
