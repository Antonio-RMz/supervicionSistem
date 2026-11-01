import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Cliente, DatosCliente, ListadoClientes } from '../models/clientes.models';
import { ClienteDto, ListadoClientesDto } from './clientes.dto';
import { aCliente, aListado, aRequest } from './clientes.mapper';

@Injectable({ providedIn: 'root' })
export class ClientesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/equipo/clientes`;

  listar(): Observable<ListadoClientes> {
    return this.http.get<ListadoClientesDto>(this.url).pipe(map(aListado));
  }

  crear(datos: DatosCliente): Observable<Cliente> {
    return this.http.post<ClienteDto>(this.url, aRequest(datos)).pipe(map(aCliente));
  }

  actualizar(id: number, datos: DatosCliente): Observable<Cliente> {
    return this.http.put<ClienteDto>(`${this.url}/${id}`, aRequest(datos)).pipe(map(aCliente));
  }

  cambiarEstado(id: number, activo: boolean): Observable<Cliente> {
    return this.http.patch<ClienteDto>(`${this.url}/${id}/estado`, { activo }).pipe(map(aCliente));
  }
}
