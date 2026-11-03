import { HttpClient } from '@angular/common/http';
import { inject, Injectable, Signal, signal, WritableSignal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * Los íconos de los proyectos. Cada uno se pide una sola vez y se recuerda mientras la app está abierta;
 * al subir o quitar uno, se actualiza en todas las pantallas que lo muestran.
 * El valor es la URL local de la imagen (blob:), null si el proyecto no tiene ícono, undefined mientras carga.
 */
@Injectable({ providedIn: 'root' })
export class IconosProyectoService {
  private readonly http = inject(HttpClient);
  private readonly iconos = new Map<number, WritableSignal<string | null | undefined>>();

  private url(proyectoId: number): string {
    return `${environment.apiUrl}/equipo/proyectos/${proyectoId}/icono`;
  }

  icono(proyectoId: number): Signal<string | null | undefined> {
    let icono = this.iconos.get(proyectoId);
    if (!icono) {
      icono = signal<string | null | undefined>(undefined);
      this.iconos.set(proyectoId, icono);
      this.cargar(proyectoId);
    }
    return icono;
  }

  subir(proyectoId: number, archivo: File): Observable<void> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<void>(this.url(proyectoId), datos).pipe(tap(() => this.cargar(proyectoId)));
  }

  quitar(proyectoId: number): Observable<void> {
    return this.http.delete<void>(this.url(proyectoId)).pipe(tap(() => this.poner(proyectoId, null)));
  }

  /** 204 (sin contenido) = no tiene ícono; si falla, también se quedan las iniciales. */
  private cargar(proyectoId: number): void {
    this.http.get(this.url(proyectoId), { responseType: 'blob', observe: 'response' }).subscribe({
      next: (r) => this.poner(proyectoId, r.status === 200 && r.body?.size ? URL.createObjectURL(r.body) : null),
      error: () => this.poner(proyectoId, null),
    });
  }

  private poner(proyectoId: number, url: string | null): void {
    let icono = this.iconos.get(proyectoId);
    if (!icono) {
      icono = signal<string | null | undefined>(undefined);
      this.iconos.set(proyectoId, icono);
    }
    const anterior = icono();
    if (anterior) URL.revokeObjectURL(anterior);
    icono.set(url);
  }
}
