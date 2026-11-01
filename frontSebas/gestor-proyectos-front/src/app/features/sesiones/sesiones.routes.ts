import { Routes } from '@angular/router';

export const SESIONES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sesiones.component').then((m) => m.SesionesComponent),
    title: 'Sesiones Activas | Gestor de Proyectos',
  },
];
