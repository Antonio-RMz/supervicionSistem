import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Dashboard } from '../models/dashboard.models';
import { DashboardDto } from './dashboard.dto';
import { aDashboard } from './dashboard.mapper';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  dashboard(proyectoId: number): Observable<Dashboard> {
    return this.http
      .get<DashboardDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/dashboard`)
      .pipe(map(aDashboard));
  }
}
