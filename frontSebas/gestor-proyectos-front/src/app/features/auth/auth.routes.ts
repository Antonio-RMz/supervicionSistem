import { Routes } from '@angular/router';
import { noAuthGuard } from '../../core/guards/no-auth.guard';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    canActivate: [noAuthGuard],
    loadComponent: () =>
      import('./login/login.component').then((m) => m.LoginComponent),
    title: 'Iniciar Sesión | Gestor de Proyectos',
  },
];
