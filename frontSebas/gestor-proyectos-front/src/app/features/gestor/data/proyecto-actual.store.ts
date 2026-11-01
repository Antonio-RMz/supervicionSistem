import { computed, inject, Injectable, signal } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { ProyectoResumen } from '../models/proyecto.models';
import { ProyectosService } from './proyectos.service';

const CLAVE = 'proyectoActual';

/**
 * El proyecto en el que estás trabajando (selector de arriba). Lo leen el Dashboard, Tablero, etc.
 * Se recuerda en el navegador para que al volver abras el mismo.
 */
@Injectable({ providedIn: 'root' })
export class ProyectoActualStore {
  private readonly service = inject(ProyectosService);
  private readonly auth = inject(AuthService);
  private cargadaPara: number | null = null;

  readonly proyectos = signal<ProyectoResumen[]>([]);
  readonly cargando = signal(false);
  readonly error = signal(false);
  private readonly elegidoId = signal<number | null>(leerGuardado());

  readonly actual = computed<ProyectoResumen | null>(
    () => this.proyectos().find((p) => p.id === this.elegidoId()) ?? this.proyectos()[0] ?? null,
  );

  /** "Te toca" del proyecto actual: lo pone el Dashboard y lo muestra el menú (Mis tickets). */
  readonly pendientesMios = signal<number | null>(null);

  cargar(): void {
    const usuarioId = this.auth.usuario()?.id ?? null;
    if (usuarioId === this.cargadaPara && this.proyectos().length) return;
    this.cargadaPara = usuarioId;
    this.cargando.set(true);
    this.error.set(false);
    this.service.misProyectos().subscribe({
      next: (proyectos) => {
        this.proyectos.set(proyectos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  /** Vuelve a pedir la lista aunque ya esté cargada (ej. después de crear o editar un proyecto). */
  recargar(): void {
    this.cargadaPara = null;
    this.proyectos.set([]);
    this.cargar();
  }

  elegir(id: number): void {
    this.elegidoId.set(id);
    this.pendientesMios.set(null);
    try {
      localStorage.setItem(CLAVE, String(id));
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
  }
}

function leerGuardado(): number | null {
  try {
    const valor = localStorage.getItem(CLAVE);
    return valor ? Number(valor) : null;
  } catch {
    return null;
  }
}
