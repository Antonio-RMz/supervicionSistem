import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ImagenDeTicket, Mantenimiento, PreparacionMantenimiento } from '../models/mantenimiento.models';
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

  /** Sube una imagen al ticket (la captura de un bug). */
  subirImagen(proyectoId: number, ticketId: number, archivo: File): Observable<void> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<void>(`${this.urlImagenes(proyectoId, ticketId)}`, datos);
  }

  /** Las imágenes del ticket, sin el archivo. */
  imagenes(proyectoId: number, ticketId: number): Observable<ImagenDeTicket[]> {
    return this.http
      .get<{ id: number; nombre: string; tamanoBytes: number }[]>(this.urlImagenes(proyectoId, ticketId))
      .pipe(map((lista) => lista.map((i) => ({ id: i.id, nombre: i.nombre }))));
  }

  /** El archivo pasa por el token: se pide como Blob y se muestra con una URL local. */
  imagen(proyectoId: number, ticketId: number, imagenId: number): Observable<Blob> {
    return this.http.get(`${this.urlImagenes(proyectoId, ticketId)}/${imagenId}`, { responseType: 'blob' });
  }

  private urlImagenes(proyectoId: number, ticketId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/${ticketId}/imagenes`;
  }

  /** Devuelve el id del bug nuevo. */
  reportarFalla(proyectoId: number, reporte: ReporteFallaRequest): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/mantenimiento/fallas`, reporte)
      .pipe(map((r) => r.id));
  }
}
