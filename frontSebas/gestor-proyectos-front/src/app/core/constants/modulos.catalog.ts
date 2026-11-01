export interface ModuloCatalog {
  ref: string;
  nombre: string;
  icono: string;
  ruta: string;
  tipo: 'SOLO_ADMIN' | 'PRINCIPAL' | 'ROL' | 'GENERAL';
  requiereProyecto: boolean;
}

export const MODULOS_CATALOG: ModuloCatalog[] = [
  { ref: 'MOD-PROYECTOS',     nombre: 'Proyectos',            icono: 'bi-kanban',          ruta: '/pro',             tipo: 'PRINCIPAL',  requiereProyecto: false },
  { ref: 'MOD-USUARIOS',      nombre: 'Usuarios',             icono: 'bi-people',          ruta: '/usuarios',        tipo: 'SOLO_ADMIN', requiereProyecto: false },
  { ref: 'MOD-ROLES',         nombre: 'Roles',                icono: 'bi-shield-check',    ruta: '/roles',           tipo: 'SOLO_ADMIN', requiereProyecto: false },
  { ref: 'MOD-CLIENTES',      nombre: 'Clientes',             icono: 'bi-building',        ruta: '/clientes',        tipo: 'SOLO_ADMIN', requiereProyecto: false },
  { ref: 'MOD-REPOSITORIOS',  nombre: 'Repositorios',         icono: 'bi-git',             ruta: '/repositorios',    tipo: 'SOLO_ADMIN', requiereProyecto: false },
  { ref: 'MOD-BACKLOG',       nombre: 'Backlog',              icono: 'bi-list-task',       ruta: '/backlog',         tipo: 'GENERAL',    requiereProyecto: true  },
  { ref: 'MOD-HISTORIAS',     nombre: 'Historias de usuario', icono: 'bi-journal-text',    ruta: '/historias',       tipo: 'GENERAL',    requiereProyecto: true  },
  { ref: 'MOD-SPRINT',        nombre: 'Sprint Planning',      icono: 'bi-calendar3',       ruta: '/sprint',          tipo: 'GENERAL',    requiereProyecto: true  },
  { ref: 'MOD-EQUIPO',        nombre: 'Equipo del proyecto',  icono: 'bi-people-fill',     ruta: '/equipo',          tipo: 'GENERAL',    requiereProyecto: true  },
  { ref: 'MOD-BUGS',          nombre: 'Bugs',                 icono: 'bi-bug',             ruta: '/bugs',            tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-REQUERIMIENTOS',nombre: 'Requerimientos',       icono: 'bi-journal-text',    ruta: '/requerimientos',  tipo: 'ROL',        requiereProyecto: false },
  { ref: 'MOD-ASIGNACION',    nombre: 'Asignacion',           icono: 'bi-person-check',    ruta: '/asignacion',      tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-REVISION',      nombre: 'Revision',             icono: 'bi-search',          ruta: '/revision',        tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-RELEASES',      nombre: 'Releases',             icono: 'bi-tag',             ruta: '/releases',        tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-MIS-TAREAS',    nombre: 'Mis tareas',           icono: 'bi-check2-square',   ruta: '/mis-tareas',      tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-DESARROLLO',    nombre: 'Desarrollo',           icono: 'bi-code-slash',      ruta: '/desarrollo',      tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-PULL-REQUESTS', nombre: 'Pull Requests',        icono: 'bi-git',             ruta: '/pull-requests',   tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-BRANCHES',      nombre: 'Branches',             icono: 'bi-diagram-2',       ruta: '/branches',        tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-QA',            nombre: 'QA',                   icono: 'bi-clipboard-check', ruta: '/qa',              tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-TICKETS',       nombre: 'Tickets en prueba',    icono: 'bi-ticket',          ruta: '/tickets',         tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-RELEASES-QA',   nombre: 'Releases QA',          icono: 'bi-box-seam',        ruta: '/releases-qa',     tipo: 'ROL',        requiereProyecto: true  },
  { ref: 'MOD-SOLICITUDES',   nombre: 'Solicitudes',          icono: 'bi-folder',          ruta: '/solicitudes',     tipo: 'ROL',        requiereProyecto: false },
];
