import { Routes } from '@angular/router';
import { clienteGuard, equipoGuard, invitadoGuard } from './core/auth/auth.guards';

export const routes: Routes = [
  // Acceso del equipo (Gestor de Proyectos)
  {
    path: 'login',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/gestor/pages/login-equipo/login-equipo-page').then((m) => m.LoginEquipoPage),
    title: 'Entrar · Gestor de Proyectos',
  },
  // Acceso de clientes (Portal de avance)
  {
    path: 'cliente/login',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/portal-cliente/pages/login/login-page').then((m) => m.LoginPage),
    title: 'Entrar · Portal de avance',
  },
  {
    path: 'portal',
    canActivate: [clienteGuard],
    loadChildren: () =>
      import('./features/portal-cliente/portal-cliente.routes').then((m) => m.PORTAL_CLIENTE_ROUTES),
  },
  {
    path: '',
    canActivate: [equipoGuard],
    loadChildren: () => import('./features/gestor/gestor.routes').then((m) => m.GESTOR_ROUTES),
  },
  { path: '**', redirectTo: '' },
];
