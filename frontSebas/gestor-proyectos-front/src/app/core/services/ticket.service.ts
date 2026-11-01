import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api.constants';
import {
  ActualizarAlcanceFaseRequest,
  ActualizarObjetivoFaseRequest,
  CrearCriterioFaseRequest,
  CrearFaseRequest,
  CrearHistoriaRequest,
  CrearReglaFaseRequest,
  CrearRequerimientoFaseRequest,
  EjecutarAccionRequest,
  TicketComentarioResponse,
  TicketDetalleApi,
  TicketFaseCriterioResponse,
  TicketFaseDefinicionResponse,
  TicketFaseReglaResponse,
  TicketFaseRequerimientoResponse,
} from '../models/ticket.model';
import { TicketProyectoResumenResponse } from '../models/proyecto.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}${API_ENDPOINTS.TICKETS.BASE}`;

  getTicketById(id: number): Observable<TicketDetalleApi> {
    return this.http.get<TicketDetalleApi>(`${this.base}/${id}`);
  }

  getTicketByFolio(folio: string): Observable<TicketDetalleApi> {
    return this.http.get<TicketDetalleApi>(`${this.base}/folio/${folio}`);
  }

  obtenerDetallePorFolio(folio: string): Observable<TicketDetalleApi> {
    return this.http.get<TicketDetalleApi>(
      `${this.base}/folio/${encodeURIComponent(folio)}`,
    );
  }

  ejecutarAccion(ticketId: number, codigoAccion: string, body: EjecutarAccionRequest = {}): Observable<TicketDetalleApi> {
    return this.http.post<TicketDetalleApi>(`${this.base}/${ticketId}/acciones/${codigoAccion}`, body);
  }

  obtenerComentarios(idTicket: number): Observable<TicketComentarioResponse[]> {
    return this.http.get<TicketComentarioResponse[]>(
      `${this.base}/${idTicket}/comentarios`,
    );
  }

  crearComentario(idTicket: number, comentario: string): Observable<TicketComentarioResponse> {
    return this.http.post<TicketComentarioResponse>(
      `${this.base}/${idTicket}/comentarios`,
      { comentario },
    );
  }

  crearFase(body: CrearFaseRequest): Observable<TicketProyectoResumenResponse> {
    return this.http.post<TicketProyectoResumenResponse>(`${this.base}/fases`, body);
  }

  crearHistoria(faseId: number, body: CrearHistoriaRequest): Observable<TicketDetalleApi> {
    return this.http.post<TicketDetalleApi>(`${this.base}/fases/${faseId}/historias`, body);
  }

  obtenerDefinicionFase(ticketId: number): Observable<TicketFaseDefinicionResponse> {
    return this.http.get<TicketFaseDefinicionResponse>(`${this.base}/${ticketId}/fase`);
  }

  actualizarObjetivoFase(ticketId: number, body: ActualizarObjetivoFaseRequest): Observable<TicketFaseDefinicionResponse> {
    return this.http.put<TicketFaseDefinicionResponse>(`${this.base}/${ticketId}/fase/objetivo`, body);
  }

  actualizarAlcanceFase(ticketId: number, body: ActualizarAlcanceFaseRequest): Observable<TicketFaseDefinicionResponse> {
    return this.http.put<TicketFaseDefinicionResponse>(`${this.base}/${ticketId}/fase/alcance`, body);
  }

  crearRequerimientoFase(ticketId: number, body: CrearRequerimientoFaseRequest): Observable<TicketFaseRequerimientoResponse> {
    return this.http.post<TicketFaseRequerimientoResponse>(`${this.base}/${ticketId}/fase/requerimientos`, body);
  }

  crearReglaFase(ticketId: number, body: CrearReglaFaseRequest): Observable<TicketFaseReglaResponse> {
    return this.http.post<TicketFaseReglaResponse>(`${this.base}/${ticketId}/fase/reglas`, body);
  }

  crearCriterioFase(ticketId: number, body: CrearCriterioFaseRequest): Observable<TicketFaseCriterioResponse> {
    return this.http.post<TicketFaseCriterioResponse>(`${this.base}/${ticketId}/fase/criterios`, body);
  }
}
