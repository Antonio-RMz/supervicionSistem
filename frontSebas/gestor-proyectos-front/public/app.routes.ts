import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layouts/private-layout/private-layout.component').then(
        (m) => m.PrivateLayoutComponent,
      ),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
        title: 'Dashboard | Gestor de Proyectos',
      },
      {
        path: 'sol',
        loadChildren: () => import('./features/sol/sol.routes').then((m) => m.SOL_ROUTES),
      },
      {
        path: 'pro',
        loadChildren: () => import('./features/pro/pro.routes').then((m) => m.PRO_ROUTES),
      },
      {
        path: 'his',
        loadChildren: () => import('./features/his/his.routes').then((m) => m.HIS_ROUTES),
      },
      {
        path: 'qa',
        loadChildren: () => import('./features/qa/qa.routes').then((m) => m.QA_ROUTES),
      },
      {
        path: 'usuarios',
        loadChildren: () =>
          import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
      {
        path: 'roles',
        loadChildren: () => import('./features/roles/roles.routes').then((m) => m.ROLES_ROUTES),
      },
      {
        path: 'permisos',
        loadChildren: () =>
          import('./features/permisos/permisos.routes').then((m) => m.PERMISOS_ROUTES),
      },
      {
        path: 'sesiones',
        loadChildren: () =>
          import('./features/sesiones/sesiones.routes').then((m) => m.SESIONES_ROUTES),
      },
      {
        path: 'configuracion',
        loadChildren: () =>
          import('./features/configuracion/configuracion.routes').then(
            (m) => m.CONFIGURACION_ROUTES,
          ),
      },
    ],
  },
  {
    path: '401',
    loadComponent: () =>
      import('./features/errors/unauthorized/unauthorized.component').then(
        (m) => m.UnauthorizedComponent,
      ),
    title: '401 No Autorizado',
  },
  {
    path: '403',
    loadComponent: () =>
      import('./features/errors/forbidden/forbidden.component').then((m) => m.ForbiddenComponent),
    title: '403 Acceso Denegado',
  },
  {
    path: '404',
    loadComponent: () =>
      import('./features/errors/not-found/not-found.component').then((m) => m.NotFoundComponent),
    title: '404 No Encontrado',
  },
  {
    path: '500',
    loadComponent: () =>
      import('./features/errors/server-error/server-error.component').then(
        (m) => m.ServerErrorComponent,
      ),
    title: '500 Error del Servidor',
  },
  {
    path: '**',
    redirectTo: '404',
  },
];
