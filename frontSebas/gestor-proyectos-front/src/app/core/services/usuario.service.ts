import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import { MensajeResponse } from '../models/mensaje-response.model';
import { RolItem, UsuarioListItem, UsuarioRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}${API_ENDPOINTS.USUARIOS.BASE}`;
  private readonly rolesBase = `${environment.apiUrl}${API_ENDPOINTS.ROLES.BASE}`;

  getRoles(): Observable<RolItem[]> {
    return this.http.get<RolItem[]>(this.rolesBase);
  }

  getAll(): Observable<UsuarioListItem[]> {
    return this.http.get<UsuarioListItem[]>(this.base);
  }

  getById(id: number): Observable<UsuarioListItem> {
    return this.http.get<UsuarioListItem>(`${this.base}/${id}`);
  }

  create(data: UsuarioRequest): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(this.base, data);
  }

  update(id: number, data: UsuarioRequest): Observable<MensajeResponse> {
    return this.http.put<MensajeResponse>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    // responseType: 'text' evita que Angular intente parsear el cuerpo vacío del 204.
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' }).pipe(
      map(() => void 0),
    );
  }
}
