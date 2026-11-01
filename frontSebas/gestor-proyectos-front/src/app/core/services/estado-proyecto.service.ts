import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import { EstadoProyecto } from '../models/configuracion.model';

@Injectable({ providedIn: 'root' })
export class EstadoProyectoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}${API_ENDPOINTS.CONFIGURACION.ESTADO_PROYECTO}`;

  getAll(): Observable<EstadoProyecto[]> {
    return this.http.get<EstadoProyecto[]>(this.url);
  }
}
