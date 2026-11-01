import { inject, Injectable, signal } from '@angular/core';
import { ResumenProyecto } from '../models/proyectos.models';
import { ProyectosDetalleService } from './proyectos-detalle.service';

/**
 * El proyecto abierto en el detalle. Lo provee el layout del detalle (no es global):
 * el encabezado y cada pestaña leen de aquí sin volver a pedirlo.
 */
@Injectable()
export class ProyectoDetalleStore {
  private readonly service = inject(ProyectosDetalleService);

  readonly resumen = signal<ResumenProyecto | null>(null);
  readonly cargando = signal(true);
  readonly error = signal(false);
  private id: number | null = null;

  cargar(id: number): void {
    this.id = id;
    this.cargando.set(true);
    this.error.set(false);
    this.service.resumen(id).subscribe({
      next: (resumen) => {
        if (this.id !== id) return; // llegó tarde: ya se abrió otro proyecto
        this.resumen.set(resumen);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  /** Después de una acción (mover un ticket) los números cambian. */
  recargar(): void {
    if (this.id !== null) this.cargar(this.id);
  }
}
