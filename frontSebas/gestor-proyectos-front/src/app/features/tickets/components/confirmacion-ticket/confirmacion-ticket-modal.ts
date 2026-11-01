import { afterNextRender, Component, computed, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { Modal } from '../../../../shared/components/modal/modal';
import { Movimiento } from '../../models/movimientos.models';
import { Confirmacion, DetalleTicket, SEVERIDADES } from '../../models/ticket-detalle.models';

/** Lo que la ventana agrega al movimiento: el motivo, quién lo bloquea o, al confirmar un bug, su severidad. */
export type DatosConfirmacion = Pick<
  Movimiento,
  'motivo' | 'bloqueadoPorId' | 'destrabadorId' | 'severidad' | 'responsableId'
>;

/**
 * Las ventanas de las acciones del ticket que piden motivo o información (PDF pág. 26):
 * Enviar a QA, Marcar bloqueada, Rechazar en QA y Cancelar; además Retirar de QA. De un bug (pág. 23):
 * Confirmar (severidad y Desarrollador), No reproducible y Publicar parche. De una mejora (pág. 30):
 * Aprobar mejora (Desarrollador).
 * Todo queda en el historial.
 */
@Component({
  selector: 'app-confirmacion-ticket-modal',
  imports: [Modal],
  templateUrl: './confirmacion-ticket-modal.html',
  styleUrl: './confirmacion-ticket-modal.scss',
})
export class ConfirmacionTicketModal {
  readonly confirmacion = input.required<Confirmacion>();
  /** El texto del botón: "Enviar a QA", "Rechazar en QA"... */
  readonly accion = input.required<string>();
  readonly ticket = input.required<DetalleTicket>();
  readonly enviando = input(false);

  readonly confirmar = output<DatosConfirmacion>();
  readonly cancelar = output<void>();

  private readonly primerCampo = viewChild<ElementRef<HTMLElement>>('primero');

  protected readonly texto = signal('');
  protected readonly pasos = signal('');
  /** Al bloquear: otro ticket o algo externo. */
  protected readonly causa = signal<'ticket' | 'externo'>('externo');
  protected readonly bloqueadoPorId = signal<number | null>(null);
  protected readonly destrabadorId = signal<number | null>(null);
  protected readonly intentado = signal(false);
  protected readonly severidades = SEVERIDADES;
  protected readonly severidad = signal<string | null>(null);
  protected readonly responsableId = signal<number | null>(null);

  protected readonly titulo = computed(() => {
    const codigo = this.ticket().codigo;
    switch (this.confirmacion()) {
      case 'ENVIAR_QA':
        return `${this.accion()} · ${codigo}`;
      case 'BLOQUEAR':
        return `Marcar ${codigo} como bloqueada`;
      case 'RECHAZAR':
        return `${this.accion()} · ${codigo}`;
      case 'CANCELAR':
        return `Cancelar ${codigo}`;
      case 'RETIRAR':
        return `${this.accion()} · ${codigo}`;
      case 'CONFIRMAR':
        return `Confirmar ${codigo}`;
      case 'NO_REPRODUCIBLE':
        return `${codigo} no se puede reproducir`;
      case 'PUBLICAR_PARCHE':
        return `Publicar parche ${this.ticket().parche?.version ?? ''}`;
      case 'APROBAR_MEJORA':
        return `Aprobar mejora · ${codigo}`;
    }
  });

  protected readonly subtitulo = computed(() => {
    const t = this.ticket();
    switch (this.confirmacion()) {
      case 'ENVIAR_QA':
        if (this.alPr()) return 'Le llegará al Líder para revisar el PR.';
        return t.tester ? `Le llegará a ${t.tester.nombre} (Analista).` : 'Le llegará a un Analista del equipo.';
      case 'BLOQUEAR':
        return 'Se avisará al Líder y el ticket saldrá en "Detenidos" del Dashboard.';
      case 'RECHAZAR': {
        if (t.estado === 'PUBLICADO') return `Regresa a ${t.responsable?.nombre ?? 'quien lo corrige'} en En corrección.`;
        if (t.estado === 'PROPUESTA') return 'La mejora queda rechazada y el motivo en su historial.';
        const vez = t.rechazosEnQa + 1;
        return `Regresa a ${t.responsable?.nombre ?? 'quien lo trabaja'}. Este será el ${vez}.${vez === 1 ? 'er' : 'º'} rechazo.`;
      }
      case 'CANCELAR':
        return t.titulo;
      case 'RETIRAR':
        return 'Regresa a trabajo antes de que lo revisen.';
      case 'CONFIRMAR':
        return 'Crítica y Alta van por hotfix desde main; Media y Baja salen con la siguiente fase.';
      case 'NO_REPRODUCIBLE':
        return t.titulo;
      case 'PUBLICAR_PARCHE':
        return 'Merge a main, tag, despliegue a producción y regreso a develop en un paso.';
      case 'APROBAR_MEJORA':
        return 'Entra a la fase. Desde aquí sigue igual que una Actividad.';
    }
  });

  /** Lo que se pide al rechazar, según el tipo: "Qué falló", "Qué cambiar"... */
  protected readonly preguntaRechazo = computed(() => {
    switch (this.ticket().estado) {
      case 'PUBLICADO':
        return '¿Qué sigue fallando?';
      case 'PROPUESTA':
        return 'Por qué se rechaza';
      case 'EN_REVISION':
        return 'Qué hay que cambiar en el PR';
      default:
        return 'Qué falló';
    }
  });

  /** Confirmar un bug o aprobar una mejora lo asigna: solo a un Desarrollador. */
  protected readonly desarrolladores = computed(() =>
    this.ticket().equipo.filter((p) => p.roles.includes('DESARROLLADOR')),
  );

  /** Enviar PR: lo revisa el Líder. Si no, es un hotfix que va a un Analista. */
  protected readonly alPr = computed(() => this.ticket().opciones.some((o) => o.destino === 'EN_REVISION'));

  /** "Pasos para reproducir" solo tiene sentido en QA. */
  protected readonly conPasos = computed(() => ['EN_QA', 'PUBLICADO'].includes(this.ticket().estado));

  /** "Faltan 2 subtareas: …" (solo aviso, no bloquea). */
  protected readonly subtareasPendientes = computed(() => this.ticket().subtareas.filter((s) => !s.hecha));

  protected readonly textoSubtareas = computed(() => {
    const pendientes = this.subtareasPendientes();
    const lista = pendientes.map((s) => s.descripcion.toLowerCase()).join(', ');
    return pendientes.length === 1 ? `Falta 1 subtarea: ${lista}` : `Faltan ${pendientes.length} subtareas: ${lista}`;
  });

  /** Los criterios que solo cubre este ticket: si se cancela se quedan sin cubrir. */
  protected readonly criteriosSinCubrir = computed(() => this.ticket().criterios.filter((c) => c.soloEste));

  protected readonly textoCriterios = computed(() => {
    const criterios = this.criteriosSinCubrir().map((c) => `“${c.descripcion}”`).join(' y ');
    const historia = this.ticket().historia?.codigo ?? 'la historia';
    return this.criteriosSinCubrir().length === 1
      ? `Este criterio de ${historia} se quedará sin cubrir: ${criterios}.`
      : `Estos criterios de ${historia} se quedarán sin cubrir: ${criterios}.`;
  });

  /** Retirar no pide motivo; Confirmar y Publicar parche no tienen campo de texto. */
  protected readonly faltaTexto = computed(
    () => this.intentado()
      && ['ENVIAR_QA', 'BLOQUEAR', 'RECHAZAR', 'CANCELAR', 'NO_REPRODUCIBLE'].includes(this.confirmacion())
      && !this.texto().trim(),
  );
  protected readonly faltaSeveridad = computed(
    () => this.intentado() && this.confirmacion() === 'CONFIRMAR' && this.severidad() === null,
  );
  protected readonly faltaResponsable = computed(
    () => this.intentado() && ['CONFIRMAR', 'APROBAR_MEJORA'].includes(this.confirmacion()) && this.responsableId() === null,
  );
  protected readonly faltaTicket = computed(
    () => this.intentado() && this.causa() === 'ticket' && this.bloqueadoPorId() === null,
  );

  constructor() {
    afterNextRender(() => {
      // Al confirmar, lo que el bug ya tenga queda elegido.
      this.severidad.set(this.ticket().severidad?.codigo ?? null);
      this.responsableId.set(this.ticket().responsable?.id ?? null);
      this.primerCampo()?.nativeElement.focus();
    });
  }

  protected enviar(): void {
    this.intentado.set(true);
    const texto = this.texto().trim();
    if (this.faltaTexto() || this.faltaTicket() || this.faltaSeveridad() || this.faltaResponsable()) {
      return;
    }
    switch (this.confirmacion()) {
      case 'RECHAZAR': {
        const pasos = this.pasos().trim();
        this.confirmar.emit({ motivo: pasos ? `${texto}\nPasos para reproducir: ${pasos}` : texto });
        break;
      }
      case 'BLOQUEAR':
        this.confirmar.emit({
          motivo: texto,
          bloqueadoPorId: this.causa() === 'ticket' ? this.bloqueadoPorId()! : undefined,
          destrabadorId: this.destrabadorId() ?? undefined,
        });
        break;
      case 'CONFIRMAR':
        this.confirmar.emit({ severidad: this.severidad()!, responsableId: this.responsableId()! });
        break;
      case 'APROBAR_MEJORA':
        this.confirmar.emit({ responsableId: this.responsableId()! });
        break;
      default:
        this.confirmar.emit({ motivo: texto || undefined });
    }
  }

  protected elegirBloqueador(valor: string): void {
    this.bloqueadoPorId.set(valor ? Number(valor) : null);
  }

  protected elegirResponsable(valor: string): void {
    this.responsableId.set(valor ? Number(valor) : null);
  }

  protected elegirDestrabador(valor: string): void {
    this.destrabadorId.set(valor ? Number(valor) : null);
  }
}
