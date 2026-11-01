import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { TicketMio } from '../models/mis-tickets.models';
import { TicketMioDto } from './mis-tickets.dto';
import { aTicketMio } from './mis-tickets.mapper';

@Injectable({ providedIn: 'root' })
export class MisTicketsService {
  private readonly http = inject(HttpClient);

  /** Ya vienen ordenados: lo que me toca primero, luego por prioridad. */
  listar(proyectoId: number): Observable<TicketMio[]> {
    return this.http
      .get<TicketMioDto[]>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mis-tickets`)
      .pipe(map((lista) => lista.map(aTicketMio)));
  }
}
