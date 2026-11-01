import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Usuario } from './auth.models';
import { AuthService } from './auth.service';

/** A dónde va cada quien después de entrar. */
export function rutaInicio(usuario: Usuario): string {
  return usuario.tipo === 'CLIENTE' ? '/portal' : '/dashboard';
}

/** Gestor de Proyectos: solo el equipo. Un cliente se va a su portal. */
export const equipoGuard: CanActivateFn = () => {
  const usuario = inject(AuthService).usuario();
  if (usuario?.tipo === 'EQUIPO') return true;
  return inject(Router).createUrlTree([usuario ? rutaInicio(usuario) : '/login']);
};

/** Portal de avance: solo clientes. Alguien del equipo se va a su Dashboard. */
export const clienteGuard: CanActivateFn = () => {
  const usuario = inject(AuthService).usuario();
  if (usuario?.tipo === 'CLIENTE') return true;
  return inject(Router).createUrlTree([usuario ? rutaInicio(usuario) : '/cliente/login']);
};

/** Usuarios y roles: solo el Administrador. Los demás regresan a su Dashboard. */
export const adminGuard: CanActivateFn = () => {
  const usuario = inject(AuthService).usuario();
  return usuario?.roles.includes('ADMIN') ? true : inject(Router).createUrlTree(['/dashboard']);
};

/** Las pantallas de login solo se ven sin sesión. */
export const invitadoGuard: CanActivateFn = () => {
  const usuario = inject(AuthService).usuario();
  return usuario ? inject(Router).createUrlTree([rutaInicio(usuario)]) : true;
};
