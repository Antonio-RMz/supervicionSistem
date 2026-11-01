import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { HistoriaDetalleService } from '../../data/historia-detalle.service';
import { ListadoHistorias } from '../../models/listado-historias.models';

/**
 * Menú Historias: todas las historias del proyecto elegido arriba, para buscarlas y abrir cada una.
 * Se filtran por texto (código o título), fase y estado.
 */
@Component({
  selector: 'app-lista-historias-page',
  imports: [RouterLink, BarraAvance, EtiquetaEstado, TipoTicket],
  templateUrl: './lista-historias-page.html',
  styleUrl: './lista-historias-page.scss',
})
export class ListaHistoriasPage {
  protected readonly proyectos = inject(ProyectoActualStore);
  private readonly service = inject(HistoriaDetalleService);

  protected readonly listado = signal<ListadoHistorias | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly texto = signal('');
  protected readonly fase = signal('');
  protected readonly estado = signal('');

  /** Las fases y estados que hay, para los filtros. */
  protected readonly opciones = computed(() => {
    const historias = this.listado()?.historias ?? [];
    const fases = new Map(historias.map((h) => [String(h.faseId), h.fase]));
    const estados = new Map(historias.map((h) => [h.estado, h.etiqueta.texto]));
    return { fases: [...fases.entries()], estados: [...estados.entries()] };
  });

  protected readonly filtradas = computed(() => {
    const texto = this.texto().trim().toLowerCase();
    return (this.listado()?.historias ?? []).filter((h) =>
      (!texto || h.codigo.toLowerCase().includes(texto) || h.titulo.toLowerCase().includes(texto))
      && (!this.fase() || String(h.faseId) === this.fase())
      && (!this.estado() || h.estado === this.estado()));
  });

  constructor() {
    // Al cambiar el proyecto de arriba se carga su lista y se limpian los filtros.
    effect(() => {
      const id = this.proyectos.actual()?.id;
      untracked(() => {
        if (id) this.cargar(id);
        else this.cargando.set(false);
      });
    });
  }

  protected limpiar(): void {
    this.texto.set('');
    this.fase.set('');
    this.estado.set('');
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.limpiar();
    this.service.listar(id).subscribe({
      next: (l) => {
        if (this.proyectos.actual()?.id !== id) return; // llegó tarde: ya se eligió otro proyecto
        this.listado.set(l);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos cargar las historias.'));
        this.cargando.set(false);
      },
    });
  }
}
