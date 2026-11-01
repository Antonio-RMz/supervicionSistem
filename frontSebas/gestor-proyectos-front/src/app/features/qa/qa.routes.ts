import { Routes } from '@angular/router';

export const QA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./qa.component').then((m) => m.QaComponent),
    title: 'Control de Calidad | Gestor de Proyectos',
  },
];
