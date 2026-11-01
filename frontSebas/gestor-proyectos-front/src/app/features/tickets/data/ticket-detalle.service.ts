import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DetalleTicket } from '../models/ticket-detalle.models';
import { DetalleTicketDto } from './ticket-detalle.dto';
import { aDetalle } from './ticket-detalle.mapper';

/** Detalle del ticket (PDF pág. 29). Sus acciones van por MovimientosService, igual que en las listas. */
@Injectable({ providedIn: 'root' })
export class TicketDetalleService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number, ticketId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/${ticketId}`;
  }

  detalle(proyectoId: number, ticketId: number): Observable<DetalleTicket> {
    return this.http.get<DetalleTicketDto>(this.url(proyectoId, ticketId)).pipe(map((dto) => aDetalle(dto)));
  }

  /** Publica el parche con todos los hotfix aprobados; version es la que se vio en la confirmación. */
  publicarParche(proyectoId: number, ticketId: number, version: string): Observable<{ version: string }> {
    return this.http.post<{ version: string }>(`${this.url(proyectoId, ticketId)}/parche`, { version });
  }

  marcarSubtarea(proyectoId: number, ticketId: number, subtareaId: number, hecha: boolean): Observable<void> {
    return this.http.put<void>(`${this.url(proyectoId, ticketId)}/subtareas/${subtareaId}`, { hecha });
  }

  agregarSubtarea(proyectoId: number, ticketId: number, descripcion: string): Observable<void> {
    return this.http.post<void>(`${this.url(proyectoId, ticketId)}/subtareas`, { descripcion });
  }

  quitarSubtarea(proyectoId: number, ticketId: number, subtareaId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId, ticketId)}/subtareas/${subtareaId}`);
  }

  comentar(proyectoId: number, ticketId: number, texto: string): Observable<void> {
    return this.http.post<void>(`${this.url(proyectoId, ticketId)}/comentarios`, { texto });
  }
}
