import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ReleasesProyecto } from '../models/releases.models';
import { ReleasesDto } from './releases.dto';
import { aReleases } from './releases.mapper';

/** Pestaña Releases del proyecto (PDF pág. 36). */
@Injectable({ providedIn: 'root' })
export class ReleasesService {
  private readonly http = inject(HttpClient);

  releases(proyectoId: number): Observable<ReleasesProyecto> {
    return this.http
      .get<ReleasesDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/releases`)
      .pipe(map(aReleases));
  }
}
