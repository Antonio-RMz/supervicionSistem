import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { infoModulo } from '../../../shared/utils/proyectos';
import { codigoHistoria, textoEstado } from '../../../shared/utils/tickets';
import { EdicionTicket, NuevoTicket, PreparacionTicket } from '../models/nuevo-ticket.models';
import { TicketImportado } from '../models/importacion-tickets.models';
import { EdicionTicketDto, NuevoTicketDto, PreparacionTicketDto } from './nuevo-ticket.dto';

@Injectable({ providedIn: 'root' })
export class NuevoTicketService {
  private readonly http = inject(HttpClient);

  preparar(proyectoId: number): Observable<PreparacionTicket> {
    return this.http
      .get<PreparacionTicketDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/nuevo`)
      .pipe(map(aPreparacion));
  }

  /** Devuelve el id del ticket creado. */
  /** Importar tickets: varias Actividades dentro de la historia, todas con el mismo responsable. */
  importar(proyectoId: number, historiaId: number, responsableId: number, tickets: TicketImportado[]): Observable<number> {
    return this.http
      .post<{ creados: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/importar`, {
        historiaId,
        responsableId,
        tickets,
      })
      .pipe(map((r) => r.creados));
  }

  crear(proyectoId: number, nuevo: NuevoTicket): Observable<number> {
    return this.http
      .post<{ id: number }>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets`, aDto(nuevo))
      .pipe(map((r) => r.id));
  }

  /** Para editarlo mientras no empiece: lo editan quien lo creó o el Líder. */
  edicion(proyectoId: number, ticketId: number): Observable<EdicionTicket> {
    return this.http
      .get<EdicionTicketDto>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/${ticketId}/edicion`)
      .pipe(
        map((dto) => ({
          preparacion: aPreparacion(dto.preparacion),
          codigo: dto.ticket.codigo,
          datos: {
            ...dto.ticket.datos,
            descripcion: dto.ticket.datos.descripcion ?? '',
            prioridad: dto.ticket.datos.prioridad ?? 'MEDIA',
            fechaLimite: dto.ticket.datos.fechaLimite ?? '',
          },
        })),
      );
  }

  editar(proyectoId: number, ticketId: number, datos: NuevoTicket): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/equipo/proyectos/${proyectoId}/tickets/${ticketId}`, aDto(datos));
  }
}

function aPreparacion(dto: PreparacionTicketDto): PreparacionTicket {
  return {
    proyectoId: dto.proyectoId,
    proyectoNombre: dto.proyectoNombre,
    liderId: dto.liderId,
    fases: dto.fases.map((f) => ({
      id: f.id,
      numero: f.numero,
      titulo: `Fase ${f.numero} · ${f.nombre}`,
      modulos: f.modulos.map((m) => ({
        ...infoModulo(m.tipo),
        ...m,
        tecnologia: m.tecnologia ?? '',
        repositorio: m.repositorio ?? '',
      })),
      historias: f.historias.map((h) => ({
        id: h.id,
        codigo: codigoHistoria(h.numero),
        titulo: h.titulo,
        estado: textoEstado(h.estado),
        enunciado: [h.como && `Como ${h.como}`, h.quiero && `quiero ${h.quiero}`, h.para && `para ${h.para}`]
          .filter(Boolean)
          .join(', '),
        analistaNombre: h.analistaNombre,
        moduloIds: h.moduloIds,
        tickets: h.tickets,
        criterios: h.criterios,
      })),
    })),
    equipo: dto.equipo,
    siguientes: dto.siguientes,
    abiertos: dto.abiertos.map((t) => ({ id: t.id, codigo: t.codigo, titulo: t.titulo, estadoTexto: textoEstado(t.estado) })),
    capas: dto.capas,
  };
}

function aDto(nuevo: NuevoTicket): NuevoTicketDto {
  return {
    faseId: nuevo.faseId!,
    historiaId: nuevo.historiaId!,
    tipo: nuevo.tipo,
    titulo: nuevo.titulo.trim(),
    descripcion: nuevo.descripcion.trim() || null,
    moduloId: nuevo.moduloId!,
    capas: nuevo.capas,
    criterioIds: nuevo.criterioIds,
    subtareas: nuevo.subtareas,
    dependeDe: nuevo.dependeDe,
    responsableId: nuevo.responsableId!,
    testerId: nuevo.testerId,
    prioridad: nuevo.prioridad,
    estimacionHoras: nuevo.estimacionHoras,
    fechaLimite: nuevo.fechaLimite || null,
  };
}
