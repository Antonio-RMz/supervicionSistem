import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { PortalClienteStore } from './data/portal-cliente.store';
import { PortalLayout } from './layout/portal-layout';

/** "Avance" del menú: abre el proyecto que estabas viendo (o el primero). */
const irAlAvance: CanActivateFn = async () => {
  const store = inject(PortalClienteStore);
  const router = inject(Router);
  await store.cargar().catch(() => []);
  const proyecto = store.seleccionado();
  return proyecto ? router.createUrlTree(['/portal/proyectos', proyecto.id]) : router.createUrlTree(['/portal/proyectos']);
};

export const PORTAL_CLIENTE_ROUTES: Routes = [
  {
    path: '',
    component: PortalLayout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'proyectos' },
      {
        path: 'proyectos',
        loadComponent: () => import('./pages/mis-proyectos/mis-proyectos-page').then((m) => m.MisProyectosPage),
        title: 'Mis proyectos · Portal de avance',
      },
      {
        path: 'proyectos/:id',
        loadComponent: () => import('./pages/avance/avance-page').then((m) => m.AvancePage),
        title: 'Avance · Portal de avance',
      },
      { path: 'avance', canActivate: [irAlAvance], children: [] },
      {
        path: 'solicitudes',
        loadComponent: () => import('./pages/proximamente/proximamente-page').then((m) => m.ProximamentePage),
        data: { titulo: 'Solicitudes' },
        title: 'Solicitudes · Portal de avance',
      },
      {
        path: 'versiones',
        loadComponent: () => import('./pages/proximamente/proximamente-page').then((m) => m.ProximamentePage),
        data: { titulo: 'Versiones' },
        title: 'Versiones · Portal de avance',
      },
    ],
  },
];
