import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import {
  ActividadProyectoResponse,
  MiembroProyectoResponse,
  PaginaResponse,
  ProyectoDetalleResponse,
  ProyectoEditarRequest,
  ProyectoEstructuraResponse,
  ProyectoNuevoRequest,
  ProyectoRequest,
  ProyectoResponse,
  TicketProyectoFiltroRequest,
  TicketProyectoResumenResponse,
} from '../models/proyecto.model';
import { ResponsableDisponibleResponse } from '../models/ticket.model';

@Injectable({ providedIn: 'root' })
export class ProyectoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}${API_ENDPOINTS.PROYECTOS.BASE}`;

  getAll(): Observable<ProyectoResponse[]> {
    return this.http.get<ProyectoResponse[]>(this.base);
  }

  getById(id: number): Observable<ProyectoResponse> {
    return this.http.get<ProyectoResponse>(`${this.base}/${id}`);
  }

  create(data: ProyectoRequest): Observable<ProyectoResponse> {
    return this.http.post<ProyectoResponse>(this.base, data);
  }

  createNuevo(data: ProyectoNuevoRequest): Observable<ProyectoResponse> {
    return this.http.post<ProyectoResponse>(this.base, data);
  }

  update(id: number, data: ProyectoRequest): Observable<ProyectoResponse> {
    return this.http.put<ProyectoResponse>(`${this.base}/${id}`, data);
  }

  updateEditar(id: number, data: ProyectoEditarRequest): Observable<ProyectoResponse> {
    return this.http.put<ProyectoResponse>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  obtenerDetalle(idProyecto: number): Observable<ProyectoDetalleResponse> {
    return this.http.get<ProyectoDetalleResponse>(`${this.base}/${idProyecto}/detalle`);
  }

  obtenerEstructura(idProyecto: number): Observable<ProyectoEstructuraResponse> {
    return this.http.get<ProyectoEstructuraResponse>(`${this.base}/${idProyecto}/estructura`);
  }

  obtenerTickets(
    idProyecto: number,
    filtros: Partial<TicketProyectoFiltroRequest> = {},
  ): Observable<PaginaResponse<TicketProyectoResumenResponse>> {
    const keys = Object.keys(filtros) as (keyof TicketProyectoFiltroRequest)[];
    let params = new HttpParams();
    for (const key of keys) {
      const value = filtros[key];
      if (value !== null && value !== undefined) {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<PaginaResponse<TicketProyectoResumenResponse>>(
      `${this.base}/${idProyecto}/tickets`,
      { params },
    );
  }

  obtenerEquipo(idProyecto: number): Observable<MiembroProyectoResponse[]> {
    return this.http.get<MiembroProyectoResponse[]>(`${this.base}/${idProyecto}/equipo`);
  }

  obtenerActividadReciente(
    idProyecto: number,
    limite = 20,
  ): Observable<ActividadProyectoResponse[]> {
    const params = new HttpParams().set('limite', String(limite));
    return this.http.get<ActividadProyectoResponse[]>(
      `${this.base}/${idProyecto}/actividad`,
      { params },
    );
  }

  obtenerResponsablesDisponibles(
    proyectoId: number,
  ): Observable<ResponsableDisponibleResponse[]> {
    return this.http.get<ResponsableDisponibleResponse[]>(
      `${this.base}/${proyectoId}/responsables-disponibles`,
    );
  }
}
