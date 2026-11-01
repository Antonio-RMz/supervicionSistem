import { Routes } from '@angular/router';
import { adminGuard } from '../../core/auth/auth.guards';
import { GestorLayout } from './layout/gestor-layout';

export const GESTOR_ROUTES: Routes = [
  {
    path: '',
    component: GestorLayout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('../dashboard/pages/dashboard/dashboard-page').then((m) => m.DashboardPage),
        title: 'Dashboard · Gestor de Proyectos',
      },
      {
        path: 'mis-tickets',
        loadComponent: () => import('../mis-tickets/pages/mis-tickets/mis-tickets-page').then((m) => m.MisTicketsPage),
        title: 'Mis tickets · Gestor de Proyectos',
      },
      {
        path: 'tablero',
        loadComponent: () => import('../tablero/pages/tablero/tablero-page').then((m) => m.TableroPage),
        title: 'Tablero · Gestor de Proyectos',
      },
      {
        path: 'fases',
        loadComponent: () => import('../fases/pages/lista-fases/lista-fases-page').then((m) => m.ListaFasesPage),
        title: 'Fases · Gestor de Proyectos',
      },
      {
        path: 'historias',
        loadComponent: () =>
          import('../historias/pages/lista-historias/lista-historias-page').then((m) => m.ListaHistoriasPage),
        title: 'Historias · Gestor de Proyectos',
      },
      {
        path: 'proyectos',
        loadComponent: () => import('../proyectos/pages/lista/proyectos-page').then((m) => m.ProyectosPage),
        title: 'Proyectos · Gestor de Proyectos',
      },
      {
        path: 'proyectos/nuevo',
        loadComponent: () =>
          import('../proyectos/pages/formulario/proyecto-form-page').then((m) => m.ProyectoFormPage),
        title: 'Nuevo proyecto · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/editar',
        loadComponent: () =>
          import('../proyectos/pages/formulario/proyecto-form-page').then((m) => m.ProyectoFormPage),
        title: 'Editar proyecto · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/mantenimiento',
        loadComponent: () =>
          import('../mantenimiento/pages/mantenimiento/mantenimiento-page').then((m) => m.MantenimientoPage),
        title: 'Mantenimiento · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/fases/nueva',
        loadComponent: () => import('../fases/pages/nueva-fase/nueva-fase-page').then((m) => m.NuevaFasePage),
        title: 'Nueva fase · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/fases/:faseId/editar',
        loadComponent: () => import('../fases/pages/nueva-fase/nueva-fase-page').then((m) => m.NuevaFasePage),
        title: 'Editar fase · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/fases/:faseId/release',
        loadComponent: () => import('../fases/pages/release-fase/release-fase-page').then((m) => m.ReleaseFasePage),
        title: 'Release · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/fases/:faseId/importar',
        loadComponent: () =>
          import('../historias/pages/importar-historias/importar-historias-page').then((m) => m.ImportarHistoriasPage),
        title: 'Importar historias · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/fases/:faseId',
        loadComponent: () =>
          import('../fases/pages/detalle-fase/detalle-fase-page').then((m) => m.DetalleFasePage),
        title: 'Fase · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/historias/nueva',
        loadComponent: () =>
          import('../historias/pages/nueva-historia/nueva-historia-page').then((m) => m.NuevaHistoriaPage),
        title: 'Nueva historia · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/historias/:historiaId/editar',
        loadComponent: () =>
          import('../historias/pages/nueva-historia/nueva-historia-page').then((m) => m.NuevaHistoriaPage),
        title: 'Editar historia · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/historias/:historiaId/importar-tickets',
        loadComponent: () =>
          import('../tickets/pages/importar-tickets/importar-tickets-page').then((m) => m.ImportarTicketsPage),
        title: 'Importar tickets · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/historias/:historiaId/integrar',
        loadComponent: () =>
          import('../historias/pages/integrar-historia/integrar-historia-page').then((m) => m.IntegrarHistoriaPage),
        title: 'Integrar historia · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/historias/:historiaId',
        loadComponent: () =>
          import('../historias/pages/detalle-historia/detalle-historia-page').then((m) => m.DetalleHistoriaPage),
        title: 'Historia · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/tickets/nuevo',
        loadComponent: () => import('../tickets/pages/nuevo-ticket/nuevo-ticket-page').then((m) => m.NuevoTicketPage),
        title: 'Nuevo ticket · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/tickets/:ticketId/editar',
        loadComponent: () => import('../tickets/pages/nuevo-ticket/nuevo-ticket-page').then((m) => m.NuevoTicketPage),
        title: 'Editar ticket · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id/tickets/:ticketId',
        loadComponent: () => import('../tickets/pages/detalle-ticket/detalle-ticket-page').then((m) => m.DetalleTicketPage),
        title: 'Ticket · Gestor de Proyectos',
      },
      {
        path: 'proyectos/:id',
        loadComponent: () => import('../proyectos/pages/detalle/proyecto-layout').then((m) => m.ProyectoLayout),
        title: 'Proyecto · Gestor de Proyectos',
        children: [
          {
            path: '',
            loadComponent: () => import('../proyectos/pages/resumen/resumen-tab').then((m) => m.ResumenTab),
          },
          {
            path: 'fases',
            loadComponent: () => import('../proyectos/pages/fases/fases-tab').then((m) => m.FasesTab),
          },
          {
            path: 'equipo',
            loadComponent: () => import('../proyectos/pages/equipo/equipo-tab').then((m) => m.EquipoTab),
          },
          {
            path: 'usuarios-sistema',
            loadComponent: () =>
              import('../proyectos/pages/usuarios-sistema/usuarios-sistema-tab').then((m) => m.UsuariosSistemaTab),
          },
          {
            path: 'modulos',
            loadComponent: () => import('../proyectos/pages/modulos/modulos-tab').then((m) => m.ModulosTab),
          },
          {
            path: 'releases',
            loadComponent: () => import('../proyectos/pages/releases/releases-tab').then((m) => m.ReleasesTab),
          },
        ],
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('../usuarios/pages/usuarios/usuarios-page').then((m) => m.UsuariosPage),
        title: 'Usuarios y roles · Gestor de Proyectos',
      },
      {
        path: 'usuarios/:id',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('../usuarios/pages/perfil-usuario/perfil-usuario-page').then((m) => m.PerfilUsuarioPage),
        title: 'Usuario · Gestor de Proyectos',
      },
      {
        path: 'clientes',
        canActivate: [adminGuard],
        loadComponent: () => import('../clientes/pages/clientes/clientes-page').then((m) => m.ClientesPage),
        title: 'Clientes · Gestor de Proyectos',
      },
    ],
  },
];
