import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { Flujo } from '../../../../shared/components/flujo/flujo';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ramaSugerida } from '../../../../shared/utils/flujos';
import { etiquetaEstadoTicket, ticketSinEmpezar } from '../../../../shared/utils/tickets';
import { ConfirmacionTicketModal, DatosConfirmacion } from '../../components/confirmacion-ticket/confirmacion-ticket-modal';
import { PromptIa } from '../../components/prompt-ia/prompt-ia';
import { avisoDeMovimiento, MovimientosService } from '../../data/movimientos.service';
import { TicketDetalleService } from '../../data/ticket-detalle.service';
import { TipoMovimiento } from '../../models/movimientos.models';
import { Confirmacion, DetalleTicket, OpcionTicket } from '../../models/ticket-detalle.models';
import { ArbolFase } from '../../../fases/components/arbol-fase/arbol-fase';
import { CambioVista } from '../../../../shared/components/cambio-vista/cambio-vista';
import { ComandosGit } from '../../components/comandos-git/comandos-git';

/** Estados en que alguien lo revisa (el Líder su PR o, en un hotfix, un Analista): al avanzar se pide qué incluye. */
const REVISION = ['EN_QA', 'EN_REVISION'];

/** Mientras su historia está en estos estados, sus tickets esperan a que la fase la arranque. */
const HISTORIA_SIN_ARRANCAR = ['BORRADOR', 'EN_PLANEACION', 'LISTA', 'LISTA_DESARROLLO'];
const EN_TRABAJO = ['EN_PROGRESO', 'EN_CORRECCION'];
const APROBADOS = ['APROBADA', 'APROBADO', 'APROBADA_QA'];
const INTEGRADOS = ['INTEGRADA', 'INTEGRADO'];
const TERMINADOS = ['CERRADA', 'CERRADO', 'PUBLICADA', 'PUBLICADO', 'REVISADA'];

/** Cómo se ve cada botón del panel (PDF pág. 22). */
const ESTILO: Record<TipoMovimiento, { icono: string; clase: string }> = {
  AVANZAR: { icono: 'bi-play-fill', clase: 'boton-primario' },
  RECHAZAR: { icono: 'bi-x-lg', clase: 'boton-peligro-suave' },
  BLOQUEAR: { icono: 'bi-slash-circle', clase: 'boton-secundario' },
  RETIRAR: { icono: 'bi-arrow-counterclockwise', clase: 'boton-secundario' },
  CANCELAR: { icono: 'bi-x-lg', clase: 'boton-peligro-suave' },
  NO_REPRODUCIBLE: { icono: 'bi-x-lg', clase: 'boton-secundario' },
  REABRIR: { icono: 'bi-arrow-repeat', clase: 'boton-secundario' },
  PUBLICAR_PARCHE: { icono: 'bi-rocket-takeoff', clase: 'boton-primario' },
};

/** Los movimientos que hacen avanzar el ticket: van arriba y a todo el ancho. */
const PRINCIPALES: TipoMovimiento[] = ['AVANZAR', 'PUBLICAR_PARCHE'];

/** Desde las listas, "Confirmar" y "Publicar parche" de un bug llegan con ?accion= para hacerse aquí. */
const ACCION_PEDIDA: Record<string, TipoMovimiento> = { confirmar: 'AVANZAR', parche: 'PUBLICAR_PARCHE', avanzar: 'AVANZAR' };

interface Revision {
  estado: 'ok' | 'falta' | 'info';
  texto: string;
}

/**
 * Detalle del ticket (PDF pág. 29) con su panel de Acciones según estado y rol (pág. 22)
 * y las ventanas que piden motivo o información (pág. 26).
 */
const FUERA_DEL_FLUJO = ['BLOQUEADA', 'CANCELADA', 'RECHAZADA', 'NO_REPRODUCIBLE'];

@Component({
  selector: 'app-detalle-ticket-page',
  imports: [RouterLink, ArbolFase, CambioVista, Avatar, ChipModulo, ComandosGit, ConfirmacionTicketModal, EtiquetaEstado, Flujo, PromptIa, TipoTicket],
  templateUrl: './detalle-ticket-page.html',
  styleUrl: './detalle-ticket-page.scss',
})
export class DetalleTicketPage implements OnInit {
  private readonly service = inject(TicketDetalleService);
  private readonly movimientos = inject(MovimientosService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly ticketId = input.required<string>();
  /** ?vista=arbol: su fase con historias y tickets conectados, con este elegido */
  readonly vista = input<string>();

  protected readonly ticket = signal<DetalleTicket | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal<string | null>(null);
  protected readonly confirmando = signal<{ opcion: OpcionTicket; confirmacion: Confirmacion } | null>(null);
  protected readonly comentario = signal('');
  protected readonly subtareaNueva = signal('');

  protected readonly botones = computed(() =>
    (this.ticket()?.opciones ?? []).map((o) => ({
      ...o,
      ...ESTILO[o.tipo],
      icono: o.tipo === 'AVANZAR' ? iconoDeAvance(o) : ESTILO[o.tipo].icono,
      habilitado: !o.impedimentos.length,
    })),
  );

  /** El panel se ordena por peso: lo que avanza arriba, lo alterno en una fila, Cancelar al final. */
  protected readonly principales = computed(() => this.botones().filter((b) => PRINCIPALES.includes(b.tipo)));
  protected readonly alternos = computed(() =>
    this.botones().filter((b) => !PRINCIPALES.includes(b.tipo) && b.tipo !== 'CANCELAR'));
  protected readonly cancelar = computed(() => this.botones().find((b) => b.tipo === 'CANCELAR') ?? null);

  protected readonly panel = computed(() => (this.ticket() ? panelDe(this.ticket()!) : null));
  protected readonly pasos = computed(() => this.ticket()?.flujo ?? []);
  /** Rama y PR se ven hasta que el Desarrollador inicia el ticket (o si ya tiene rama o PR). */
  protected readonly enDesarrollo = computed(() => {
    const t = this.ticket();
    return !!t && (!ticketSinEmpezar(t.tipo, t.estado) || !!t.rama || t.pullRequests.length > 0);
  });
  /** Bloqueado, cancelado, rechazado...: fuera del camino normal. */
  protected readonly fuera = computed(() => {
    const estado = this.ticket()?.estado ?? '';
    return FUERA_DEL_FLUJO.includes(estado) ? etiquetaEstadoTicket(estado) : null;
  });

  ngOnInit(): void {
    this.cargar(true);
  }

  protected presionar(opcion: OpcionTicket): void {
    if (opcion.impedimentos.length) return;
    const confirmacion = confirmacionDe(opcion, this.ticket()!);
    if (confirmacion) {
      this.confirmando.set({ opcion, confirmacion });
    } else {
      this.mover(opcion, {});
    }
  }

  protected mover(opcion: OpcionTicket, datos: DatosConfirmacion): void {
    const t = this.ticket();
    if (!t) return;
    this.enviando.set(opcion.tipo + opcion.texto);
    this.aviso.set(null);
    if (opcion.tipo === 'PUBLICAR_PARCHE') {
      this.publicarParche(t);
      return;
    }
    this.movimientos.mover({ ticketId: t.id, tipo: opcion.tipo, estadoEsperado: t.estado, ...datos }).subscribe({
      next: (resultado) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: avisoDeMovimiento(t.codigo, resultado), error: false });
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos actualizar ${t.codigo}.`), error: true });
        this.cargar();
      },
    });
  }

  private publicarParche(t: DetalleTicket): void {
    this.service.publicarParche(t.proyectoId, t.id, t.parche?.version ?? '').subscribe({
      next: (parche) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: `Se publicó el parche ${parche.version}: ya está en producción y en develop.`, error: false });
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos publicar el parche.'), error: true });
        this.cargar();
      },
    });
  }

  protected marcarSubtarea(subtareaId: number, hecha: boolean): void {
    const t = this.ticket();
    if (!t) return;
    this.enviando.set(`subtarea-${subtareaId}`);
    this.service.marcarSubtarea(t.proyectoId, t.id, subtareaId, hecha).subscribe({
      next: () => {
        this.enviando.set(null);
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar la subtarea.'), error: true });
        this.cargar();
      },
    });
  }

  protected agregarSubtarea(): void {
    const t = this.ticket();
    const descripcion = this.subtareaNueva().trim();
    if (!t || !descripcion) return;
    this.enviando.set('subtarea-nueva');
    this.service.agregarSubtarea(t.proyectoId, t.id, descripcion).subscribe({
      next: () => {
        this.enviando.set(null);
        this.subtareaNueva.set('');
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos agregar la subtarea.'), error: true });
      },
    });
  }

  protected quitarSubtarea(subtareaId: number): void {
    const t = this.ticket();
    if (!t) return;
    this.enviando.set(`subtarea-${subtareaId}`);
    this.service.quitarSubtarea(t.proyectoId, t.id, subtareaId).subscribe({
      next: () => {
        this.enviando.set(null);
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos quitar la subtarea.'), error: true });
        this.cargar();
      },
    });
  }

  protected comentar(): void {
    const t = this.ticket();
    const texto = this.comentario().trim();
    if (!t || !texto) return;
    this.enviando.set('comentario');
    this.service.comentar(t.proyectoId, t.id, texto).subscribe({
      next: () => {
        this.enviando.set(null);
        this.comentario.set('');
        this.cargar();
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar el comentario.'), error: true });
      },
    });
  }

  private cargar(primeraVez = false): void {
    this.service.detalle(Number(this.id()), Number(this.ticketId())).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.cargando.set(false);
        if (primeraVez) this.abrirAccionPedida(ticket);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar el ticket.'));
        this.cargando.set(false);
      },
    });
  }

  private abrirAccionPedida(ticket: DetalleTicket): void {
    const pedida = ACCION_PEDIDA[this.route.snapshot.queryParamMap.get('accion') ?? ''];
    if (!pedida) return;
    this.router.navigate([], { queryParams: { accion: null }, queryParamsHandling: 'merge', replaceUrl: true });
    const opcion = ticket.opciones.find((o) => o.tipo === pedida);
    if (opcion) this.presionar(opcion);
  }
}

function confirmacionDe(o: OpcionTicket, t: DetalleTicket): Confirmacion | null {
  switch (o.tipo) {
    case 'AVANZAR':
      if (t.tipo === 'BUGS' && t.estado === 'REPORTADO') return 'CONFIRMAR';
      if (t.tipo === 'MEJR' && t.estado === 'PROPUESTA') return 'APROBAR_MEJORA';
      return REVISION.includes(o.destino) ? 'ENVIAR_QA' : null;
    case 'RECHAZAR':
      return 'RECHAZAR';
    case 'BLOQUEAR':
      return 'BLOQUEAR';
    case 'RETIRAR':
      return 'RETIRAR';
    case 'CANCELAR':
      return 'CANCELAR';
    case 'NO_REPRODUCIBLE':
      return 'NO_REPRODUCIBLE';
    case 'PUBLICAR_PARCHE':
      return 'PUBLICAR_PARCHE';
    case 'REABRIR':
      return null;
  }
}

function iconoDeAvance(o: OpcionTicket): string {
  if (o.texto === 'Desbloquear') return 'bi-unlock';
  if (o.texto === 'Verificar') return 'bi-eye';
  if (REVISION.includes(o.destino)) return 'bi-send';
  if (EN_TRABAJO.includes(o.destino)) return 'bi-play-fill';
  return 'bi-check-lg';
}

type Panel = { descripcion: string; avanzaSolo: boolean; revisiones: Revision[] };

/** Un bug: lo que cambia respecto a Actividad, sobre todo si va por hotfix (PDF pág. 23). */
function panelDeBug(t: DetalleTicket, general: Panel): Panel {
  const severidad = t.severidad?.texto ?? 'sin definir';
  const motivo = t.motivoActual;
  switch (t.estado) {
    case 'REPORTADO':
      return {
        descripcion: 'Lo reportó alguien del equipo o el Analista (a partir de una solicitud del cliente).',
        avanzaSolo: false,
        revisiones: [{ estado: 'info', texto: 'Confirmar pide severidad y Desarrollador' }],
      };
    case 'CONFIRMADO':
      return {
        ...general,
        descripcion: t.hotfix
          ? `Severidad ${severidad}: va por hotfix. Al iniciar se crea la rama desde main.`
          : `Severidad ${severidad}: sigue el flujo normal desde develop y sale con la siguiente fase.`,
        revisiones: [
          ...general.revisiones.filter((r) => r.estado === 'falta'),
          ...(t.hotfix ? [{ estado: 'ok' as const, texto: `Severidad ${severidad} → ruta hotfix` }] : []),
          { estado: 'info', texto: `Rama: ${ramaSugerida(t.tipo, t.numero, t.titulo, t.hotfix)}` },
        ],
      };
    case 'EN_CORRECCION':
      return t.hotfix ? { ...general, descripcion: 'Se corrige sobre la versión de producción, no sobre develop.' } : general;
    case 'EN_QA':
      return t.hotfix
        ? {
            ...general,
            descripcion: 'Se prueba en el ambiente staging-hotfix, que tiene la versión de producción más la corrección.',
            revisiones: [
              ...general.revisiones,
              ...(t.severidad?.codigo === 'CRITICA'
                ? [{ estado: 'info' as const, texto: 'Si no hay Analista disponible, el Líder puede aprobar (queda marcado)' }]
                : []),
            ],
          }
        : general;
    case 'APROBADO':
      return t.hotfix
        ? {
            descripcion: 'Listo para salir. Puede publicarse solo o junto con otros bugs aprobados.',
            avanzaSolo: false,
            revisiones: [{ estado: 'ok', texto: 'Merge a main, tag, producción y regreso a develop en un paso' }],
          }
        : general;
    case 'PUBLICADO':
      return {
        descripcion: 'Ya está en producción. Falta confirmar que la falla desapareció de verdad.',
        avanzaSolo: false,
        revisiones: [{ estado: 'info', texto: '“Sigue fallando” lo regresa a En corrección' }],
      };
    case 'CERRADO':
      return {
        descripcion: 'Verificado en producción.',
        avanzaSolo: true,
        revisiones: [{ estado: 'ok', texto: 'develop ya tiene la corrección' }],
      };
    case 'NO_REPRODUCIBLE':
      return {
        descripcion: `No se pudo reproducir${motivo ? `: “${motivo.motivo}”` : ''}. Si vuelve a pasar, el Líder lo reabre desde el detalle.`,
        avanzaSolo: false,
        revisiones: [],
      };
    default:
      return general;
  }
}

function panelDe(t: DetalleTicket): Panel {
  const general = panelGeneral(t);
  if (t.tipo === 'BUGS') return panelDeBug(t, general);
  return panelDeOtroTipo(t, general) ?? general;
}

/** Los pasos propios de una Mejora (PDF pág. 30). */
function panelDeOtroTipo(t: DetalleTicket, general: Panel): Panel | null {
  switch (`${t.tipo}:${t.estado}`) {
    case 'MEJR:PROPUESTA':
      return {
        descripcion: 'Un cambio a algo que ya existe. El Líder decide si entra en la fase, para que no entren cambios sin control.',
        avanzaSolo: false,
        revisiones: [
          { estado: 'info', texto: 'Aprobar pide el Desarrollador que la hará' },
          { estado: 'info', texto: 'Rechazar pide motivo y queda en el historial' },
        ],
      };
    case 'MEJR:APROBADA':
      return { ...general, descripcion: 'Aprobada y asignada. Desde aquí sigue igual que una Actividad.' };
    case 'MEJR:RECHAZADA':
      return {
        descripcion: `Rechazada${t.motivoActual?.usuario ? ` por ${t.motivoActual.usuario}` : ''}${t.motivoActual ? `: “${t.motivoActual.motivo}”` : ''}.`,
        avanzaSolo: false,
        revisiones: [],
      };
    default:
      return null;
  }
}

/** Lo que dice el panel en cada estado y lo que revisa abajo (PDF pág. 22). */
function panelGeneral(t: DetalleTicket): Panel {
  const impedimentos: Revision[] = t.opciones.flatMap((o) => o.impedimentos.map((texto) => ({ estado: 'falta' as const, texto })));
  const pr = t.pullRequests[0];
  const subtareas: Revision[] = t.subtareas.length
    ? [{
        estado: t.subtareasHechas === t.subtareas.length ? 'ok' : 'info',
        texto: `Subtareas ${t.subtareasHechas} de ${t.subtareas.length} (aviso, no bloquea)`,
      }]
    : [];
  const motivo = t.motivoActual;

  if (t.estado === 'BLOQUEADA') {
    const dias = motivo?.fecha ? Math.floor((Date.now() - motivo.fecha.getTime()) / 86_400_000) : null;
    return {
      descripcion: `Bloqueada${motivo?.usuario ? ` por ${motivo.usuario}` : ''}${motivo ? `: “${motivo.motivo}”` : ''}.`,
      avanzaSolo: false,
      revisiones: [
        ...(t.bloqueadoPor ? [{ estado: 'info' as const, texto: `Se desbloquea solo cuando ${t.bloqueadoPor} se termine` }] : []),
        ...(dias !== null
          ? [{ estado: 'info' as const, texto: `Lleva ${dias} ${dias === 1 ? 'día' : 'días'} bloqueada · el Líder fue avisado` }]
          : []),
      ],
    };
  }
  if (t.estado === 'CANCELADA') {
    return {
      descripcion: `Cancelada${motivo?.usuario ? ` por ${motivo.usuario}` : ''}${motivo ? `: “${motivo.motivo}”` : ''}.`,
      avanzaSolo: false,
      revisiones: t.criterios.filter((c) => c.soloEste)
        .map((c) => ({ estado: 'falta' as const, texto: `Criterio “${c.descripcion}” quedó sin cubrir` })),
    };
  }
  if (EN_TRABAJO.includes(t.estado)) {
    return {
      descripcion: t.meToca
        ? t.opciones.some((o) => o.destino === 'EN_QA')
          ? `Cuando termines, envíalo a QA${t.tester ? `: le llegará a ${t.tester.nombre} (Analista)` : ': le llegará a un Analista del equipo'}.`
          : 'Cuando termines, envía tu PR: lo revisa el Líder. Después su Analista prueba la historia completa.'
        : 'Se está trabajando. Cuando termine, envía su PR al Líder.',
      avanzaSolo: false,
      revisiones: [
        ...(t.rama ? [{ estado: 'ok' as const, texto: `Rama ${t.rama}` }] : []),
        ...(pr ? [{ estado: pr.ci.tono === 'verde' ? 'ok' as const : 'falta' as const, texto: `PR #${pr.numero} · ${pr.ci.texto}` }] : []),
        ...subtareas,
      ],
    };
  }
  if (REVISION.includes(t.estado)) {
    const propio = t.tester && t.responsable && t.tester.id === t.responsable.id;
    return {
      descripcion: 'Se prueba en staging o por Swagger. Quien lo hizo aún puede retirarlo si no lo han empezado a revisar.',
      avanzaSolo: false,
      revisiones: [
        ...impedimentos,
        ...(pr ? [{ estado: 'ok' as const, texto: `PR #${pr.numero} desplegado en staging de prueba` }] : []),
        ...(t.tester ? [{ estado: propio ? 'falta' as const : 'ok' as const, texto: propio ? 'Es su propio ticket' : 'No es su propio ticket' }] : []),
      ],
    };
  }
  if (APROBADOS.includes(t.estado)) {
    return {
      descripcion: t.historia ? 'Funciona bien. Espera a que su historia se acepte e integre.' : 'Funciona bien.',
      avanzaSolo: t.opciones.every((o) => o.tipo === 'CANCELAR'),
      revisiones: t.historia ? [{ estado: 'info', texto: `${t.historia.codigo} ${t.historia.estadoTexto.toLowerCase()}` }] : [],
    };
  }
  if (INTEGRADOS.includes(t.estado)) {
    return {
      descripcion: 'Su PR ya entró a develop y está en staging.',
      avanzaSolo: true,
      revisiones: [{ estado: 'info', texto: 'Esperando el release de la fase' }],
    };
  }
  if (TERMINADOS.includes(t.estado)) {
    return { descripcion: 'Terminado.', avanzaSolo: true, revisiones: [{ estado: 'ok', texto: 'En producción' }] };
  }
  // Actividad en Borrador: el Líder la termina de definir y la marca Lista.
  if (t.estado === 'BORRADOR') {
    return {
      descripcion: t.meToca
        ? 'Borrador. Termínala de definir (edítala, agrégale subtareas) y márcala como Lista.'
        : 'Borrador. El Líder la termina de definir y la marca como Lista; después la inicia su Desarrollador.',
      avanzaSolo: false,
      revisiones: [{ estado: 'info', texto: 'Con su primera Actividad Lista, la historia queda lista para desarrollo' }],
    };
  }
  // Su historia aún no arranca: el ticket ya está listo, pero nadie lo puede iniciar hasta que inicie la fase.
  const historia = t.historia;
  if (historia && HISTORIA_SIN_ARRANCAR.includes(historia.estado)) {
    return {
      descripcion: `Lista. Se podrá iniciar cuando arranque su historia ${historia.codigo}, al iniciarse la fase.`,
      avanzaSolo: false,
      revisiones: [
        historia.estado === 'LISTA_DESARROLLO'
          ? { estado: 'falta', texto: 'Falta que el Líder inicie la fase' }
          : { estado: 'falta', texto: `${historia.codigo} todavía se está planeando` },
        { estado: 'info', texto: 'Mientras tanto se puede editar y agregarle subtareas' },
      ],
    };
  }
  // Por hacer, Reportado, Confirmado, Propuesta...: el inicio del flujo.
  return {
    descripcion: t.meToca
      ? `Listo para trabajarse. Al iniciar, crea la rama ${ramaSugerida(t.tipo, t.numero, t.titulo, t.hotfix)} desde ${t.hotfix ? 'main' : (t.modulo?.ramaBase ?? 'develop')} (los comandos están en Desarrollo).`
      : t.actor === 'Nadie' ? 'Avanza solo.' : `Le toca al ${t.actor}.`,
    avanzaSolo: false,
    revisiones: [
      ...impedimentos,
      ...t.dependencias.map((d) => ({
        estado: ['verde', 'violeta'].includes(d.etiqueta.tono) ? 'ok' as const : 'falta' as const,
        texto: `${d.codigo} (dependencia) ${d.etiqueta.texto.toLowerCase()}`,
      })),
      { estado: 'info', texto: 'El Líder puede Cancelar desde el detalle' },
    ],
  };
}

