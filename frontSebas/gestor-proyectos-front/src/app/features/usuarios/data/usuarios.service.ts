import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Accesos, NuevoUsuario, PerfilUsuario, Permiso, Rol, UsuarioFila } from '../models/usuarios.models';
import { AccesosDto, PerfilDto, PermisoDto, RolDto, UsuarioFilaDto } from './usuarios.dto';
import { aAccesos, aAccesosDto, aNuevoUsuarioDto, aPerfil, aPermiso, aRol, aUsuarioFila } from './usuarios.mapper';

/** Usuarios y roles. Solo el Administrador puede llamar a estos endpoints. */
@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/equipo/usuarios`;

  listar(): Observable<UsuarioFila[]> {
    return this.http.get<UsuarioFilaDto[]>(this.url).pipe(map((lista) => lista.map(aUsuarioFila)));
  }

  crear(nuevo: NuevoUsuario): Observable<UsuarioFila> {
    return this.http.post<UsuarioFilaDto>(this.url, aNuevoUsuarioDto(nuevo)).pipe(map(aUsuarioFila));
  }

  perfil(id: number): Observable<PerfilUsuario> {
    return this.http.get<PerfilDto>(`${this.url}/${id}`).pipe(map((dto) => aPerfil(dto)));
  }

  accesos(id: number): Observable<Accesos> {
    return this.http.get<AccesosDto>(`${this.url}/${id}/accesos`).pipe(map(aAccesos));
  }

  guardarAccesos(id: number, accesos: Accesos): Observable<Accesos> {
    return this.http.put<AccesosDto>(`${this.url}/${id}/accesos`, aAccesosDto(accesos)).pipe(map(aAccesos));
  }

  cambiarEstado(id: number, activo: boolean): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/estado`, { activo });
  }

  roles(): Observable<Rol[]> {
    return this.http
      .get<RolDto[]>(`${environment.apiUrl}/equipo/accesos/roles`)
      .pipe(map((lista) => lista.map(aRol)));
  }

  permisos(): Observable<Permiso[]> {
    return this.http
      .get<PermisoDto[]>(`${environment.apiUrl}/equipo/accesos/permisos`)
      .pipe(map((lista) => lista.map(aPermiso)));
  }
}
