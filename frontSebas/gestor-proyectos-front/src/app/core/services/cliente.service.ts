import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import { ClienteRequest, ClienteResponse } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}${API_ENDPOINTS.CLIENTES.BASE}`;

  obtenerTodos(): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(this.base);
  }

  obtenerPorId(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.base}/${id}`);
  }

  crear(dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(this.base, dto);
  }

  actualizar(id: number, dto: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.base}/${id}`, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' }).pipe(
      map(() => void 0),
    );
  }
}
