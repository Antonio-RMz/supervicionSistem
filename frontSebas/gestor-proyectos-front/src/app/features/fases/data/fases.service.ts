import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { infoModulo } from '../../../shared/utils/proyectos';
import { nombresDeRoles } from '../../../shared/utils/roles';
import { EdicionFase, NuevaFase, PreparacionFase } from '../models/fases.models';
import { EdicionDto, NuevaFaseDto, PreparacionDto } from './fases.dto';

@Injectable({ providedIn: 'root' })
export class FasesService {
  private readonly http = inject(HttpClient);

  preparar(proyectoId: number): Observable<PreparacionFase> {
    return this.http
      .get<PreparacionDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/fases/nueva`)
      .pipe(map(aPreparacion));
  }

  /** Devuelve el id de la fase creada. */
  crear(proyectoId: number, nueva: NuevaFase): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/fases`, aDto(nueva))
      .pipe(map((r) => r.id));
  }

  prepararEdicion(proyectoId: number, faseId: number): Observable<EdicionFase> {
    return this.http
      .get<EdicionDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/fases/${faseId}/editar`)
      .pipe(map(aEdicion));
  }

  editar(proyectoId: number, faseId: number, cambios: NuevaFase): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/fases/${faseId}`, aDto(cambios));
  }
}

function aEdicion(dto: EdicionDto): EdicionFase {
  const f = dto.fase;
  return {
    preparacion: aPreparacion(dto.preparacion),
    fase: {
      id: f.id,
      numero: f.numero,
      estado: f.estado,
      datos: {
        nombre: f.nombre,
        objetivo: f.objetivo ?? '',
        fechaInicio: f.fechaInicio,
        fechaFin: f.fechaFin,
        responsableId: f.responsableId,
        versionRelease: f.versionRelease ?? '',
        prioridad: f.prioridad,
        moduloIds: f.moduloIds,
        criteriosManuales: f.criteriosManuales,
      },
      modulosEnUso: f.modulosEnUso,
    },
  };
}

function aPreparacion(dto: PreparacionDto): PreparacionFase {
  return {
    proyectoId: dto.proyectoId,
    proyectoNombre: dto.proyectoNombre,
    numero: dto.numero,
    versionSugerida: dto.versionSugerida,
    inicioSugerido: dto.inicioSugerido,
    liderId: dto.liderId,
    faseActiva: dto.hayFaseActiva ? dto.faseActiva : null,
    modulos: dto.modulos.map((m) => ({ ...infoModulo(m.tipo), ...m, tecnologia: m.tecnologia ?? '' })),
    equipo: dto.equipo.map((p) => ({ id: p.id, nombreCompleto: p.nombreCompleto, roles: nombresDeRoles(p.roles) })),
    criteriosAutomaticos: dto.criteriosAutomaticos,
  };
}

function aDto(nueva: NuevaFase): NuevaFaseDto {
  return {
    nombre: nueva.nombre.trim(),
    objetivo: nueva.objetivo.trim() || null,
    fechaInicio: nueva.fechaInicio,
    fechaFin: nueva.fechaFin,
    responsableId: nueva.responsableId!,
    versionRelease: nueva.versionRelease.trim() || null,
    prioridad: nueva.prioridad,
    moduloIds: nueva.moduloIds,
    criteriosManuales: nueva.criteriosManuales,
  };
}
