import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import { PrioridadProyecto } from '../models/configuracion.model';

@Injectable({ providedIn: 'root' })
export class PrioridadProyectoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}${API_ENDPOINTS.CONFIGURACION.PRIORIDAD_PROYECTO}`;

  getAll(): Observable<PrioridadProyecto[]> {
    return this.http.get<PrioridadProyecto[]>(this.url);
  }
}
