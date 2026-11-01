import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProyectoResumen } from '../models/proyecto.models';
import { ProyectoResumenDto } from './proyectos.dto';

@Injectable({ providedIn: 'root' })
export class ProyectosService {
  private readonly http = inject(HttpClient);

  /** Los proyectos donde soy miembro (todos si soy Admin). */
  misProyectos(): Observable<ProyectoResumen[]> {
    return this.http
      .get<ProyectoResumenDto[]>(`${environment.apiUrl}/equipo/proyectos`)
      .pipe(map((lista) => lista.map(aProyectoResumen)));
  }
}

/** DTO -> modelo. Hoy son iguales; si el back cambia un nombre, solo se toca aquí. */
function aProyectoResumen(dto: ProyectoResumenDto): ProyectoResumen {
  return { id: dto.id, codigo: dto.codigo, nombre: dto.nombre, estado: dto.estado, clienteNombre: dto.clienteNombre };
}
