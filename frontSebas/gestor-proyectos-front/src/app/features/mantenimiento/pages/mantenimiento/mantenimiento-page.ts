import { Component, computed, DestroyRef, effect, inject, input, signal, untracked } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Modal } from '../../../../shared/components/modal/modal';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { TicketDetalleService } from '../../../tickets/data/ticket-detalle.service';
import { NuevoTicketModal } from '../../components/nuevo-ticket-modal/nuevo-ticket-modal';
import { ReportarFallaModal } from '../../components/reportar-falla-modal/reportar-falla-modal';
import { MantenimientoService } from '../../data/mantenimiento.service';
import { Mantenimiento, PreparacionMantenimiento, TicketMantenimiento } from '../../models/mantenimiento.models';

/** Los tres grupos de la vista, con "todos" para no filtrar. */
type Grupo = 'todos' | 'urgentes' | 'listos' | 'normales';

/**
 * Bugs (antes Mantenimiento, PDF pág. 37): la fase permanente del proyecto, como lista de tarjetas.
 * Lo urgente (Crítica y Alta) va por hotfix desde main y sale en un parche; lo normal se trabaja desde
 * develop y sale con la siguiente fase. Abajo quedan publicar el parche y los parches publicados.
 */
@Component({
  selector: 'app-mantenimiento-page',
  imports: [NgTemplateOutlet, RouterLink, Avatar, EtiquetaEstado, Modal, NuevoTicketModal, ReportarFallaModal, TipoTicket],
  templateUrl: './mantenimiento-page.html',
  styleUrl: './mantenimiento-page.scss',
})
export class MantenimientoPage {
  private readonly service = inject(MantenimientoService);
  private readonly tickets = inject(TicketDetalleService);
  private readonly proyectos = inject(ProyectoActualStore);

  /** :id del proyecto */
  readonly id = input.required<string>();

  protected readonly mantenimiento = signal<Mantenimiento | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly confirmando = signal(false);
  protected readonly publicando = signal(false);
  /** Módulos, equipo y permisos para los botones Nuevo ticket y Reportar falla. */
  protected readonly preparacion = signal<PreparacionMantenimiento | null>(null);
  protected readonly creandoTicket = signal(false);
  protected readonly reportandoFalla = signal(false);

  protected readonly filtro = signal<Grupo>('todos');
  /** URL local de la miniatura de cada bug con imagen, por id de ticket. */
  protected readonly miniaturas = signal<Record<number, string>>({});

  /** Todos los bugs abiertos, del más urgente al normal, con su grupo. */
  private readonly todos = computed(() => {
    const m = this.mantenimiento();
    if (!m) return [];
    const con = (lista: TicketMantenimiento[], grupo: Grupo) => lista.map((ticket) => ({ ticket, grupo }));
    return [...con(m.urgentes, 'urgentes'), ...con(m.listos, 'listos'), ...con(m.normales, 'normales')];
  });

  protected readonly visibles = computed(() => {
    const filtro = this.filtro();
    return filtro === 'todos' ? this.todos() : this.todos().filter((b) => b.grupo === filtro);
  });

  protected readonly filtros = computed(() => {
    const m = this.mantenimiento();
    return [
      { grupo: 'todos' as Grupo, texto: 'Todos', cuenta: this.todos().length },
      { grupo: 'urgentes' as Grupo, texto: 'Urgentes · hotfix', cuenta: m?.urgentes.length ?? 0 },
      { grupo: 'listos' as Grupo, texto: 'Listos para el parche', cuenta: m?.listos.length ?? 0 },
      { grupo: 'normales' as Grupo, texto: 'Con la siguiente fase', cuenta: m?.normales.length ?? 0 },
    ];
  });

  /** "Va con la Fase 5" */
  protected readonly vaCon = computed(() => {
    const texto = this.mantenimiento()?.conLaFase ?? 'va con la siguiente fase';
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  });

  protected readonly proyectoNombre = computed(
    () => this.proyectos.proyectos().find((p) => p.id === Number(this.id()))?.nombre ?? 'Proyecto',
  );

  constructor() {
    // Cada vez que llega la lista se piden las miniaturas; las anteriores se liberan.
    effect((onCleanup) => {
      const proyectoId = Number(this.id());
      const conImagen = this.todos().filter((b) => b.ticket.imagenId);
      const pedidas = conImagen.map((b) =>
        this.service.imagen(proyectoId, b.ticket.id, b.ticket.imagenId!).subscribe({
          next: (blob) => this.miniaturas.update((m) => ({ ...m, [b.ticket.id]: URL.createObjectURL(blob) })),
          error: () => undefined, // sin miniatura: la tarjeta muestra el ícono
        }),
      );
      onCleanup(() => {
        pedidas.forEach((s) => s.unsubscribe());
        this.liberarMiniaturas();
      });
    });
    inject(DestroyRef).onDestroy(() => this.liberarMiniaturas());

    effect(() => {
      const id = Number(this.id());
      untracked(() => {
        this.proyectos.elegir(id); // el selector de arriba muestra este proyecto
        this.cargar(id);
        this.service.preparar(id).subscribe({ next: (p) => this.preparacion.set(p), error: () => this.preparacion.set(null) });
      });
    });
  }

  /** Salen juntos todos los hotfix aprobados; el back toma el parche completo a partir de cualquiera. */
  protected publicar(): void {
    const m = this.mantenimiento();
    const primero = m?.listos[0];
    if (!m || !primero) return;
    this.publicando.set(true);
    this.aviso.set(null);
    this.tickets.publicarParche(Number(this.id()), primero.id, m.siguienteParche).subscribe({
      next: (parche) => {
        this.publicando.set(false);
        this.confirmando.set(false);
        this.aviso.set({ texto: `Se publicó el parche ${parche.version}: ya está en producción y en develop.`, error: false });
        this.cargar(Number(this.id()));
      },
      error: (e) => {
        this.publicando.set(false);
        this.confirmando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos publicar el parche.'), error: true });
        this.cargar(Number(this.id()));
      },
    });
  }

  protected ticketCreado(codigo: string): void {
    this.creandoTicket.set(false);
    this.aviso.set({ texto: `Se creó ${codigo} en Mantenimiento.`, error: false });
    this.recargar();
  }

  protected fallaReportada(falla: { codigo: string; hotfix: boolean; imagenesFallidas: boolean }): void {
    this.reportandoFalla.set(false);
    const ruta = falla.hotfix ? 'Irá por hotfix en cuanto el Líder lo confirme.' : 'Irá con la siguiente fase.';
    if (falla.imagenesFallidas) {
      this.aviso.set({
        texto: `Se reportó ${falla.codigo}, pero alguna imagen no se pudo subir. ${ruta}`,
        error: true,
      });
    } else {
      this.aviso.set({ texto: `Se reportó ${falla.codigo}. ${ruta}`, error: false });
    }
    this.recargar();
  }

  private liberarMiniaturas(): void {
    untracked(() => {
      Object.values(this.miniaturas()).forEach((url) => URL.revokeObjectURL(url));
      this.miniaturas.set({});
    });
  }

  /** Tras crear, el siguiente número de cada tipo cambia. */
  private recargar(): void {
    const id = Number(this.id());
    this.cargar(id);
    this.service.preparar(id).subscribe({ next: (p) => this.preparacion.set(p) });
  }

  private cargar(id: number): void {
    this.service.mantenimiento(id).subscribe({
      next: (m) => {
        this.mantenimiento.set(m);
        this.cargando.set(false);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar Mantenimiento.'));
        this.cargando.set(false);
      },
    });
  }
}
