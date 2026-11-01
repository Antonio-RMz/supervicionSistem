import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { MisActividadesResponse } from './mis-actividades.models';

@Injectable({ providedIn: 'root' })
export class MisActividadesService {
  private readonly http = inject(HttpClient);
  private readonly url  = `${environment.apiUrl}${API_ENDPOINTS.MIS_ACTIVIDADES.BASE}`;

  obtenerMisActividades(): Observable<MisActividadesResponse> {
    return this.http.get<MisActividadesResponse>(this.url);
  }
}
