import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Tablero } from '../models/tablero.models';
import { TableroDto } from './tablero.dto';
import { aTablero } from './tablero.mapper';

@Injectable({ providedIn: 'root' })
export class TableroService {
  private readonly http = inject(HttpClient);

  /** Sin faseId, el back elige la fase en curso. */
  tablero(proyectoId: number, faseId: number | null): Observable<Tablero> {
    const params = faseId ? new HttpParams().set('faseId', faseId) : undefined;
    return this.http
      .get<TableroDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tablero`, { params })
      .pipe(map(aTablero));
  }
}
