import { Routes } from '@angular/router';

export const HIS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./his.component').then((m) => m.HisComponent),
    title: 'Historial | Gestor de Proyectos',
  },
];
