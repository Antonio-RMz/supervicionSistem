import { Component, DestroyRef, effect, ElementRef, inject, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { haceCuanto } from '../../../../shared/utils/fechas';
import { Notificacion, NotificacionesService, TipoNotificacion } from '../../data/notificaciones.service';

const ICONOS: Record<TipoNotificacion, string> = {
  ASIGNACION: 'bi-person-check',
  BUG_REPORTADO: 'bi-bug',
  COMENTARIO: 'bi-chat-left-text',
};

/** "(3) " al inicio del título de la pestaña. */
const PREFIJO = /^\(\d+\+?\) /;

/**
 * La campana de la barra superior: contador de no leídas y el panel con las más recientes. Lo que acaba de
 * llegar sale solo abajo a la izquierda, y el título de la pestaña dice cuántas hay sin leer.
 */
@Component({
  selector: 'app-campana',
  templateUrl: './campana.html',
  styleUrl: './campana.scss',
  host: { '(document:click)': 'clicAfuera($event)', '(document:keydown.escape)': 'abierto.set(false)' },
})
export class Campana {
  protected readonly servicio = inject(NotificacionesService);
  private readonly router = inject(Router);
  private readonly elemento = inject(ElementRef<HTMLElement>);

  protected readonly abierto = signal(false);
  protected readonly iconos = ICONOS;
  protected readonly haceCuanto = haceCuanto;

  private readonly titulo = inject(Title);

  constructor() {
    this.servicio.iniciar();

    // "(2) Ticket · Gestor de Proyectos": al cambiar el número y al cambiar de página (cada ruta pone su título).
    effect(() => this.ponerTitulo(this.servicio.noLeidas()));
    const navegacion = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => setTimeout(() => this.ponerTitulo(this.servicio.noLeidas())));

    inject(DestroyRef).onDestroy(() => {
      navegacion.unsubscribe();
      this.ponerTitulo(0);
      this.servicio.detener();
    });
  }

  private ponerTitulo(noLeidas: number): void {
    const base = this.titulo.getTitle().replace(PREFIJO, '');
    this.titulo.setTitle(noLeidas ? `(${noLeidas > 99 ? '99+' : noLeidas}) ${base}` : base);
  }

  protected alternar(): void {
    this.abierto.update((a) => !a);
    if (this.abierto()) this.servicio.cargar();
  }

  /** La marca como leída y lleva al ticket o a la historia. */
  protected abrir(n: Notificacion): void {
    this.servicio.marcarLeida(n);
    this.abierto.set(false);
    if (n.ticketId) this.router.navigate(['/proyectos', n.proyectoId, 'tickets', n.ticketId]);
    else if (n.historiaId) this.router.navigate(['/proyectos', n.proyectoId, 'historias', n.historiaId]);
  }

  protected clicAfuera(evento: MouseEvent): void {
    if (this.abierto() && !this.elemento.nativeElement.contains(evento.target as Node)) this.abierto.set(false);
  }
}
