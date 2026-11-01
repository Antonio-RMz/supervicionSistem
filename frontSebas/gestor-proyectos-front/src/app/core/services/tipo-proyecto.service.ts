import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import { TipoProyecto } from '../models/configuracion.model';

@Injectable({ providedIn: 'root' })
export class TipoProyectoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}${API_ENDPOINTS.CONFIGURACION.TIPO_PROYECTO}`;

  getAll(): Observable<TipoProyecto[]> {
    return this.http.get<TipoProyecto[]>(this.url);
  }
}
