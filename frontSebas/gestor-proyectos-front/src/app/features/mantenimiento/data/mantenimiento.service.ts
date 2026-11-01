import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Mantenimiento, PreparacionMantenimiento } from '../models/mantenimiento.models';
import { MantenimientoDto, NuevoTicketMantenimientoRequest, PreparacionMantenimientoDto, ReporteFallaRequest } from './mantenimiento.dto';
import { aMantenimiento, aPreparacion } from './mantenimiento.mapper';

/** Vista de Mantenimiento del proyecto (PDF pág. 37) y sus modales (págs. 38 y 39). */
@Injectable({ providedIn: 'root' })
export class MantenimientoService {
  private readonly http = inject(HttpClient);

  mantenimiento(proyectoId: number): Observable<Mantenimiento> {
    return this.http
      .get<MantenimientoDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mantenimiento`)
      .pipe(map(aMantenimiento));
  }

  preparar(proyectoId: number): Observable<PreparacionMantenimiento> {
    return this.http
      .get<PreparacionMantenimientoDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mantenimiento/nuevo`)
      .pipe(map(aPreparacion));
  }

  /** Devuelve el id del ticket nuevo. */
  crearTicket(proyectoId: number, ticket: NuevoTicketMantenimientoRequest): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mantenimiento/tickets`, ticket)
      .pipe(map((r) => r.id));
  }

  /** Devuelve el id del bug nuevo. */
  reportarFalla(proyectoId: number, reporte: ReporteFallaRequest): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mantenimiento/fallas`, reporte)
      .pipe(map((r) => r.id));
  }
}
