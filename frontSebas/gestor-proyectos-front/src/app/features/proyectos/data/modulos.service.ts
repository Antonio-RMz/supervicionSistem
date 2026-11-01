import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DatosModulo, ModulosProyecto } from '../models/modulos.models';
import { ModulosDto } from './modulos.dto';
import { aModulos, aRequestModulo } from './modulos.mapper';

/** Pestaña Módulos del proyecto (PDF pág. 35). Agregar, editar y quitar los hacen el Líder o un Admin. */
@Injectable({ providedIn: 'root' })
export class ModulosService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/modulos`;
  }

  modulos(proyectoId: number): Observable<ModulosProyecto> {
    return this.http.get<ModulosDto>(this.url(proyectoId)).pipe(map((dto) => aModulos(dto)));
  }

  /** Crea el módulo si id es null; si no, lo actualiza. */
  guardar(proyectoId: number, id: number | null, datos: DatosModulo): Observable<void> {
    const body = aRequestModulo(datos);
    return id === null
      ? this.http.post<{ id: number }>(this.url(proyectoId), body).pipe(map(() => undefined))
      : this.http.put<void>(`${this.url(proyectoId)}/${id}`, body);
  }

  quitar(proyectoId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.url(proyectoId)}/${id}`);
  }
}
