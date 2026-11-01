import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DatosProyecto, FaseArbol, ListadoProyectos, Persona, ResumenProyecto } from '../models/proyectos.models';
import { ConfiguracionDto, FaseArbolDto, ListadoDto, PersonaDto, ResumenDto } from './proyectos.dto';
import { aArbol, aDatos, aListado, aPersona, aRequest, aResumen } from './proyectos.mapper';

/**
 * Lista, detalle y formulario de Proyectos. (El selector de arriba usa ProyectosService de features/gestor,
 * que solo trae id y nombre.)
 */
@Injectable({ providedIn: 'root' })
export class ProyectosDetalleService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/equipo/proyectos`;

  listar(): Observable<ListadoProyectos> {
    return this.http.get<ListadoDto>(`${this.url}/tarjetas`).pipe(map(aListado));
  }

  resumen(id: number): Observable<ResumenProyecto> {
    return this.http.get<ResumenDto>(`${this.url}/${id}/resumen`).pipe(map(aResumen));
  }

  arbol(id: number): Observable<FaseArbol[]> {
    return this.http.get<FaseArbolDto[]>(`${this.url}/${id}/arbol`).pipe(map(aArbol));
  }

  configuracion(id: number): Observable<{ codigo: string; datos: DatosProyecto }> {
    return this.http
      .get<ConfiguracionDto>(`${this.url}/${id}/configuracion`)
      .pipe(map((dto) => ({ codigo: dto.codigo, datos: aDatos(dto) })));
  }

  crear(datos: DatosProyecto): Observable<number> {
    return this.http.post<{ id: number }>(this.url, aRequest(datos)).pipe(map((r) => r.id));
  }

  actualizar(id: number, datos: DatosProyecto): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}`, aRequest(datos));
  }

  /** Personas del equipo para elegir Líder y miembros. */
  personas(): Observable<Persona[]> {
    return this.http
      .get<PersonaDto[]>(`${environment.apiUrl}/equipo/personas`)
      .pipe(map((lista) => lista.map(aPersona)));
  }
}
