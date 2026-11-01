import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { RolItem } from '../../core/models/usuario.model';

export interface Permiso {
  id: number;
  nombre: string;
  descripcion: string;
  metodoHttp: 'GET' | 'POST' | 'PUT' | 'DELETE';
  path: string;
  activo: boolean;
}

export interface RolPermisoUpdateRequest {
  agregar: number[];
  quitar: number[];
}

@Injectable({ providedIn: 'root' })
export class RolesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}${API_ENDPOINTS.ROLES.BASE}`;

  getAll(): Observable<RolItem[]> {
    return this.http.get<RolItem[]>(this.base);
  }

  getPermisos(idRol: number): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(`${this.base}/${idRol}/permisos`);
  }

  updatePermisos(idRol: number, body: RolPermisoUpdateRequest): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.base}/${idRol}/permisos`, body);
  }
}
