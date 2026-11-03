// Nombres y colores de los roles del equipo. Los roles viajan del back como código (ADMIN, ANALISTA...).

import { Etiqueta, Tono } from '../models/etiqueta.model';

/** En este orden se muestran siempre. */
export const ORDEN_ROL = ['ADMIN', 'LIDER', 'ANALISTA', 'DESARROLLADOR'];

/** "Administrador · Líder" (menú lateral, encabezados). */
const NOMBRE_ROL: Record<string, string> = {
  ADMIN: 'Administrador',
  LIDER: 'Líder',
  ANALISTA: 'Analista',
  DESARROLLADOR: 'Desarrollador',
};

/** Pastillas de la tabla de usuarios: "Admin" en rojo, "Analista" en violeta... */
const ETIQUETA_ROL: Record<string, Etiqueta> = {
  ADMIN: { texto: 'Admin', tono: 'rojo' },
  LIDER: { texto: 'Líder', tono: 'morado' },
  ANALISTA: { texto: 'Analista', tono: 'violeta' },
  DESARROLLADOR: { texto: 'Desarrollador', tono: 'verde' },
};

export function ordenarRoles(roles: readonly string[]): string[] {
  const posicion = (rol: string) => (ORDEN_ROL.indexOf(rol) + 1 || ORDEN_ROL.length + 1);
  return [...roles].sort((a, b) => posicion(a) - posicion(b));
}

/** ['LIDER', 'ADMIN'] -> "Administrador · Líder" */
export function nombresDeRoles(roles: readonly string[]): string {
  return ordenarRoles(roles).map((rol) => NOMBRE_ROL[rol] ?? rol).join(' · ');
}

export function etiquetaDeRol(rol: string): Etiqueta {
  return ETIQUETA_ROL[rol] ?? { texto: rol, tono: 'gris' };
}

const TONOS_AVATAR: Tono[] = ['morado', 'violeta', 'verde', 'azul', 'turquesa', 'naranja'];

/** Cada persona con su color de avatar, siempre el mismo. Las cuentas inactivas, en gris. */
export function tonoDeAvatar(id: number, activo = true): Tono {
  return activo ? TONOS_AVATAR[id % TONOS_AVATAR.length] : 'gris';
}

/** El Administrador ve todo; los demás, si tienen alguno de esos roles. */
function tieneRol(mios: readonly string[], ...roles: string[]): boolean {
  return mios.includes('ADMIN') || roles.some((r) => mios.includes(r));
}

/** Prompt de desarrollo de una historia: Líder y Desarrollador. */
export function puedePromptDesarrollo(roles: readonly string[]): boolean {
  return tieneRol(roles, 'LIDER', 'DESARROLLADOR');
}

/** Prompt de diseño de una historia (Figma): Líder y Analista. */
export function puedePromptDiseno(roles: readonly string[]): boolean {
  return tieneRol(roles, 'LIDER', 'ANALISTA');
}
