import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, finalize, Observable, of, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SessionService } from './session.service';
import { LoginRequest } from '../models/login-request.model';
import { AuthResponse } from '../models/auth-response.model';
import { UsuarioResponse } from '../models/usuario-response.model';
import { MensajeResponse } from '../models/mensaje-response.model';
import { RefreshResponse } from '../models/refresh-response.model';
import { API_ENDPOINTS, APP_ROUTES } from '../constants/api.constants';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  private readonly base = environment.apiUrl;

  login(credentials: LoginRequest): Observable<UsuarioResponse> {
    return this.http
      .post<AuthResponse>(`${this.base}${API_ENDPOINTS.AUTH.LOGIN}`, credentials)
      .pipe(
        tap((res) => {
          this.session.guardarToken(res.token, res.expiracion);
          if (res.modulos?.length) {
            this.session.setModulos(res.modulos);
          }
        }),
        switchMap(() => this.me()),
      );
  }

  me(): Observable<UsuarioResponse> {
    return this.http
      .get<UsuarioResponse>(`${this.base}${API_ENDPOINTS.AUTH.ME}`)
      .pipe(tap((usuario) => this.session.guardarUsuario(usuario)));
  }

  logout(): Observable<MensajeResponse | null> {
    return this.http
      .post<MensajeResponse>(`${this.base}${API_ENDPOINTS.AUTH.LOGOUT}`, {})
      .pipe(
        catchError(() => of(null)),
        finalize(() => {
          this.session.limpiarSesion();
          this.router.navigate([APP_ROUTES.LOGIN]);
        }),
      );
  }

  refresh(): Observable<RefreshResponse> {
    return this.http
      .post<RefreshResponse>(`${this.base}${API_ENDPOINTS.AUTH.REFRESH}`, {})
      .pipe(tap((res) => this.session.guardarToken(res.token, res.expiracion)));
  }
}
