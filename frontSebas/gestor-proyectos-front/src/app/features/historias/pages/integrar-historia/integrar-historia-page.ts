import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { HistoriaDetalleService } from '../../data/historia-detalle.service';
import { IntegracionHistoria } from '../../models/integracion.models';

/**
 * Integrar historia (PDF pág. 28): qué entra a develop por módulo, la revisión previa y qué pasará.
 * Solo quien tiene el permiso de integrar (el Líder) ve los botones; los demás la ven igual.
 */
@Component({
  selector: 'app-integrar-historia-page',
  imports: [RouterLink, EtiquetaEstado, TipoTicket],
  templateUrl: './integrar-historia-page.html',
  styleUrl: './integrar-historia-page.scss',
})
export class IntegrarHistoriaPage implements OnInit {
  private readonly service = inject(HistoriaDetalleService);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly historiaId = input.required<string>();

  protected readonly integracion = signal<IntegracionHistoria | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  protected integrar(): void {
    const i = this.integracion();
    if (!i || i.impedimentos.length) return;
    this.enviando.set(true);
    this.aviso.set(null);
    this.service.ejecutar(i.proyectoId, i.historiaId, 'INTEGRAR', i.estado).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.set({ texto: `${i.codigo} se integró a develop. Staging se actualiza con el CI.`, error: false });
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos integrar ${i.codigo}.`), error: true });
        this.cargar();
      },
    });
  }

  private cargar(): void {
    this.service.integracion(Number(this.id()), Number(this.historiaId())).subscribe({
      next: (i) => {
        this.integracion.set(i);
        this.cargando.set(false);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar la historia.'));
        this.cargando.set(false);
      },
    });
  }
}
