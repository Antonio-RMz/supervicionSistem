import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Movimiento, ResultadoMovimiento } from '../models/movimientos.models';

/** Ejecuta la acción del flujo de un ticket. Lo usan el Tablero, Mis tickets y el Dashboard. */
@Injectable({ providedIn: 'root' })
export class MovimientosService {
  private readonly http = inject(HttpClient);

  mover(movimiento: Movimiento): Observable<ResultadoMovimiento> {
    const { ticketId, tipo, estadoEsperado, motivo, bloqueadoPorId, destrabadorId, severidad, responsableId } = movimiento;
    return this.http.post<ResultadoMovimiento>(`${environment.apiUrl}/equipo/tickets/${ticketId}/movimientos`, {
      tipo,
      estadoEsperado,
      motivo: motivo ?? null,
      bloqueadoPorId: bloqueadoPorId ?? null,
      destrabadorId: destrabadorId ?? null,
      severidad: severidad ?? null,
      responsableId: responsableId ?? null,
    });
  }
}

/** "ACTV-46: Iniciar · rama feature/actv-46-..." para el aviso después de mover. */
export function avisoDeMovimiento(codigo: string, resultado: ResultadoMovimiento): string {
  return resultado.rama
    ? `${codigo}: ${resultado.accion}. Se creó la rama ${resultado.rama}`
    : `${codigo}: ${resultado.accion}.`;
}
