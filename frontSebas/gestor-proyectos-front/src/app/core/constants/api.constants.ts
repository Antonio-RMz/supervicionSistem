export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    ME: '/auth/me',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
  },
  PROYECTOS: {
    BASE: '/proyectos',
  },
  USUARIOS: {
    BASE: '/usuario',
  },
  ROLES: {
    BASE: '/roles',
  },
  CLIENTES: {
    BASE: '/clientes',
  },
  CONFIGURACION: {
    TIPO_PROYECTO:      '/confi/tipoProyecto',
    ESTADO_PROYECTO:    '/confi/estadoProyecto',
    PRIORIDAD_PROYECTO: '/confi/prioridadProyecto',
  },
  TICKETS: {
    BASE: '/tickets',
  },
  MIS_ACTIVIDADES: {
    BASE: '/mis-actividades',
  },
} as const;

export const STORAGE_KEYS = {
  TOKEN: 'gp_token',
  USER: 'gp_user',
  EXPIRATION: 'gp_exp',
  MODULOS: 'gp_modulos',
  PROYECTO_ID: 'gp_proyecto_id',
} as const;

export const APP_ROUTES = {
  LOGIN: '/auth/login',
  DASHBOARD: '/dashboard',
  UNAUTHORIZED: '/401',
  FORBIDDEN: '/403',
  NOT_FOUND: '/404',
  SERVER_ERROR: '/500',
} as const;
