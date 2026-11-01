import { Routes } from '@angular/router';

export const ROLES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./roles.component').then((m) => m.RolesComponent),
    title: 'Roles | Gestor de Proyectos',
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./detalle/rol-detalle.component').then((m) => m.RolDetalleComponent),
    title: 'Detalle de Rol | Gestor de Proyectos',
  },
];
