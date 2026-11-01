import { Routes } from '@angular/router';

export const PERMISOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./permisos.component').then((m) => m.PermisosComponent),
    title: 'Permisos | Gestor de Proyectos',
  },
];
