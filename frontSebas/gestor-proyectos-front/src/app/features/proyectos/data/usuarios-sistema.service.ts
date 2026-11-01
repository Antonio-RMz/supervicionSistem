import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UsuarioDelSistema, UsuariosDelSistema } from '../models/usuarios-sistema.models';

/** JSON de /usuarios-sistema (ver UsuariosDelSistemaDtos.java). */
interface UsuariosDelSistemaDto {
  usuarios: { nombre: string; descripcion: string | null }[];
  puedeEditar: boolean;
}

/** Pestaña Usuarios del sistema: quiénes usan el software que se construye. */
@Injectable({ providedIn: 'root' })
export class UsuariosSistemaService {
  private readonly http = inject(HttpClient);

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/usuarios-sistema`;
  }

  consultar(proyectoId: number): Observable<UsuariosDelSistema> {
    return this.http.get<UsuariosDelSistemaDto>(this.url(proyectoId)).pipe(
      map((dto) => ({
        usuarios: dto.usuarios.map((u) => ({ nombre: u.nombre, descripcion: u.descripcion ?? '' })),
        puedeEditar: dto.puedeEditar,
      })),
    );
  }

  /** Reemplaza la lista completa, en este orden. */
  guardar(proyectoId: number, usuarios: UsuarioDelSistema[]): Observable<void> {
    return this.http.put<void>(this.url(proyectoId), {
      usuarios: usuarios.map((u) => ({ nombre: u.nombre.trim(), descripcion: u.descripcion.trim() || null })),
    });
  }
}
