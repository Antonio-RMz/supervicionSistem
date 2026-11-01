import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { MiProyecto } from '../models/portal.models';
import { PortalClienteService } from './portal-cliente.service';

/**
 * Estado compartido del portal: la lista de proyectos del cliente y cuál está viendo.
 * La usan la barra de arriba (selector de proyecto) y las pantallas.
 */
@Injectable({ providedIn: 'root' })
export class PortalClienteStore {
  private readonly service = inject(PortalClienteService);
  private readonly auth = inject(AuthService);
  private carga: Promise<MiProyecto[]> | null = null;
  /** Usuario para el que se cargó la lista; si entra otro, se vuelve a pedir. */
  private cargadaPara: number | null = null;

  readonly proyectos = signal<MiProyecto[]>([]);
  readonly cargando = signal(false);
  readonly error = signal(false);
  readonly seleccionadoId = signal<number | null>(null);

  readonly seleccionado = computed<MiProyecto | null>(
    () => this.proyectos().find((p) => p.id === this.seleccionadoId()) ?? this.proyectos()[0] ?? null,
  );

  /** Pide los proyectos una sola vez; las siguientes llamadas reutilizan la misma respuesta. */
  cargar(): Promise<MiProyecto[]> {
    const usuarioId = this.auth.usuario()?.id ?? null;
    if (usuarioId !== this.cargadaPara) {
      this.limpiar();
      this.cargadaPara = usuarioId;
    }
    if (!this.carga) {
      this.cargando.set(true);
      this.error.set(false);
      this.carga = firstValueFrom(this.service.misProyectos())
        .then((proyectos) => {
          this.proyectos.set(proyectos);
          return proyectos;
        })
        .catch((e) => {
          this.error.set(true);
          this.carga = null;
          throw e;
        })
        .finally(() => this.cargando.set(false));
    }
    return this.carga;
  }

  private limpiar(): void {
    this.carga = null;
    this.proyectos.set([]);
    this.seleccionadoId.set(null);
  }
}
