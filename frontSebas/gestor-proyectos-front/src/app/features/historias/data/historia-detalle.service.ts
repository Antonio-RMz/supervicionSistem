import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AccionDeListaHistoria,
  AccionHistoria,
  DatosAccionHistoria,
  DetalleHistoria,
  ResultadoAccionHistoria,
} from '../models/historia-detalle.models';
import { AccionDeListaHistoriaDto, DetalleHistoriaDto, ResultadoAccionHistoriaDto } from './historia-detalle.dto';
import { aAccionesDeLista, aDetalle, aRequestAccion } from './historia-detalle.mapper';
import { IntegracionHistoria } from '../models/integracion.models';
import { IntegracionDto } from './integracion.dto';
import { aIntegracion } from './integracion.mapper';
import { ListadoHistorias } from '../models/listado-historias.models';
import { ListadoHistoriasDto } from './listado-historias.dto';
import { aListadoHistorias } from './listado-historias.mapper';

/** Detalle de la historia y sus acciones por estado (PDF pág. 21, 24, 28 y 33). */
@Injectable({ providedIn: 'root' })
export class HistoriaDetalleService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias`;
  }

  /** Todas las historias del proyecto (menú Historias), de la más reciente a la más antigua. */
  listar(proyectoId: number): Observable<ListadoHistorias> {
    return this.http.get<ListadoHistoriasDto>(this.url(proyectoId)).pipe(map((dto) => aListadoHistorias(dto)));
  }

  detalle(proyectoId: number, historiaId: number): Observable<DetalleHistoria> {
    return this.http
      .get<DetalleHistoriaDto>(`${this.url(proyectoId)}/${historiaId}`)
      .pipe(map((dto) => aDetalle(dto)));
  }

  /** La vista Integrar historia (PDF pág. 28). Integrar se hace con ejecutar(..., 'INTEGRAR'). */
  integracion(proyectoId: number, historiaId: number): Observable<IntegracionHistoria> {
    return this.http
      .get<IntegracionDto>(`${this.url(proyectoId)}/${historiaId}/integracion`)
      .pipe(map((dto) => aIntegracion(dto)));
  }

  /** El botón de cada historia en las listas; las historias sin botón no vienen. */
  accionesDeLista(proyectoId: number): Observable<AccionDeListaHistoria[]> {
    return this.http.get<AccionDeListaHistoriaDto[]>(`${this.url(proyectoId)}/acciones`).pipe(map(aAccionesDeLista));
  }

  /** estadoEsperado: el que se veía al presionar; si alguien más la movió, el back no la pisa. */
  ejecutar(
    proyectoId: number,
    historiaId: number,
    accion: AccionHistoria,
    estadoEsperado: string,
    datos: DatosAccionHistoria = {},
  ): Observable<ResultadoAccionHistoria> {
    return this.http.post<ResultadoAccionHistoriaDto>(
      `${this.url(proyectoId)}/${historiaId}/acciones`,
      aRequestAccion(accion, estadoEsperado, datos),
    );
  }

  verificarCriterio(proyectoId: number, historiaId: number, criterioId: number, verificado: boolean): Observable<void> {
    return this.http.put<void>(`${this.url(proyectoId)}/${historiaId}/criterios/${criterioId}`, { verificado });
  }

  /** La borra del todo; el back solo lo deja si aún no se inicia. */
  eliminar(proyectoId: number, historiaId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId)}/${historiaId}`);
  }

  comentar(proyectoId: number, historiaId: number, texto: string): Observable<void> {
    return this.http.post<void>(`${this.url(proyectoId)}/${historiaId}/comentarios`, { texto });
  }

  subirImagen(proyectoId: number, historiaId: number, archivo: File): Observable<void> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<void>(`${this.url(proyectoId)}/${historiaId}/imagenes`, datos);
  }

  /** El archivo de la imagen: pasa por el token, así que no se puede poner directo en un <img>. */
  imagen(proyectoId: number, historiaId: number, imagenId: number): Observable<Blob> {
    return this.http.get(`${this.url(proyectoId)}/${historiaId}/imagenes/${imagenId}`, { responseType: 'blob' });
  }

  quitarImagen(proyectoId: number, historiaId: number, imagenId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId)}/${historiaId}/imagenes/${imagenId}`);
  }
}
