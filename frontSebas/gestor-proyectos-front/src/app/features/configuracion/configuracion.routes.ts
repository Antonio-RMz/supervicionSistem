import { Routes } from '@angular/router';

export const CONFIGURACION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./configuracion.component').then((m) => m.ConfiguracionComponent),
    title: 'Configuración | Gestor de Proyectos',
  },
];
