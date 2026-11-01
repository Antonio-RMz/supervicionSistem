import { Routes } from '@angular/router';

export const PRO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pro.component').then((m) => m.ProComponent),
    title: 'Proyectos | Gestor de Proyectos',
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./nuevo/proyecto-nuevo.component').then((m) => m.ProyectoNuevoComponent),
    title: 'Nuevo Proyecto | Gestor de Proyectos',
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./detalle/proyecto-detalle.component').then((m) => m.ProyectoDetalleComponent),
    title: 'Detalle del Proyecto | Gestor de Proyectos',
  },
  {
    path: ':id/fases',
    loadComponent: () =>
      import('./fases/fases.component').then((m) => m.FasesComponent),
    title: 'Fases | Gestor de Proyectos',
  },
  {
    path: ':id/fases/:faseId',
    loadComponent: () =>
      import('./fases/fase-detalle/fase-detalle.component').then((m) => m.FaseDetalleComponent),
    title: 'Detalle de Fase | Gestor de Proyectos',
  },
  {
    path: ':id/fases/:faseId/actividades/:actividadId',
    loadComponent: () =>
      import('./actividades/actividad-detalle/actividad-detalle.component').then((m) => m.ActividadDetalleComponent),
    title: 'Detalle de Actividad | Gestor de Proyectos',
  },
  {
    path: ':id/actividades',
    loadComponent: () =>
      import('./actividades/actividades.component').then((m) => m.ActividadesComponent),
    title: 'Actividades | Gestor de Proyectos',
  },
  {
    path: ':id/backlog',
    loadComponent: () =>
      import('./backlog/backlog.component').then((m) => m.BacklogComponent),
    title: 'Backlog | Gestor de Proyectos',
  },
  {
    path: ':id/historias',
    loadComponent: () =>
      import('./historias/historias.component').then((m) => m.HistoriasComponent),
    title: 'Historias | Gestor de Proyectos',
  },
  {
    path: ':id/historias/:historiaId',
    loadComponent: () =>
      import('./historias/historia-detalle/historia-detalle.component').then((m) => m.HistoriaDetalleComponent),
    title: 'Detalle de Historia | Gestor de Proyectos',
  },
  {
    path: ':id/sprint',
    loadComponent: () =>
      import('./sprint/sprint.component').then((m) => m.SprintComponent),
    title: 'Sprint Planning | Gestor de Proyectos',
  },
  {
    path: ':id/equipo',
    loadComponent: () =>
      import('./equipo/equipo.component').then((m) => m.EquipoComponent),
    title: 'Equipo del Proyecto | Gestor de Proyectos',
  },
  {
    path: ':id/tickets',
    loadComponent: () =>
      import('./tickets/proyecto-tickets.component').then((m) => m.ProyectoTicketsComponent),
    title: 'Tickets | Gestor de Proyectos',
  },
  {
    path: ':id/tickets/:folio',
    loadComponent: () =>
      import('./tickets/detalle/ticket-detalle.component').then((m) => m.TicketDetalleComponent),
    title: 'Detalle de Ticket | Gestor de Proyectos',
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./editar/proyecto-editar.component').then((m) => m.ProyectoEditarComponent),
    title: 'Editar Proyecto | Gestor de Proyectos',
  },
];
