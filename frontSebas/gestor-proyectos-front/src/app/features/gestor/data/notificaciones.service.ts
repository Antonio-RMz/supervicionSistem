import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { aFechaHora } from '../../../shared/utils/fechas';

/** ASIGNACION: te asignaron un ticket o una historia · BUG_REPORTADO: bug en tu proyecto · COMENTARIO: comentaron tu ticket */
export type TipoNotificacion = 'ASIGNACION' | 'BUG_REPORTADO' | 'COMENTARIO';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  texto: string;
  proyectoId: number;
  /** Lleva al ticket… */
  ticketId: number | null;
  /** …o a la historia (cuando te asignan una historia). */
  historiaId: number | null;
  leida: boolean;
  fecha: Date | null;
}

interface BandejaDto {
  noLeidas: number;
  notificaciones: {
    id: number;
    tipo: TipoNotificacion;
    texto: string;
    proyectoId: number;
    ticketId: number | null;
    historiaId: number | null;
    leida: boolean;
    creadoEn: string | null;
  }[];
}

/** Cada cuánto se pregunta si hay notificaciones nuevas. */
const CADA_MS = 20_000;
const CLAVE_SONIDO = 'gestor.notificaciones.sonido';
const MAXIMO_EMERGENTES = 3;
const DURACION_EMERGENTE_MS = 8_000;
/** Volumen de cada nota (0 a 1). */
const VOLUMEN = 0.9;

/**
 * La campana: pregunta al API cada 20 s (y al volver a la pestaña) y suena cuando llega una nueva.
 * El navegador solo deja sonar después de que la persona hizo clic en la página, así que el audio se
 * prepara con el primer clic.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/equipo/notificaciones`;

  readonly notificaciones = signal<Notificacion[]>([]);
  readonly noLeidas = signal(0);
  /** Se recuerda en este navegador; por defecto suena. */
  readonly conSonido = signal(leerSonido());
  readonly hayNuevas = computed(() => this.noLeidas() > 0);
  /** Las que acaban de llegar: salen solas abajo a la izquierda y se van después de unos segundos. */
  readonly emergentes = signal<Notificacion[]>([]);

  /** El id más alto ya visto: lo que llegue arriba de él es nuevo y suena. null = aún no se carga. */
  private ultimoVisto: number | null = null;
  private temporizador: ReturnType<typeof setInterval> | null = null;
  private audio: AudioContext | null = null;

  private readonly alVolver = () => {
    if (document.visibilityState === 'visible') this.cargar();
  };
  private readonly prepararAudio = () => {
    try {
      this.audio ??= new AudioContext();
      if (this.audio.state === 'suspended') void this.audio.resume();
    } catch {
      // Sin Web Audio: las notificaciones llegan, solo que sin sonido.
    }
  };

  /** Empieza a preguntar. Lo llama el layout del equipo al entrar. */
  iniciar(): void {
    if (this.temporizador) return;
    this.cargar();
    this.temporizador = setInterval(() => {
      if (document.visibilityState === 'visible') this.cargar();
    }, CADA_MS);
    document.addEventListener('visibilitychange', this.alVolver);
    document.addEventListener('pointerdown', this.prepararAudio, { once: true });
  }

  /** Deja de preguntar (al salir del layout o cerrar sesión). */
  detener(): void {
    this.emergentes.set([]);
    if (this.temporizador) clearInterval(this.temporizador);
    this.temporizador = null;
    this.ultimoVisto = null;
    document.removeEventListener('visibilitychange', this.alVolver);
    document.removeEventListener('pointerdown', this.prepararAudio);
  }

  cargar(): void {
    this.http.get<BandejaDto>(this.url).subscribe({
      next: (b) => {
        const lista = b.notificaciones.map((n) => ({ ...n, fecha: aFechaHora(n.creadoEn) }));
        const maximo = lista.reduce((m, n) => Math.max(m, n.id), 0);
        // Suena y sale sola solo si llegó algo nuevo sin leer desde la última vez (no al abrir la página).
        const nuevas = this.ultimoVisto === null ? [] : lista.filter((n) => n.id > this.ultimoVisto! && !n.leida);
        if (nuevas.length) {
          this.sonar();
          this.mostrar(nuevas);
        }
        this.ultimoVisto = Math.max(this.ultimoVisto ?? 0, maximo);
        this.notificaciones.set(lista);
        this.noLeidas.set(b.noLeidas);
      },
      error: () => undefined, // se vuelve a intentar en la siguiente vuelta
    });
  }

  /** Las muestra solas (máximo 3 a la vez) y las quita a los 8 segundos. */
  private mostrar(nuevas: Notificacion[]): void {
    this.emergentes.update((actuales) => [...nuevas.slice().reverse(), ...actuales].slice(0, MAXIMO_EMERGENTES));
    for (const n of nuevas) setTimeout(() => this.cerrarEmergente(n), DURACION_EMERGENTE_MS);
  }

  cerrarEmergente(n: Notificacion): void {
    this.emergentes.update((actuales) => actuales.filter((x) => x.id !== n.id));
  }

  marcarLeida(n: Notificacion): void {
    this.cerrarEmergente(n);
    if (n.leida) return;
    this.notificaciones.update((lista) => lista.map((x) => (x.id === n.id ? { ...x, leida: true } : x)));
    this.noLeidas.update((c) => Math.max(0, c - 1));
    this.http.post<void>(`${this.url}/${n.id}/leida`, {}).subscribe({ error: () => this.cargar() });
  }

  marcarTodasLeidas(): void {
    this.notificaciones.update((lista) => lista.map((x) => ({ ...x, leida: true })));
    this.noLeidas.set(0);
    this.http.post<void>(`${this.url}/leidas`, {}).subscribe({ error: () => this.cargar() });
  }

  alternarSonido(): void {
    this.conSonido.update((s) => !s);
    try {
      localStorage.setItem(CLAVE_SONIDO, String(this.conSonido()));
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
    if (this.conSonido()) this.sonar(); // para que se escuche cómo suena
  }

  /**
   * Tres notas que suben, generadas en el navegador (sin archivo de audio). Dos ondas por nota (seno + triángulo)
   * para que se oiga fuerte y claro; el compresor evita que se distorsione al subir el volumen.
   */
  sonar(): void {
    if (!this.conSonido()) return;
    this.prepararAudio();
    const ctx = this.audio;
    if (!ctx) return;
    const salida = ctx.createDynamicsCompressor();
    salida.threshold.value = -10;
    salida.ratio.value = 4;
    salida.connect(ctx.destination);
    const inicio = ctx.currentTime;
    for (const [frecuencia, retraso] of [[784, 0], [1047, 0.16], [1319, 0.32]]) {
      const volumen = ctx.createGain();
      volumen.gain.setValueAtTime(0.0001, inicio + retraso);
      volumen.gain.exponentialRampToValueAtTime(VOLUMEN, inicio + retraso + 0.015);
      volumen.gain.exponentialRampToValueAtTime(0.0001, inicio + retraso + 0.5);
      volumen.connect(salida);
      for (const onda of ['sine', 'triangle'] as OscillatorType[]) {
        const nota = ctx.createOscillator();
        nota.type = onda;
        nota.frequency.value = frecuencia;
        nota.connect(volumen);
        nota.start(inicio + retraso);
        nota.stop(inicio + retraso + 0.55);
      }
    }
  }
}

function leerSonido(): boolean {
  try {
    return localStorage.getItem(CLAVE_SONIDO) !== 'false';
  } catch {
    return true;
  }
}
