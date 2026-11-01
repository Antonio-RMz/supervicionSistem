import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { UsuarioResponse } from '../models/usuario-response.model';
import { ProyectoResponse } from '../models/proyecto.model';
import { ModuloBackend } from '../models/auth-response.model';
import { STORAGE_KEYS } from '../constants/api.constants';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly _token          = signal<string | null>(null);
  private readonly _usuario        = signal<UsuarioResponse | null>(null);
  private readonly _proyectoActivo = signal<ProyectoResponse | null>(null);
  private readonly _proyectoId     = signal<number | null>(null);
  private readonly _modulos        = signal<ModuloBackend[]>([]);

  readonly token          = this._token.asReadonly();
  readonly usuario        = this._usuario.asReadonly();
  readonly proyectoActivo = this._proyectoActivo.asReadonly();
  readonly modulos        = this._modulos.asReadonly();

  // Prefiere el ID persistido; si no, usa el del objeto en memoria.
  readonly proyectoId = computed(
    () => this._proyectoId() ?? this._proyectoActivo()?.id ?? null,
  );

  get isAuthenticated(): boolean {
    return !!this._token() && !this.isTokenExpired();
  }

  // ─── Token ────────────────────────────────────────────────────────────────
  guardarToken(token: string, expiracion: number): void {
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEYS.TOKEN, token);
      localStorage.setItem(STORAGE_KEYS.EXPIRATION, expiracion.toString());
    }
    this._token.set(token);
  }

  getToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  }

  // ─── Usuario ──────────────────────────────────────────────────────────────
  guardarUsuario(usuario: UsuarioResponse): void {
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(usuario));
    }
    this._usuario.set(usuario);
  }

  // ─── Módulos ──────────────────────────────────────────────────────────────
  setModulos(modulos: ModuloBackend[]): void {
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEYS.MODULOS, JSON.stringify(modulos));
    }
    this._modulos.set(modulos);
  }

  getModulos(): ModuloBackend[] {
    return this._modulos();
  }

  clearModulos(): void {
    if (this.isBrowser) {
      localStorage.removeItem(STORAGE_KEYS.MODULOS);
    }
    this._modulos.set([]);
  }

  // ─── Proyecto activo ──────────────────────────────────────────────────────
  setProyectoActivo(id: number): void {
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEYS.PROYECTO_ID, String(id));
    }
    this._proyectoId.set(id);
  }

  getProyectoActivo(): number | null {
    return this._proyectoId();
  }

  clearProyectoActivo(): void {
    if (this.isBrowser) {
      localStorage.removeItem(STORAGE_KEYS.PROYECTO_ID);
    }
    this._proyectoId.set(null);
  }

  // Compatibilidad con código existente que pasa el objeto completo
  seleccionarProyecto(proyecto: ProyectoResponse): void {
    this._proyectoActivo.set(proyecto);
    this.setProyectoActivo(proyecto.id);
  }

  deseleccionarProyecto(): void {
    this._proyectoActivo.set(null);
    this.clearProyectoActivo();
  }

  // ─── Recuperar sesión (page refresh) ──────────────────────────────────────
  recuperarSesion(): void {
    if (!this.isBrowser) return;

    const token      = localStorage.getItem(STORAGE_KEYS.TOKEN);
    const userRaw    = localStorage.getItem(STORAGE_KEYS.USER);
    const modulosRaw = localStorage.getItem(STORAGE_KEYS.MODULOS);
    const proyectoId = localStorage.getItem(STORAGE_KEYS.PROYECTO_ID);

    if (token) this._token.set(token);

    if (userRaw) {
      try {
        this._usuario.set(JSON.parse(userRaw));
      } catch {
        this.limpiarSesion();
        return;
      }
    }

    if (modulosRaw) {
      try {
        this._modulos.set(JSON.parse(modulosRaw));
      } catch {
        localStorage.removeItem(STORAGE_KEYS.MODULOS);
      }
    }

    if (proyectoId) {
      const id = parseInt(proyectoId, 10);
      if (!isNaN(id)) this._proyectoId.set(id);
    }
  }

  // ─── Limpiar todo ─────────────────────────────────────────────────────────
  limpiarSesion(): void {
    if (this.isBrowser) {
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      localStorage.removeItem(STORAGE_KEYS.EXPIRATION);
      localStorage.removeItem(STORAGE_KEYS.MODULOS);
      localStorage.removeItem(STORAGE_KEYS.PROYECTO_ID);
    }
    this._token.set(null);
    this._usuario.set(null);
    this._proyectoActivo.set(null);
    this._proyectoId.set(null);
    this._modulos.set([]);
  }

  private isTokenExpired(): boolean {
    if (!this.isBrowser) return true;
    const exp = localStorage.getItem(STORAGE_KEYS.EXPIRATION);
    if (!exp) return true;
    return Date.now() > parseInt(exp, 10);
  }
}
