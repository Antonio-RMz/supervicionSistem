import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AccionDeLista, AccionFase, DatosAccion, DetalleFase, ReleaseFase, ResultadoAccion } from '../models/fase-detalle.models';
import { AccionDeListaDto, DetalleFaseDto, ReleaseFaseDto, ResultadoAccionDto } from './fase-detalle.dto';
import { aAccionesDeLista, aDetalle, aRelease, aRequestAccion } from './fase-detalle.mapper';

/** Detalle de la fase y sus acciones por estado (PDF pág. 20, 25 y 32). */
@Injectable({ providedIn: 'root' })
export class FaseDetalleService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/fases`;
  }

  detalle(proyectoId: number, faseId: number): Observable<DetalleFase> {
    return this.http.get<DetalleFaseDto>(`${this.url(proyectoId)}/${faseId}`).pipe(map((dto) => aDetalle(dto)));
  }

  /** La borra con sus historias; el back solo lo deja si está Planeada y nada se ha iniciado. */
  eliminar(proyectoId: number, faseId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId)}/${faseId}`);
  }

  /** El enlace de Figma de toda la fase y si lo heredan las historias sin diseño propio. */
  guardarDiseno(proyectoId: number, faseId: number, figmaUrl: string | null, paraTodas: boolean): Observable<void> {
    return this.http.put<void>(`${this.url(proyectoId)}/${faseId}/diseno`, { figmaUrl, paraTodas });
  }

  /** El release de la fase (PDF pág. 27). */
  release(proyectoId: number, faseId: number): Observable<ReleaseFase> {
    return this.http.get<ReleaseFaseDto>(`${this.url(proyectoId)}/${faseId}/release`).pipe(map(aRelease));
  }

  guardarNotas(proyectoId: number, faseId: number, notas: string, enviarAlCliente: boolean): Observable<void> {
    return this.http.put<void>(`${this.url(proyectoId)}/${faseId}/release/notas`, { notas, enviarAlCliente });
  }

  /** El botón de cada fase en el árbol del proyecto; las fases sin botón no vienen. */
  accionesDeLista(proyectoId: number): Observable<AccionDeLista[]> {
    return this.http.get<AccionDeListaDto[]>(`${this.url(proyectoId)}/acciones`).pipe(map(aAccionesDeLista));
  }

  /** estadoEsperado: el que se veía al presionar; si alguien más la movió, el back no la pisa. */
  ejecutar(
    proyectoId: number,
    faseId: number,
    accion: AccionFase,
    estadoEsperado: string,
    datos: DatosAccion = {},
  ): Observable<ResultadoAccion> {
    return this.http.post<ResultadoAccionDto>(
      `${this.url(proyectoId)}/${faseId}/acciones`,
      aRequestAccion(accion, estadoEsperado, datos),
    );
  }

  marcarCriterio(proyectoId: number, faseId: number, criterioId: number): Observable<ResultadoAccion> {
    return this.http.post<ResultadoAccionDto>(
      `${this.url(proyectoId)}/${faseId}/criterios/${criterioId}/cumplido`,
      {},
    );
  }
}
