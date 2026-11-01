import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AvanceProyecto, MiProyecto } from '../models/portal.models';
import { AvanceDto, MiProyectoDto } from './portal-cliente.dto';
import { aAvance, aMiProyecto } from './portal-cliente.mapper';

/** Único lugar del portal que habla con el back. Las pantallas reciben modelos, nunca DTOs. */
@Injectable({ providedIn: 'root' })
export class PortalClienteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/portal`;

  misProyectos(): Observable<MiProyecto[]> {
    return this.http.get<MiProyectoDto[]>(`${this.url}/proyectos`).pipe(map((lista) => lista.map(aMiProyecto)));
  }

  avance(proyectoId: number): Observable<AvanceProyecto> {
    return this.http.get<AvanceDto>(`${this.url}/proyectos/${proyectoId}/avance`).pipe(map(aAvance));
  }
}
