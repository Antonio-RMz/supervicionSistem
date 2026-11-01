import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Candidato, EquipoProyecto } from '../models/equipo.models';
import { CandidatoDto, EquipoDto } from './equipo.dto';
import { aCandidato, aEquipo } from './equipo.mapper';

/** Pestaña Equipo del proyecto (PDF pág. 34). Agregar, quitar y reasignar los hacen el Líder o un Admin. */
@Injectable({ providedIn: 'root' })
export class EquipoService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/equipo`;
  }

  equipo(proyectoId: number): Observable<EquipoProyecto> {
    return this.http.get<EquipoDto>(this.url(proyectoId)).pipe(map((dto) => aEquipo(dto)));
  }

  candidatos(proyectoId: number): Observable<Candidato[]> {
    return this.http.get<CandidatoDto[]>(`${this.url(proyectoId)}/candidatos`).pipe(map((c) => c.map(aCandidato)));
  }

  agregar(proyectoId: number, usuarioId: number): Observable<void> {
    return this.http.post<void>(this.url(proyectoId), { usuarioId });
  }

  quitar(proyectoId: number, miembroId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId)}/${miembroId}`);
  }

  /** @returns cuántos tickets cambiaron de manos */
  reasignar(proyectoId: number, miembroId: number, responsableNuevoId: number | null, testerNuevoId: number | null): Observable<number> {
    return this.http
      .post<{ tickets: number }>(`${this.url(proyectoId)}/${miembroId}/reasignar`, { responsableNuevoId, testerNuevoId })
      .pipe(map((r) => r.tickets));
  }
}
