import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  HostListener,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, isPlatformBrowser, NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY } from 'rxjs';
import { catchError, distinctUntilChanged, filter, finalize, map, switchMap } from 'rxjs/operators';
import { SessionService } from '../../../../core/services/session.service';
import { TicketService } from '../../../../core/services/ticket.service';
import { ProyectoService } from '../../../../core/services/proyecto.service';
import { TicketProyectoResumenResponse } from '../../../../core/models/proyecto.model';
import { AccionDisponible, TicketComentarioResponse, TicketDetalleApi, TicketFaseDefinicionResponse, TicketRelacionadoResponse, UsuarioComentarioResponse } from '../../../../core/models/ticket.model';
import { EstadoBadgeComponent } from '../../../../shared/components/estado-badge/estado-badge.component';
import { AsignarAnalistaModalComponent } from './asignar-analista-modal/asignar-analista-modal.component';
import { CrearHistoriaModalComponent } from '../crear-historia-modal/crear-historia-modal.component';

// ─── Tab ─────────────────────────────────────────────────────────────────────
type Tab = 'resumen' | 'historial';

// ─── Tipos locales ────────────────────────────────────────────────────────────
interface TipoTicket { code: string; label: string; icon: string; color: string; }

interface ContextNode {
  folio:  string;
  titulo: string;
  link:   (string | number)[] | null;
}

interface AccionModalConfig {
  codigo:                string;
  titulo:                string;
  mensaje:               string;
  etiquetaCampo:         string;
  placeholder:           string;
  comentarioObligatorio: boolean;
  textoConfirmar:        string;
  tipo:                  'normal' | 'warning' | 'danger' | 'success';
}

// ─── Catálogos ────────────────────────────────────────────────────────────────
const TIPOS_TICKET: TipoTicket[] = [
  { code: 'FASE', label: 'Fase',                icon: 'bi-layers',          color: '#4361ee' },
  { code: 'HIST', label: 'Historia de usuario', icon: 'bi-journal-text',    color: '#7209b7' },
  { code: 'ACT',  label: 'Actividad',           icon: 'bi-check2-square',   color: '#f72585' },
  { code: 'ACTV', label: 'Actividad',           icon: 'bi-check2-square',   color: '#f72585' },
  { code: 'BUG',  label: 'Error',               icon: 'bi-bug',             color: '#ef4444' },
  { code: 'MEJ',  label: 'Mejora',              icon: 'bi-arrow-up-circle', color: '#0d9488' },
  { code: 'TEC',  label: 'Tarea técnica',       icon: 'bi-gear',            color: '#64748b' },
  { code: 'INV',  label: 'Investigación',       icon: 'bi-search',          color: '#e67e22' },
  { code: 'DOC',  label: 'Documentación',       icon: 'bi-file-text',       color: '#8e44ad' },
];

const PRIORIDAD_CFG: Record<string, { label: string; css: string }> = {
  ALTA:  { label: 'Alta',  css: 'prio-alta'  },
  MEDIA: { label: 'Media', css: 'prio-media' },
  BAJA:  { label: 'Baja',  css: 'prio-baja'  },
};

const ACCION_ICONO: Record<string, string> = {
  INICIAR:     'bi-play-fill',
  APROBAR:     'bi-hand-thumbs-up-fill',
  ENVIAR:      'bi-send-fill',
  COMPLETAR:   'bi-check2-all',
  FINALIZAR:   'bi-check-circle-fill',
  BLOQUEAR:    'bi-lock-fill',
  DESBLOQUEAR: 'bi-unlock-fill',
  CANCELAR:    'bi-x-circle-fill',
  REABRIR:     'bi-arrow-counterclockwise',
  RECHAZAR:    'bi-slash-circle-fill',
  ASIGNAR:     'bi-person-plus-fill',
};

const AVATAR_COLORS = [
  '#4361ee', '#7209b7', '#f72585', '#3a0ca3',
  '#0d9488', '#e67e22', '#ef4444', '#8e44ad',
  '#0891b2', '#65a30d',
];

@Component({
  selector: 'app-ticket-detalle',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, EstadoBadgeComponent, NgClass, DatePipe, AsignarAnalistaModalComponent, CrearHistoriaModalComponent],
  templateUrl: './ticket-detalle.component.html',
  styleUrl:    './ticket-detalle.component.scss',
})
export class TicketDetalleComponent implements OnInit, OnDestroy {
  protected readonly session      = inject(SessionService);
  private  readonly ticketService = inject(TicketService);
  private  readonly proyectoSvc   = inject(ProyectoService);
  private  readonly route         = inject(ActivatedRoute);
  private  readonly router        = inject(Router);
  private  readonly platformId    = inject(PLATFORM_ID);
  private  readonly destroyRef    = inject(DestroyRef);

  // ── Signals del ticket ────────────────────────────────────────────────────
  protected readonly ticket          = signal<TicketDetalleApi | null>(null);
  protected readonly cargando        = signal(false);
  protected readonly error           = signal<string | null>(null);
  protected readonly accionEnProceso = signal<string | null>(null);
  protected readonly notificacion    = signal<string | null>(null);
  protected readonly historial       = signal<never[]>([]);

  // ── Signals de comentarios ────────────────────────────────────────────────
  protected readonly comentarios         = signal<TicketComentarioResponse[]>([]);
  protected readonly comentarioTexto     = signal('');
  protected readonly cargandoComentarios = signal(false);
  protected readonly enviandoComentario  = signal(false);
  protected readonly errorComentarios    = signal<string | null>(null);

  // ── UI ────────────────────────────────────────────────────────────────────
  protected readonly tabActual                = signal<Tab>('resumen');
  protected readonly menuAccionesAbierto      = signal(false);
  protected readonly modalAsignarAnalistaOpen = signal(false);
  protected readonly modalHistoriaAbierto     = signal(false);
  protected readonly proyectoIdActual         = signal<number>(0);

  // ── Definición funcional FASE ─────────────────────────────────────────────
  protected readonly definicionFase      = signal<TicketFaseDefinicionResponse | null>(null);
  protected readonly cargandoDefinicion  = signal(false);
  protected readonly errorDefinicion     = signal<string | null>(null);

  // ── Modales definición funcional ──────────────────────────────────────────
  protected readonly modalObjetivoOpen   = signal(false);
  protected readonly modalObjetivoTexto  = signal('');

  protected readonly modalAlcanceOpen    = signal(false);
  protected readonly modalAlcanceIncluye = signal('');
  protected readonly modalAlcanceFuera   = signal('');

  protected readonly modalReqOpen        = signal(false);
  protected readonly modalReqCodigo      = signal('');
  protected readonly modalReqTitulo      = signal('');
  protected readonly modalReqPrioridad   = signal('');
  protected readonly modalReqEstado      = signal('');

  protected readonly modalReglaOpen      = signal(false);
  protected readonly modalReglaCodigo    = signal('');
  protected readonly modalReglaDesc      = signal('');

  protected readonly modalCriterioOpen   = signal(false);
  protected readonly modalCriterioDesc   = signal('');

  // ── Estado de guardado por modal ──────────────────────────────────────────
  protected readonly guardandoObjetivo  = signal(false);
  protected readonly errorObjetivo      = signal<string | null>(null);
  protected readonly guardandoAlcance   = signal(false);
  protected readonly errorAlcance       = signal<string | null>(null);
  protected readonly guardandoReq       = signal(false);
  protected readonly errorReq           = signal<string | null>(null);
  protected readonly guardandoRegla     = signal(false);
  protected readonly errorRegla         = signal<string | null>(null);
  protected readonly guardandoCriterio  = signal(false);
  protected readonly errorCriterio      = signal<string | null>(null);

  private accionAnalistaActual: AccionDisponible | null = null;

  // ── Modal de confirmación de acción ──────────────────────────────────────
  protected readonly modalAccionAbierto = signal(false);
  protected readonly accionSeleccionada = signal<AccionModalConfig | null>(null);
  protected readonly comentarioAccion   = signal('');
  protected readonly guardandoAccion    = signal(false);
  protected readonly errorAccion        = signal<string | null>(null);

  protected folioParam = '';
  protected idProyecto = 0;
  private   notificacionTimer: ReturnType<typeof setTimeout> | null = null;

  // ── Computeds ─────────────────────────────────────────────────────────────
  protected readonly proyectoActivo = computed(() => this.session.proyectoActivo());

  protected readonly tipoCfg = computed(() => {
    const codigo = this.ticket()?.tipo.codigo ?? '';
    return TIPOS_TICKET.find(t => t.code === codigo) ?? TIPOS_TICKET[0];
  });

  protected readonly tabsAdaptados = computed<{ id: Tab; label: string; icon: string }[]>(() => [
    { id: 'resumen',   label: 'Resumen',   icon: 'bi-grid-1x2'      },
    { id: 'historial', label: 'Historial', icon: 'bi-clock-history' },
  ]);

  protected readonly puedeEditarDefinicion = computed(() => {
    const t = this.ticket();
    if (!t || t.tipo.codigo !== 'FASE' || t.estado.codigo !== 'DEFI') return false;
    const rol = this.session.usuario()?.rol ?? '';
    return ['ANALISTA_COMERCIAL', 'LÍDER_DE_PROYECTO', 'ADMINISTRADOR'].includes(rol);
  });

  protected readonly historiasBotonesVisibles = computed(() => {
    const t = this.ticket();
    if (!t || t.tipo.codigo !== 'FASE') return false;
    return !['BORRADOR', 'DEFI', 'PENDIENTE_VALIDACION'].includes(t.estado.codigo);
  });

  // Historias de la FASE cargadas vía proyectoSvc (ticketsHijos del detalle viene vacío del backend)
  protected readonly historiasDeFase = signal<TicketRelacionadoResponse[]>([]);

  protected readonly historiasFase = computed(() => this.historiasDeFase());

  protected readonly ubicacionItems = computed<ContextNode[]>(() => {
    const t    = this.ticket();
    const proy = this.proyectoActivo();
    if (!t) return [];
    const items: ContextNode[] = [];
    if (proy) {
      items.push({ folio: proy.iniciales, titulo: proy.nombre, link: ['/pro', proy.id] });
    }
    items.push({ folio: t.folio, titulo: t.titulo, link: null });
    return items;
  });

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.idProyecto = Number(this.route.snapshot.paramMap.get('id') ?? 0);

    this.route.paramMap
      .pipe(
        map(params => params.get('folio')),
        filter((folio): folio is string => !!folio),
        distinctUntilChanged(),
        switchMap(folio => {
          this.folioParam = folio;
          this.cargando.set(true);
          this.error.set(null);
          this.comentarios.set([]);
          this.errorComentarios.set(null);
          this.definicionFase.set(null);
          this.errorDefinicion.set(null);
          this.historiasDeFase.set([]);
          return this.ticketService.obtenerDetallePorFolio(folio).pipe(
            catchError(err => {
              this.error.set(err?.error?.mensaje ?? 'No se pudo cargar el ticket.');
              this.cargando.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ticketApi => {
          console.log('Ticket completo:', ticketApi);
          console.log('ticketsHijos del backend:', ticketApi.ticketsHijos);

          this.ticket.set(ticketApi);
          this.folioParam = ticketApi.folio;
          this.tabActual.set('resumen');
          this.cargando.set(false);
          this.cargarComentarios(ticketApi.id);

          if (ticketApi.tipo.codigo === 'FASE') {
            this.cargarDefinicionFase(ticketApi.id);
            this.cargarHistoriasDeFase(ticketApi);
          }
        },
      });
  }

  ngOnDestroy(): void {
    if (this.notificacionTimer) clearTimeout(this.notificacionTimer);
  }

  // ── Tabs ──────────────────────────────────────────────────────────────────
  protected setTab(tab: Tab): void { this.tabActual.set(tab); }

  // ── Dropdown de acciones ──────────────────────────────────────────────────
  protected toggleMenuAcciones(): void {
    if (!this.menuAccionesAbierto()) {
      document.dispatchEvent(new CustomEvent('appCloseMenus'));
    }
    this.menuAccionesAbierto.update(v => !v);
  }

  @HostListener('document:appCloseMenus')
  protected onCloseMenus(): void {
    this.menuAccionesAbierto.set(false);
  }

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    if (this.modalAccionAbierto()) {
      this.cerrarModalAccion();
      return;
    }
    if (this.modalHistoriaAbierto()) {
      this.modalHistoriaAbierto.set(false);
      return;
    }
    this.menuAccionesAbierto.set(false);
  }

  protected ejecutarYCerrar(accion: AccionDisponible): void {
    this.menuAccionesAbierto.set(false);

    console.log('Acción seleccionada:', accion.codigo);

    switch (accion.codigo) {
      case 'ASIGNAR_RESPONSABLE': {
        const ticketActual = this.ticket();
        if (!ticketActual) return;

        const proyectoId = ticketActual.proyectoId ?? ticketActual.proyecto?.id;

        if (!proyectoId || proyectoId <= 0) {
          console.error('No se encontró un proyectoId válido en el ticket:', ticketActual);
          this.error.set('No fue posible identificar el proyecto del ticket.');
          return;
        }

        console.log('Abriendo modal de asignación — proyectoId:', proyectoId);
        this.proyectoIdActual.set(proyectoId);
        this.accionAnalistaActual = accion;
        this.modalAsignarAnalistaOpen.set(true);
        return;
      }
      default:
        this.abrirModalAccion(accion);
    }
  }

  protected menuItemCss(codigo: string): string {
    if (codigo.startsWith('CANCELAR_') || codigo.startsWith('RECHAZAR_')) return 'item-danger';
    if (
      codigo.startsWith('FINALIZAR_') ||
      codigo.startsWith('COMPLETAR_') ||
      codigo.startsWith('APROBAR_')
    ) return 'item-success';
    if (codigo.startsWith('BLOQUEAR_')) return 'item-warning';
    return '';
  }

  // ── Acciones disponibles ──────────────────────────────────────────────────
  protected ejecutarAccionDirecta(accion: AccionDisponible): void {
    if (this.accionEnProceso()) return;

    if (this.requiereConfirmacion(accion.codigo)) {
      if (!isPlatformBrowser(this.platformId)) return;
      if (!window.confirm(this.obtenerMensajeConfirmacion(accion))) return;
    }

    const ticketActual = this.ticket();
    if (!ticketActual) return;

    this.accionEnProceso.set(accion.codigo);
    this.error.set(null);

    this.ticketService
      .ejecutarAccion(ticketActual.id, accion.codigo)
      .pipe(
        catchError(err => {
          this.error.set(err?.error?.mensaje ?? 'No fue posible ejecutar la acción.');
          this.accionEnProceso.set(null);
          return EMPTY;
        }),
      )
      .subscribe({
        next: ticketActualizado => {
          this.ticket.set(ticketActualizado);
          this.accionEnProceso.set(null);
          this.mostrarNotificacionExito(`"${accion.nombre}" ejecutado correctamente.`);
        },
      });
  }

  protected requiereConfirmacion(codigoAccion: string): boolean {
    return (
      codigoAccion.startsWith('CANCELAR_') ||
      codigoAccion.startsWith('REABRIR_')  ||
      codigoAccion.startsWith('FINALIZAR_')
    );
  }

  protected obtenerMensajeConfirmacion(accion: AccionDisponible): string {
    if (accion.codigo.startsWith('CANCELAR_'))  return `¿Cancelar "${accion.nombreEstadoDestino}"?`;
    if (accion.codigo.startsWith('REABRIR_'))   return '¿Reabrir este ticket?';
    if (accion.codigo.startsWith('FINALIZAR_')) return '¿Confirmas finalizar este ticket?';
    return `¿Confirmas "${accion.nombre}"?`;
  }

  // ── Modal de confirmación de acción ──────────────────────────────────────
  protected abrirModalAccion(accion: AccionDisponible): void {
    this.errorAccion.set(null);
    this.comentarioAccion.set('');

    switch (accion.codigo) {
      case 'CANCELAR_FASE':
        this.accionSeleccionada.set({
          codigo:                accion.codigo,
          titulo:                'Cancelar fase',
          mensaje:               '¿Estás seguro de que deseas cancelar esta fase?',
          etiquetaCampo:         'Motivo de cancelación',
          placeholder:           'Agrega un comentario (opcional)...',
          comentarioObligatorio: false,
          textoConfirmar:        'Cancelar fase',
          tipo:                  'danger',
        });
        break;

      case 'SOLICITAR_CAMBIOS':
        this.accionSeleccionada.set({
          codigo:                accion.codigo,
          titulo:                'Solicitar cambios',
          mensaje:               'Indica qué cambios debe realizar el responsable antes de continuar.',
          etiquetaCampo:         'Comentario de cambios',
          placeholder:           'Agrega un comentario (opcional)...',
          comentarioObligatorio: false,
          textoConfirmar:        'Solicitar cambios',
          tipo:                  'warning',
        });
        break;

      case 'APROBAR_DEFINICION':
        this.accionSeleccionada.set({
          codigo:                accion.codigo,
          titulo:                'Aprobar definición',
          mensaje:               'Confirma que la definición funcional está completa y lista para continuar.',
          etiquetaCampo:         'Comentario',
          placeholder:           'Agrega un comentario (opcional)...',
          comentarioObligatorio: false,
          textoConfirmar:        'Aprobar definición',
          tipo:                  'success',
        });
        break;

      default:
        this.ejecutarAccionDirecta(accion);
        return;
    }

    this.modalAccionAbierto.set(true);
  }

  protected cerrarModalAccion(): void {
    if (this.guardandoAccion()) return;
    this.modalAccionAbierto.set(false);
    this.errorAccion.set(null);
  }

  protected onHistoriaCreada(): void {
    this.modalHistoriaAbierto.set(false);
    const t = this.ticket();
    if (t) this.cargarHistoriasDeFase(t);
  }

  protected obtenerProyectoIdActual(): number | null {
    const t = this.ticket();
    const fromTicket = t?.proyectoId ?? t?.proyecto?.id ?? null;
    const fromRoute  = this.idProyecto > 0 ? this.idProyecto : null;
    const result     = (fromTicket && fromTicket > 0 ? fromTicket : null) ?? fromRoute;
    console.log('[Ticket Detalle] obtenerProyectoIdActual:', result, '(ticket:', fromTicket, ', route:', fromRoute, ')');
    return result;
  }

  protected abrirModalCrearHistoria(): void {
    const proyectoId = this.obtenerProyectoIdActual();
    const faseId     = this.ticket()?.id ?? null;
    if (!proyectoId || !faseId) {
      console.error('[Ticket Detalle] No se pudo identificar proyecto/fase para crear historia.', { proyectoId, faseId });
      return;
    }
    this.modalHistoriaAbierto.set(true);
  }

  protected confirmarAccion(): void {
    const ticketId = this.ticket()?.id;
    const accion   = this.accionSeleccionada();
    if (!ticketId || !accion || this.guardandoAccion()) return;

    const comentario = this.comentarioAccion().trim();

    this.guardandoAccion.set(true);
    this.errorAccion.set(null);

    this.ticketService
      .ejecutarAccion(ticketId, accion.codigo, { comentario: comentario || undefined })
      .pipe(
        finalize(() => this.guardandoAccion.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ticketActualizado => {
          this.ticket.set(ticketActualizado);
          this.modalAccionAbierto.set(false);
          this.comentarioAccion.set('');
          this.accionSeleccionada.set(null);
          this.mostrarNotificacionExito('La acción se ejecutó correctamente.');
          if (ticketActualizado.tipo.codigo === 'FASE') {
            this.cargarDefinicionFase(ticketId);
          }
          this.cargarComentarios(ticketId);
        },
        error: err => {
          this.errorAccion.set(
            err?.error?.mensaje ?? 'No fue posible ejecutar la acción.',
          );
        },
      });
  }

  protected obtenerIconoAccion(codigoAccion: string): string {
    if (ACCION_ICONO[codigoAccion]) return ACCION_ICONO[codigoAccion];
    for (const [prefix, icon] of Object.entries(ACCION_ICONO)) {
      if (codigoAccion.startsWith(prefix + '_')) return icon;
    }
    return 'bi-arrow-right';
  }

  // ── Modal Asignar Responsable ──────────────────────────────────────────────
  protected onResponsableSeleccionado(idUsuarioResponsable: number): void {
    const accion = this.accionAnalistaActual;
    this.modalAsignarAnalistaOpen.set(false);
    this.accionAnalistaActual = null;

    if (!accion) return;

    console.log('Resultado del modal:', { confirmado: true, idUsuarioResponsable });
    console.log('Payload enviado:', { idUsuarioResponsable });

    this.ejecutarAsignacionResponsable(accion.codigo, idUsuarioResponsable);
  }

  private ejecutarAsignacionResponsable(codigoAccion: string, idUsuarioResponsable: number): void {
    const ticketActual = this.ticket();
    if (!ticketActual) return;

    this.accionEnProceso.set(codigoAccion);
    this.error.set(null);

    this.ticketService
      .ejecutarAccion(ticketActual.id, codigoAccion, { idUsuarioResponsable })
      .pipe(
        catchError(err => {
          this.error.set(err?.error?.mensaje ?? 'No fue posible asignar el responsable.');
          this.accionEnProceso.set(null);
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.accionEnProceso.set(null);
          this.mostrarNotificacionExito('Responsable asignado correctamente.');
          this.recargarTicket();
        },
      });
  }

  private cargarHistoriasDeFase(ticket: TicketDetalleApi): void {
    const proyectoId =
      ticket.proyectoId ??
      ticket.proyecto?.id ??
      this.session.proyectoId() ??
      undefined;

    if (!proyectoId) {
      console.warn('[TicketDetalle] proyectoId no disponible — no se pueden cargar historias de la fase');
      return;
    }

    this.proyectoSvc
      .obtenerTickets(proyectoId, { idTicketPadre: ticket.id, tamanio: 500, pagina: 0 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: pagina => {
          const historias = pagina.contenido
            .filter(t => t.tipo?.codigo === 'HIST')
            .map((t): TicketRelacionadoResponse => ({
              id:         t.id,
              folio:      t.folio,
              titulo:     t.titulo,
              tipoTicket: t.tipo?.codigo ?? 'HIST',
              estado:     t.estado?.nombre ?? '',
            }));
          console.log('[TicketDetalle] historias cargadas para FASE:', historias);
          this.historiasDeFase.set(historias);
        },
        error: () => this.historiasDeFase.set([]),
      });
  }

  private recargarTicket(): void {
    const folio = this.folioParam;
    if (!folio) return;
    this.ticketService
      .obtenerDetallePorFolio(folio)
      .pipe(
        catchError(err => {
          this.error.set(err?.error?.mensaje ?? 'No se pudo recargar el ticket.');
          return EMPTY;
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ticketActualizado => this.ticket.set(ticketActualizado),
      });
  }

  // ── Helpers de presentación ───────────────────────────────────────────────
  protected tipoCfgPara(code: string): TipoTicket | undefined {
    return TIPOS_TICKET.find(t => t.code === code);
  }

  protected tipoColor(code: string): string {
    return TIPOS_TICKET.find(t => t.code === code)?.color ?? '#6c757d';
  }

  protected tipoIcono(code: string): string {
    return TIPOS_TICKET.find(t => t.code === code)?.icon ?? 'bi-ticket';
  }

  protected prio(code: string): { label: string; css: string } {
    return PRIORIDAD_CFG[code] ?? { label: code, css: '' };
  }

  private mostrarNotificacionExito(mensaje: string): void {
    if (this.notificacionTimer) clearTimeout(this.notificacionTimer);
    this.notificacion.set(mensaje);
    this.notificacionTimer = setTimeout(() => this.notificacion.set(null), 4000);
  }

  // ── Comentarios ───────────────────────────────────────────────────────────
  private cargarComentarios(idTicket: number): void {
    this.cargandoComentarios.set(true);
    this.errorComentarios.set(null);

    this.ticketService
      .obtenerComentarios(idTicket)
      .pipe(finalize(() => this.cargandoComentarios.set(false)))
      .subscribe({
        next: comentarios => {
          this.comentarios.set(comentarios ?? []);
        },
        error: (error: unknown) => {
          console.error('Error al obtener comentarios', error);
          this.comentarios.set([]);
          this.errorComentarios.set('No se pudieron cargar los comentarios.');
        },
      });
  }

  protected cargarDefinicionFase(ticketId: number): void {
    this.cargandoDefinicion.set(true);
    this.errorDefinicion.set(null);

    this.ticketService
      .obtenerDefinicionFase(ticketId)
      .pipe(finalize(() => this.cargandoDefinicion.set(false)))
      .subscribe({
        next: response => {
          console.group('Definición funcional FASE');
          console.log('Ticket ID:', ticketId);
          console.log('Respuesta API:', response);
          console.log('Objetivo:', response.objetivo);
          console.log('Requerimientos:', response.requerimientos);
          console.log('Reglas:', response.reglas);
          console.log('Criterios:', response.criterios);
          console.groupEnd();
          this.definicionFase.set(response);
        },
        error: () => {
          this.errorDefinicion.set('No fue posible obtener la definición funcional.');
        },
      });
  }

  protected reintentarDefinicion(): void {
    const id = this.ticket()?.id;
    if (id) this.cargarDefinicionFase(id);
  }

  // ── Acciones modales definición FASE ──────────────────────────────────────

  // Objetivo
  protected editarObjetivo(): void {
    this.modalObjetivoTexto.set(this.definicionFase()?.objetivo ?? '');
    this.errorObjetivo.set(null);
    this.modalObjetivoOpen.set(true);
  }

  protected cerrarModalObjetivo(): void {
    if (this.guardandoObjetivo()) return;
    this.modalObjetivoOpen.set(false);
    this.errorObjetivo.set(null);
  }

  protected guardarObjetivo(): void {
    const ticketId = this.ticket()?.id;
    const objetivo = this.modalObjetivoTexto().trim();
    if (!ticketId || !objetivo || this.guardandoObjetivo()) return;

    this.guardandoObjetivo.set(true);
    this.errorObjetivo.set(null);

    this.ticketService
      .actualizarObjetivoFase(ticketId, { objetivo })
      .pipe(
        finalize(() => this.guardandoObjetivo.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: response => {
          this.definicionFase.set(response);
          this.modalObjetivoOpen.set(false);
          this.modalObjetivoTexto.set('');
          this.mostrarNotificacionExito('Objetivo guardado correctamente.');
        },
        error: err => {
          this.errorObjetivo.set(
            err?.error?.mensaje ?? 'No fue posible guardar el objetivo de la fase.',
          );
        },
      });
  }

  // Alcance
  protected editarAlcance(): void {
    const d = this.definicionFase();
    this.modalAlcanceIncluye.set(d?.incluye ?? '');
    this.modalAlcanceFuera.set(d?.fueraAlcance ?? '');
    this.errorAlcance.set(null);
    this.modalAlcanceOpen.set(true);
  }

  protected cerrarModalAlcance(): void {
    if (this.guardandoAlcance()) return;
    this.modalAlcanceOpen.set(false);
    this.errorAlcance.set(null);
  }

  protected guardarAlcance(): void {
    const ticketId = this.ticket()?.id;
    if (!ticketId || this.guardandoAlcance()) return;

    this.guardandoAlcance.set(true);
    this.errorAlcance.set(null);

    this.ticketService
      .actualizarAlcanceFase(ticketId, {
        incluye:      this.modalAlcanceIncluye().trim() || null,
        fueraAlcance: this.modalAlcanceFuera().trim() || null,
      })
      .pipe(
        finalize(() => this.guardandoAlcance.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: response => {
          this.definicionFase.set(response);
          this.modalAlcanceOpen.set(false);
          this.mostrarNotificacionExito('Alcance guardado correctamente.');
        },
        error: err => {
          this.errorAlcance.set(
            err?.error?.mensaje ?? 'No fue posible guardar el alcance.',
          );
        },
      });
  }

  // Requerimiento
  protected agregarRequerimiento(): void {
    this.modalReqCodigo.set('');
    this.modalReqTitulo.set('');
    this.modalReqPrioridad.set('');
    this.modalReqEstado.set('');
    this.errorReq.set(null);
    this.modalReqOpen.set(true);
  }

  protected cerrarModalReq(): void {
    if (this.guardandoReq()) return;
    this.modalReqOpen.set(false);
    this.errorReq.set(null);
  }

  protected guardarRequerimiento(): void {
    const ticketId = this.ticket()?.id;
    const titulo   = this.modalReqTitulo().trim();
    if (!ticketId || !titulo || this.guardandoReq()) return;

    this.guardandoReq.set(true);
    this.errorReq.set(null);

    this.ticketService
      .crearRequerimientoFase(ticketId, {
        codigo:    this.modalReqCodigo().trim(),
        titulo,
        prioridad: this.modalReqPrioridad() || null,
        estado:    this.modalReqEstado() || null,
      })
      .pipe(
        finalize(() => this.guardandoReq.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.cargarDefinicionFase(ticketId);
          this.modalReqOpen.set(false);
          this.mostrarNotificacionExito('Requerimiento creado correctamente.');
        },
        error: err => {
          this.errorReq.set(
            err?.error?.mensaje ?? 'No fue posible crear el requerimiento.',
          );
        },
      });
  }

  // Regla
  protected agregarRegla(): void {
    this.modalReglaCodigo.set('');
    this.modalReglaDesc.set('');
    this.errorRegla.set(null);
    this.modalReglaOpen.set(true);
  }

  protected cerrarModalRegla(): void {
    if (this.guardandoRegla()) return;
    this.modalReglaOpen.set(false);
    this.errorRegla.set(null);
  }

  protected guardarRegla(): void {
    const ticketId   = this.ticket()?.id;
    const descripcion = this.modalReglaDesc().trim();
    if (!ticketId || !descripcion || this.guardandoRegla()) return;

    this.guardandoRegla.set(true);
    this.errorRegla.set(null);

    this.ticketService
      .crearReglaFase(ticketId, {
        codigo: this.modalReglaCodigo().trim(),
        descripcion,
      })
      .pipe(
        finalize(() => this.guardandoRegla.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.cargarDefinicionFase(ticketId);
          this.modalReglaOpen.set(false);
          this.mostrarNotificacionExito('Regla creada correctamente.');
        },
        error: err => {
          this.errorRegla.set(
            err?.error?.mensaje ?? 'No fue posible crear la regla.',
          );
        },
      });
  }

  // Criterio
  protected agregarCriterio(): void {
    this.modalCriterioDesc.set('');
    this.errorCriterio.set(null);
    this.modalCriterioOpen.set(true);
  }

  protected cerrarModalCriterio(): void {
    if (this.guardandoCriterio()) return;
    this.modalCriterioOpen.set(false);
    this.errorCriterio.set(null);
  }

  protected guardarCriterio(): void {
    const ticketId   = this.ticket()?.id;
    const descripcion = this.modalCriterioDesc().trim();
    if (!ticketId || !descripcion || this.guardandoCriterio()) return;

    this.guardandoCriterio.set(true);
    this.errorCriterio.set(null);

    this.ticketService
      .crearCriterioFase(ticketId, { descripcion })
      .pipe(
        finalize(() => this.guardandoCriterio.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.cargarDefinicionFase(ticketId);
          this.modalCriterioOpen.set(false);
          this.mostrarNotificacionExito('Criterio creado correctamente.');
        },
        error: err => {
          this.errorCriterio.set(
            err?.error?.mensaje ?? 'No fue posible crear el criterio.',
          );
        },
      });
  }

  protected comentar(): void {
    const ticketActual = this.ticket();
    const texto = this.comentarioTexto().trim();

    if (!ticketActual || !texto || this.enviandoComentario()) return;

    this.enviandoComentario.set(true);
    this.errorComentarios.set(null);

    this.ticketService
      .crearComentario(ticketActual.id, texto)
      .pipe(
        finalize(() => this.enviandoComentario.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (comentarioCreado: TicketComentarioResponse) => {
          this.comentarioTexto.set('');
          this.comentarios.update(lista => [comentarioCreado, ...lista]);
        },
        error: (error: { status?: number }) => {
          console.error('Error al publicar comentario', error);
          if (error.status === 400) {
            this.errorComentarios.set('Escribe un comentario válido.');
          } else if (error.status === 403) {
            this.errorComentarios.set('No tienes permiso para comentar.');
          } else {
            this.errorComentarios.set('No se pudo publicar el comentario.');
          }
        },
      });
  }

  protected obtenerIniciales(usuario: UsuarioComentarioResponse): string {
    const nombre   = usuario.nombre?.trim() ?? '';
    const apellido = usuario.apellido?.trim() ?? '';
    return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase();
  }

  protected avatarColor(id: number): string {
    return AVATAR_COLORS[id % AVATAR_COLORS.length];
  }

  // ── Formateo de fechas ────────────────────────────────────────────────────
  protected formatFecha(fecha: string | null | undefined, incluirHora = false): string {
    if (!fecha) return '—';
    const d = new Date(fecha);
    if (isNaN(d.getTime())) return fecha;
    const meses = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    const base  = `${d.getDate()} ${meses[d.getMonth()]} ${d.getFullYear()}`;
    if (incluirHora && fecha.includes('T')) {
      const h = d.getHours().toString().padStart(2, '0');
      const m = d.getMinutes().toString().padStart(2, '0');
      return `${base} · ${h}:${m}`;
    }
    return base;
  }

  protected formatFechaRelativa(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    const d = new Date(fecha);
    if (isNaN(d.getTime())) return this.formatFecha(fecha, true);
    const diffMs  = Date.now() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1)  return 'Hace un momento';
    if (diffMin < 60) return `Hace ${diffMin} min`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24)   return `Hace ${diffH} h`;
    const diffD = Math.floor(diffH / 24);
    if (diffD === 1) return 'Ayer';
    if (diffD < 7)   return `Hace ${diffD} días`;
    return this.formatFecha(fecha, true);
  }
}
