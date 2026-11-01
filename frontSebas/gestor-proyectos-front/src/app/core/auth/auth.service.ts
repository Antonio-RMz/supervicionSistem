import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequestDto, LoginResponseDto } from './auth.dto';
import { aSesion } from './auth.mapper';
import { Sesion } from './auth.models';

const CLAVE = 'sesion';

/** Guarda quién entró. "Recordarme" usa localStorage; si no, sessionStorage (se borra al cerrar el navegador). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesionActual = signal<Sesion | null>(leerGuardada());

  readonly usuario = computed(() => this.sesionActual()?.usuario ?? null);
  readonly token = computed(() => this.sesionActual()?.token ?? null);

  iniciarSesion(correo: string, password: string, recordarme: boolean): Observable<Sesion> {
    const body: LoginRequestDto = { correo, password };
    return this.http.post<LoginResponseDto>(`${environment.apiUrl}/auth/login`, body).pipe(
      map(aSesion),
      tap((sesion) => this.guardar(sesion, recordarme)),
    );
  }

  /** Regresa al login de donde vino: el cliente al del portal, el equipo al del Gestor. */
  cerrarSesion(): void {
    const eraCliente = this.usuario()?.tipo === 'CLIENTE';
    localStorage.removeItem(CLAVE);
    sessionStorage.removeItem(CLAVE);
    this.sesionActual.set(null);
    this.router.navigate([eraCliente ? '/cliente/login' : '/login']);
  }

  private guardar(sesion: Sesion, recordarme: boolean): void {
    (recordarme ? localStorage : sessionStorage).setItem(CLAVE, JSON.stringify(sesion));
    this.sesionActual.set(sesion);
  }
}

function leerGuardada(): Sesion | null {
  try {
    const texto = localStorage.getItem(CLAVE) ?? sessionStorage.getItem(CLAVE);
    return texto ? (JSON.parse(texto) as Sesion) : null;
  } catch {
    return null;
  }
}
