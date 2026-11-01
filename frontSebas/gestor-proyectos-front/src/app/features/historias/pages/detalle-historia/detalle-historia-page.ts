import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Modal } from '../../../../shared/components/modal/modal';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { Flujo } from '../../../../shared/components/flujo/flujo';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { FLUJO_HISTORIA, pasosDe } from '../../../../shared/utils/flujos';
import { etiquetaHistoria } from '../../../../shared/utils/tickets';
import { ConfirmacionHistoriaModal } from '../../components/confirmacion-historia/confirmacion-historia-modal';
import { ImagenesHistoria } from '../../components/imagenes-historia/imagenes-historia';
import { HistoriaDetalleService } from '../../data/historia-detalle.service';
import {
  ACCIONES_CON_CONFIRMACION,
  AccionConConfirmacion,
  AccionDisponible,
  AccionHistoria,
  DatosAccionHistoria,
  DetalleHistoria,
} from '../../models/historia-detalle.models';
import { ArbolFase } from '../../../fases/components/arbol-fase/arbol-fase';
import { CambioVista } from '../../../../shared/components/cambio-vista/cambio-vista';

/** Cómo se ve cada botón del panel de Acciones (PDF pág. 21). */
const BOTONES: Record<AccionHistoria, { icono: string; clase: string }> = {
  INICIAR_PLANEACION: { icono: 'bi-play-fill', clase: 'boton-primario' },
  MARCAR_LISTA: { icono: 'bi-flag', clase: 'boton-primario' },
  REGRESAR_A_PLANEACION: { icono: 'bi-arrow-left', clase: 'boton-secundario' },
  DESCARTAR: { icono: 'bi-x-lg', clase: 'boton-peligro-suave' },
  MOVER_DE_FASE: { icono: 'bi-arrow-right', clase: 'boton-secundario' },
  ACEPTAR: { icono: 'bi-check-lg', clase: 'boton-primario' },
  DEVOLVER: { icono: 'bi-arrow-counterclockwise', clase: 'boton-peligro-suave' },
  INTEGRAR: { icono: 'bi-sign-merge-right', clase: 'boton-primario' },
  REABRIR: { icono: 'bi-arrow-repeat', clase: 'boton-secundario' },
};

/** La decisión principal primero, como en el PDF. */
const ORDEN: AccionHistoria[] = ['INICIAR_PLANEACION', 'MARCAR_LISTA', 'ACEPTAR', 'INTEGRAR', 'REABRIR',
  'MOVER_DE_FASE', 'REGRESAR_A_PLANEACION', 'DEVOLVER', 'DESCARTAR'];

/** Los estados que avanzan solos: el panel lo dice en lugar de mostrar botones. */
const AVANZA_SOLA = ['EN_DESARROLLO', 'INTEGRADA', 'CERRADA'];

interface Revision {
  estado: 'ok' | 'falta' | 'info';
  texto: string;
}

/**
 * Detalle de la historia (PDF pág. 24) con su panel de Acciones según el estado (pág. 21)
 * y las ventanas de confirmación (pág. 33).
 */
@Component({
  selector: 'app-detalle-historia-page',
  imports: [RouterLink, ArbolFase, CambioVista, Avatar, ChipModulo, ConfirmacionHistoriaModal, EtiquetaEstado, Flujo, ImagenesHistoria, Modal, TipoTicket],
  templateUrl: './detalle-historia-page.html',
  styleUrl: './detalle-historia-page.scss',
})
export class DetalleHistoriaPage implements OnInit {
  private readonly service = inject(HistoriaDetalleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly historiaId = input.required<string>();
  /** ?vista=arbol: su fase con historias y tickets conectados, con este elegido */
  readonly vista = input<string>();

  protected readonly historia = signal<DetalleHistoria | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  /** La acción, criterio o comentario que se está enviando, para deshabilitar los botones. */
  protected readonly enviando = signal<string | null>(null);
  protected readonly confirmando = signal<AccionConConfirmacion | null>(null);
  /** La ventana para confirmar Eliminar está abierta. */
  protected readonly eliminando = signal(false);
  protected readonly comentario = signal('');

  protected readonly avanzaSola = computed(() => AVANZA_SOLA.includes(this.historia()?.estado ?? ''));

  protected readonly botones = computed(() =>
    [...(this.historia()?.acciones ?? [])]
      .sort((a, b) => ORDEN.indexOf(a.accion) - ORDEN.indexOf(b.accion))
      .map((a) => ({ ...a, ...BOTONES[a.accion], habilitado: a.accion === 'INTEGRAR' || !a.impedimentos.length })),
  );

  protected readonly panel = computed(() => (this.historia() ? panelDe(this.historia()!) : null));
  protected readonly pasos = computed(() => pasosDe(FLUJO_HISTORIA, this.historia()?.estado ?? ''));
  protected readonly fuera = computed(() =>
    this.historia()?.estado === 'DESCARTADA' ? etiquetaHistoria('DESCARTADA') : null,
  );

  ngOnInit(): void {
    // Al volver de Importar tickets llega cuántos se crearon para avisarlo arriba.
    const creados = history.state?.ticketsImportados as number | undefined;
    if (creados) {
      this.aviso.set({
        texto: creados === 1 ? 'Se creó 1 ticket desde JSON.' : `Se crearon ${creados} tickets desde JSON.`,
        error: false,
      });
    }
    this.cargar(true);
  }

  protected presionar(boton: AccionDisponible): void {
    if (boton.accion === 'INTEGRAR') {
      // Integrar tiene su propia vista con la revisión previa (PDF pág. 28); ahí se ve qué falta.
      const h = this.historia()!;
      this.router.navigate(['/proyectos', h.proyectoId, 'historias', h.id, 'integrar']);
      return;
    }
    if (boton.impedimentos.length) return;
    if (ACCIONES_CON_CONFIRMACION.includes(boton.accion)) {
      this.confirmando.set(boton.accion as AccionConConfirmacion);
    } else {
      this.ejecutar(boton.accion);
    }
  }

  protected ejecutar(accion: AccionHistoria, datos: DatosAccionHistoria = {}): void {
    const h = this.historia();
    if (!h) return;
    this.enviando.set(accion);
    this.aviso.set(null);
    this.service.ejecutar(h.proyectoId, h.id, accion, h.estado, datos).subscribe({
      next: (r) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        const bug = r.bugCreado ? ` Se creó ${r.bugCreado} dentro de la historia.` : '';
        this.aviso.set({ texto: `${h.codigo}: ${r.accion}.${bug}`, error: false });
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos actualizar ${h.codigo}.`), error: true });
        this.cargar(false);
      },
    });
  }

  protected eliminar(): void {
    const h = this.historia();
    if (!h) return;
    this.enviando.set('eliminar');
    this.service.eliminar(h.proyectoId, h.id).subscribe({
      next: () => this.router.navigate(['/proyectos', h.proyectoId, 'fases', h.faseId]),
      error: (e) => {
        this.enviando.set(null);
        this.eliminando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos eliminar ${h.codigo}.`), error: true });
        this.cargar(false);
      },
    });
  }

  protected verificar(criterioId: number, verificado: boolean): void {
    const h = this.historia();
    if (!h) return;
    this.enviando.set(`criterio-${criterioId}`);
    this.service.verificarCriterio(h.proyectoId, h.id, criterioId, verificado).subscribe({
      next: () => {
        this.enviando.set(null);
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar el criterio.'), error: true });
        this.cargar(false);
      },
    });
  }

  protected comentar(): void {
    const h = this.historia();
    const texto = this.comentario().trim();
    if (!h || !texto) return;
    this.enviando.set('comentario');
    this.service.comentar(h.proyectoId, h.id, texto).subscribe({
      next: () => {
        this.enviando.set(null);
        this.comentario.set('');
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar el comentario.'), error: true });
      },
    });
  }

  /** Se subió o quitó una imagen. */
  protected recargar(): void {
    this.cargar(false);
  }

  private cargar(primeraVez: boolean): void {
    this.service.detalle(Number(this.id()), Number(this.historiaId())).subscribe({
      next: (historia) => {
        this.historia.set(historia);
        this.cargando.set(false);
        if (primeraVez) this.abrirAccionPedida(historia);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar la historia.'));
        this.cargando.set(false);
      },
    });
  }

  /** Desde las listas, el botón llega con ?accion=INICIAR para confirmarse aquí. */
  private abrirAccionPedida(historia: DetalleHistoria): void {
    const pedida = this.route.snapshot.queryParamMap.get('accion') as AccionHistoria | null;
    if (!pedida) return;
    this.router.navigate([], { queryParams: { accion: null }, queryParamsHandling: 'merge', replaceUrl: true });
    const boton = historia.acciones.find((a) => a.accion === pedida);
    if (boton) this.presionar(boton);
  }
}

/** Lo que dice el panel en cada estado y lo que revisa abajo (PDF pág. 21). */
function panelDe(h: DetalleHistoria): { descripcion: string; revisiones: Revision[] } {
  const impedimentos = (accion: AccionHistoria): Revision[] =>
    (h.acciones.find((a) => a.accion === accion)?.impedimentos ?? []).map((texto) => ({ estado: 'falta', texto }));
  const criterios = h.criterios.length;

  switch (h.estado) {
    case 'BORRADOR':
      return {
        descripcion: 'Tiene lo básico. Al iniciar la planeación se edita completa: Como / Quiero / Para, criterios, repositorios y diseño.',
        revisiones: [{ estado: 'info', texto: 'Iniciar planeación la deja lista para completarse' }],
      };
    case 'EN_PLANEACION':
      return {
        descripcion: 'Se está definiendo: con Editar se capturan Como / Quiero / Para, criterios, repositorios y diseño. Cuando tenga todo, se marca como Lista.',
        revisiones: [
          h.como && h.quiero && h.para
            ? { estado: 'ok', texto: 'Como / Quiero / Para completo' }
            : { estado: 'falta', texto: 'Falta completar Como / Quiero / Para' },
          criterios
            ? { estado: 'ok', texto: `${criterios} ${criterios === 1 ? 'criterio' : 'criterios'} de aceptación` }
            : { estado: 'falta', texto: 'Sin criterios de aceptación' },
          h.modulos.length
            ? { estado: 'ok', texto: `${h.modulos.length} ${h.modulos.length === 1 ? 'repositorio' : 'repositorios'}` }
            : { estado: 'falta', texto: 'Sin repositorios elegidos' },
        ],
      };
    case 'LISTA': {
      // El Líder crea sus Actividades (nacen en Borrador) y las marca Listas; con la primera Lista,
      // la historia pasa sola a Lista para desarrollo.
      const actividades = h.tickets.filter((t) => t.tipo === 'ACTV');
      const enBorrador = actividades.filter((t) => t.estadoCodigo === 'BORRADOR').length;
      return {
        descripcion: h.puedeCrearTicket
          ? 'Ya está definida. Te toca crear sus Actividades y marcarlas Listas: con la primera Lista queda lista para desarrollo.'
          : 'Ya está definida. El Líder crea sus Actividades y las marca Listas.',
        revisiones: [
          actividades.length
            ? { estado: 'ok', texto: `${actividades.length} ${actividades.length === 1 ? 'Actividad creada' : 'Actividades creadas'}` }
            : { estado: 'falta', texto: h.puedeCrearTicket ? 'Falta crear sus Actividades: te toca a ti' : 'Falta crear sus Actividades' },
          ...(enBorrador
            ? [{ estado: 'falta' as const, texto: `${enBorrador} en Borrador: márcalas Listas desde cada Actividad` }]
            : []),
          { estado: 'info', texto: 'Se puede regresar a planeación desde aquí' },
        ],
      };
    }
    case 'LISTA_DESARROLLO':
      return {
        descripcion: 'Lista para desarrollo. Arranca sola cuando el Líder inicie la fase.',
        revisiones: [
          { estado: 'ok', texto: `${h.tickets.length} ${h.tickets.length === 1 ? 'ticket' : 'tickets'} con su Desarrollador` },
          { estado: 'info', texto: `Espera a que inicie la ${h.faseTitulo.split(' · ')[0]}` },
        ],
      };
    case 'EN_DESARROLLO':
      return {
        descripcion: 'Se trabaja en sus tickets. Pasa sola a QA cuando todos sus PR estén aprobados.',
        // Aprobado o más adelante se ve en violeta o verde (etiquetaEstadoTicket).
        revisiones: h.tickets.map((t) => ({
          estado: ['violeta', 'verde'].includes(t.estado.tono) ? 'ok' : 'info',
          texto: `${t.codigo} · ${t.estado.texto}`,
        })),
      };
    case 'EN_VALIDACION':
      return {
        descripcion: h.meToca
          ? 'Todos sus PR están aprobados. Pruébala completa en staging, revisa sus criterios y acéptala o devuélvela.'
          : 'Todos sus PR están aprobados. Su Analista la prueba completa contra lo que pidió el cliente.',
        revisiones: [
          { estado: 'ok', texto: `Tickets aprobados: ${h.ticketsAprobados} de ${h.ticketsQueCuentan}` },
          {
            estado: h.criteriosVerificados === criterios ? 'ok' : 'info',
            texto: `Criterios verificados: ${h.criteriosVerificados} de ${criterios}`,
          },
          { estado: 'info', texto: 'Devolver pide el motivo y reabre un ticket o crea un bug' },
        ],
      };
    case 'ACEPTADA':
      return {
        descripcion: 'Cumple lo que pidió el cliente. Falta meter su código a develop.',
        revisiones: [
          { estado: 'ok', texto: `Tickets aprobados: ${h.ticketsAprobados} de ${h.ticketsQueCuentan}` },
          { estado: 'info', texto: 'Al integrar, sus tickets pasan a Integrada' },
        ],
      };
    case 'INTEGRADA':
      return {
        descripcion: `Su código ya está en develop y staging. Se cierra sola al publicar la ${h.faseTitulo}.`,
        revisiones: [{ estado: 'info', texto: 'Esperando el release de la fase' }],
      };
    case 'CERRADA':
      return {
        descripcion: 'Publicada en producción. Si la fase se revierte, vuelve a Integrada.',
        revisiones: [{ estado: 'ok', texto: 'En producción' }],
      };
    case 'DESCARTADA':
      return {
        descripcion: h.motivoActual
          ? `Descartada${h.motivoActual.usuario ? ` por ${h.motivoActual.usuario}` : ''}: “${h.motivoActual.motivo}”.`
          : 'Descartada.',
        revisiones: [
          ...impedimentos('REABRIR'),
          { estado: 'info', texto: 'Al reabrirla vuelve a Borrador' },
        ],
      };
    default:
      return { descripcion: '', revisiones: [] };
  }
}
