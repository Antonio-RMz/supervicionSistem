import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { infoModulo } from '../../../shared/utils/proyectos';
import { EdicionHistoria, NuevaHistoria, PreparacionHistoria } from '../models/historias.models';
import { HistoriaImportada, HistoriasDeFase, ResultadoImportacion } from '../models/importacion.models';
import { EdicionDto, HistoriasDeFaseDto, NuevaHistoriaDto, PreparacionDto } from './historias.dto';
import { codigoHistoria } from '../../../shared/utils/tickets';

@Injectable({ providedIn: 'root' })
export class HistoriasService {
  private readonly http = inject(HttpClient);

  preparar(proyectoId: number): Observable<PreparacionHistoria> {
    return this.http
      .get<PreparacionDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias/nueva`)
      .pipe(map(aPreparacion));
  }

  /** Devuelve el id de la historia creada. */
  crear(proyectoId: number, nueva: NuevaHistoria): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias`, aDto(nueva))
      .pipe(map((r) => r.id));
  }

  /** Importar historias: todas en Borrador dentro de la fase, con el mismo Analista. */
  importar(
    proyectoId: number,
    faseId: number,
    analistaId: number,
    historias: HistoriaImportada[],
  ): Observable<ResultadoImportacion> {
    return this.http.post<ResultadoImportacion>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias/importar`, {
      faseId,
      analistaId,
      historias,
    });
  }

  /** Las historias de la fase completas, para copiarlas como JSON. */
  deFase(proyectoId: number, faseId: number): Observable<HistoriasDeFase> {
    return this.http
      .get<HistoriasDeFaseDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias`, { params: { fase: faseId } })
      .pipe(
        map((dto) => ({
          proyectoNombre: dto.proyectoNombre,
          faseTitulo: `Fase ${dto.faseNumero} · ${dto.faseNombre}`,
          historias: dto.historias,
        })),
      );
  }

  prepararEdicion(proyectoId: number, historiaId: number): Observable<EdicionHistoria> {
    return this.http
      .get<EdicionDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias/${historiaId}/editar`)
      .pipe(map(aEdicion));
  }

  editar(proyectoId: number, historiaId: number, cambios: NuevaHistoria): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/historias/${historiaId}`, aDto(cambios));
  }
}

function aEdicion(dto: EdicionDto): EdicionHistoria {
  const h = dto.historia;
  return {
    preparacion: aPreparacion(dto.preparacion),
    historia: {
      id: h.id,
      codigo: codigoHistoria(h.numero),
      estado: h.estado,
      datos: {
        borrador: h.estado === 'BORRADOR' || h.estado === 'EN_PLANEACION',
        faseId: h.faseId,
        titulo: h.titulo,
        como: h.como ?? '',
        quiero: h.quiero ?? '',
        para: h.para ?? '',
        criterios: h.criterios,
        figmaUrl: h.figmaUrl ?? '',
        disenoAprobado: h.disenoAprobado,
        analistaId: h.analistaId,
        prioridad: h.prioridad,
        puntos: h.puntos,
        moduloIds: h.moduloIds,
      },
    },
  };
}

function aPreparacion(dto: PreparacionDto): PreparacionHistoria {
  return {
    proyectoId: dto.proyectoId,
    proyectoNombre: dto.proyectoNombre,
    codigo: codigoHistoria(dto.numero),
    fases: dto.fases.map((f) => ({
      id: f.id,
      numero: f.numero,
      titulo: `Fase ${f.numero} · ${f.nombre}`,
      modulos: f.modulos.map((m) => ({ ...infoModulo(m.tipo ?? ''), id: m.id, tipo: m.tipo ?? '', nombre: m.nombre })),
    })),
    analistas: dto.analistas.map((a) => ({
      id: a.id,
      nombreCompleto: a.nombreCompleto,
      esAnalista: a.roles.includes('ANALISTA'),
    })),
    usuariosDelSistema: dto.usuariosDelSistema,
  };
}

function aDto(nueva: NuevaHistoria): NuevaHistoriaDto {
  const texto = (valor: string) => valor.trim() || null;
  return {
    borrador: nueva.borrador,
    faseId: nueva.faseId!,
    titulo: nueva.titulo.trim(),
    como: texto(nueva.como),
    quiero: texto(nueva.quiero),
    para: texto(nueva.para),
    criterios: nueva.criterios,
    figmaUrl: texto(nueva.figmaUrl),
    disenoAprobado: nueva.disenoAprobado,
    analistaId: nueva.analistaId!,
    prioridad: nueva.prioridad,
    puntos: nueva.puntos,
    moduloIds: nueva.moduloIds,
  };
}
