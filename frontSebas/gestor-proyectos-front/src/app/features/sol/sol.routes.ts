import { Routes } from '@angular/router';

export const SOL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sol.component').then((m) => m.SolComponent),
    title: 'Solicitudes | Gestor de Proyectos',
  },
];
