import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Modal } from '../../../../shared/components/modal/modal';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { Flujo } from '../../../../shared/components/flujo/flujo';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { FLUJO_FASE, pasosDe } from '../../../../shared/utils/flujos';
import { diaYMes, fechaCorta } from '../../../../shared/utils/fechas';
import { CambioVista } from '../../../../shared/components/cambio-vista/cambio-vista';
import { ArbolFase } from '../../components/arbol-fase/arbol-fase';
import { ConfirmacionFaseModal } from '../../components/confirmacion-fase/confirmacion-fase-modal';
import { HistoriaDetalleService } from '../../../historias/data/historia-detalle.service';
import { FaseDetalleService } from '../../data/fase-detalle.service';
import {
  ACCIONES_CON_CONFIRMACION,
  AccionConConfirmacion,
  AccionDisponible,
  AccionFase,
  Criterio,
  DatosAccion,
  DetalleFase,
  HistoriaDeFase,
} from '../../models/fase-detalle.models';

/** Cómo se ve cada botón del panel de Acciones (PDF pág. 20). */
const BOTONES: Record<AccionFase, { icono: string; clase: string }> = {
  INICIAR: { icono: 'bi-play-fill', clase: 'boton-primario' },
  PAUSAR: { icono: 'bi-pause-fill', clase: 'boton-secundario' },
  REANUDAR: { icono: 'bi-play-fill', clase: 'boton-primario' },
  CANCELAR: { icono: 'bi-x-lg', clase: 'boton-peligro-suave' },
  CERRAR: { icono: 'bi-check2-circle', clase: 'boton-primario' },
  SUBIR_A_MAIN: { icono: 'bi-sign-merge-right', clase: 'boton-primario' },
  DESPLEGAR: { icono: 'bi-rocket-takeoff', clase: 'boton-primario' },
  REVERTIR: { icono: 'bi-arrow-counterclockwise', clase: 'boton-peligro-suave' },
  PUBLICAR_DE_NUEVO: { icono: 'bi-arrow-repeat', clase: 'boton-primario' },
  REABRIR: { icono: 'bi-arrow-repeat', clase: 'boton-secundario' },
};

/** En En curso, el botón de cerrar va primero (PDF pág. 32). */
const ORDEN: AccionFase[] = ['CERRAR', 'INICIAR', 'REANUDAR', 'SUBIR_A_MAIN', 'DESPLEGAR', 'PUBLICAR_DE_NUEVO',
  'REABRIR', 'PAUSAR', 'REVERTIR', 'CANCELAR'];

/** Cada renglón con palomita de abajo del panel: cumplido, pendiente o solo informativo. */
interface Revision {
  estado: 'ok' | 'falta' | 'info';
  texto: string;
}

/** Los estados en que la fase recibe historias nuevas (igual que en el back). */
const ACEPTA_HISTORIAS = ['PLANEADA', 'EN_CURSO', 'PAUSADA'];

/**
 * Detalle de la fase (PDF pág. 32) con su panel de Acciones según el estado (pág. 20)
 * y las ventanas de confirmación de las acciones delicadas (pág. 25).
 */
@Component({
  selector: 'app-detalle-fase-page',
  imports: [RouterLink, ArbolFase, CambioVista, Avatar, BarraAvance, ChipModulo, ConfirmacionFaseModal, EtiquetaEstado, Flujo, Modal, TipoTicket],
  templateUrl: './detalle-fase-page.html',
  styleUrl: './detalle-fase-page.scss',
})
export class DetalleFasePage implements OnInit {
  private readonly service = inject(FaseDetalleService);
  private readonly historias = inject(HistoriaDetalleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  readonly faseId = input.required<string>();
  /** ?vista=arbol: la fase con sus historias y tickets conectados */
  readonly vista = input<string>();

  protected readonly fase = signal<DetalleFase | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  /** La acción (o el criterio) que se está enviando, para deshabilitar los botones. */
  protected readonly enviando = signal<string | null>(null);
  protected readonly confirmando = signal<AccionConConfirmacion | null>(null);
  /** La ventana para confirmar Eliminar fase está abierta. */
  protected readonly eliminando = signal(false);
  /** La historia de la lista que se va a eliminar; null si la ventana está cerrada. */
  protected readonly historiaAEliminar = signal<HistoriaDeFase | null>(null);
  /** El enlace de Figma que se está escribiendo; null si no se está editando. */
  protected readonly editandoFigma = signal<string | null>(null);

  protected readonly fechaCorta = fechaCorta;

  protected readonly encabezado = computed(() => {
    const f = this.fase();
    if (!f) return '';
    const partes = [f.proyectoNombre];
    if (f.fechaInicio || f.fechaFin) {
      partes.push(`${diaYMes(f.fechaInicio) || '…'} – ${fechaCorta(f.fechaFin) || '…'}`);
    }
    partes.push(`Responsable: ${f.responsableNombre}`);
    if (f.versionRelease) partes.push(`Release ${f.versionRelease}`);
    return partes.join(' · ');
  });

  protected readonly aceptaHistorias = computed(() => {
    const f = this.fase();
    return !!f && !f.mantenimiento && f.puedeCrearHistoria && ACEPTA_HISTORIAS.includes(f.estado);
  });


  /** "Se borran la fase y sus 9 historias (con sus criterios, imágenes y comentarios)." */
  protected readonly textoEliminar = computed(() => {
    const n = this.fase()?.historias.length ?? 0;
    if (!n) return 'Se borra la fase.';
    return `Se borran la fase y ${n === 1 ? 'su historia' : `sus ${n} historias`} (con sus criterios, imágenes y comentarios).`;
  });

  /** Las que aún se planean y no se pueden marcar Lista: llevan punto ámbar. */
  protected readonly hayIncompletas = computed(() =>
    (this.fase()?.historias ?? []).some((h) => h.incompleta && this.planeandose(h.estado)));

  protected planeandose(estado: string): boolean {
    return ['BORRADOR', 'EN_PLANEACION'].includes(estado);
  }

  /** Los botones del panel en el orden del PDF, con su ícono y estilo. */
  protected readonly botones = computed(() => {
    const acciones = this.fase()?.acciones ?? [];
    return [...acciones]
      .sort((a, b) => ORDEN.indexOf(a.accion) - ORDEN.indexOf(b.accion))
      .map((a) => ({ ...a, ...BOTONES[a.accion], habilitado: !a.impedimentos.length }));
  });

  /** El panel se ordena por peso: lo que avanza arriba, lo alterno en lista, Cancelar al final. */
  protected readonly principales = computed(() => this.botones().filter((b) => b.clase === 'boton-primario'));
  protected readonly alternos = computed(() =>
    this.botones().filter((b) => b.clase !== 'boton-primario' && b.accion !== 'CANCELAR'));
  protected readonly cancelar = computed(() => this.botones().find((b) => b.accion === 'CANCELAR') ?? null);

  protected readonly panel = computed(() => (this.fase() ? panelDe(this.fase()!) : null));
  /** Mantenimiento no tiene pasos: es permanente. Pausada se ve en En curso; Revertida, en Publicada. */
  protected readonly pasos = computed(() => {
    const f = this.fase();
    return !f || f.mantenimiento ? [] : pasosDe(FLUJO_FASE, f.estado, { PAUSADA: 'EN_CURSO', REVERTIDA: 'PUBLICADA' });
  });
  protected readonly fuera = computed(() => {
    const f = this.fase();
    return f && ['PAUSADA', 'REVERTIDA', 'CANCELADA'].includes(f.estado) ? f.etiqueta : null;
  });

  ngOnInit(): void {
    // Al volver de Importar historias llega el resultado para avisarlo arriba.
    const importacion = history.state?.importacion as { creadas: number; incompletas: number } | undefined;
    if (importacion) this.aviso.set({ texto: textoImportacion(importacion), error: false });
    this.cargar(true);
  }

  protected eliminar(): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set('eliminar');
    this.service.eliminar(f.proyectoId, f.id).subscribe({
      next: () => this.router.navigate(['/proyectos', f.proyectoId, 'fases']),
      error: (e) => {
        this.enviando.set(null);
        this.eliminando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos eliminar la ${f.titulo}.`), error: true });
        this.cargar(false);
      },
    });
  }

  protected eliminarHistoria(h: HistoriaDeFase): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set('historia');
    this.historias.eliminar(f.proyectoId, h.id).subscribe({
      next: () => {
        this.enviando.set(null);
        this.historiaAEliminar.set(null);
        this.aviso.set({ texto: `Se eliminó ${h.codigo} · ${h.titulo}.`, error: false });
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.historiaAEliminar.set(null);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos eliminar ${h.codigo}.`), error: true });
        this.cargar(false);
      },
    });
  }

  protected guardarFigma(): void {
    const url = this.editandoFigma()?.trim() ?? '';
    this.guardarDiseno(url || null, this.fase()?.diseno.paraTodas ?? true, () => this.editandoFigma.set(null));
  }

  protected cambiarParaTodas(paraTodas: boolean): void {
    this.guardarDiseno(this.fase()?.diseno.figmaUrl ?? null, paraTodas);
  }

  private guardarDiseno(figmaUrl: string | null, paraTodas: boolean, alTerminar?: () => void): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set('diseno');
    this.aviso.set(null);
    this.service.guardarDiseno(f.proyectoId, f.id, figmaUrl, paraTodas).subscribe({
      next: () => {
        this.enviando.set(null);
        alTerminar?.();
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar el diseño de la fase.'), error: true });
      },
    });
  }

  protected presionar(boton: AccionDisponible): void {
    if (boton.impedimentos.length) return;
    if (ACCIONES_CON_CONFIRMACION.includes(boton.accion)) {
      this.confirmando.set(boton.accion as AccionConConfirmacion);
    } else {
      this.ejecutar(boton.accion);
    }
  }

  protected ejecutar(accion: AccionFase, datos: DatosAccion = {}): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set(accion);
    this.aviso.set(null);
    this.service.ejecutar(f.proyectoId, f.id, accion, f.estado, datos).subscribe({
      next: (r) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        const bug = r.bugCreado ? ` Se creó ${r.bugCreado} y quedó asignado.` : '';
        this.aviso.set({ texto: `${f.titulo}: ${r.accion}.${bug}`, error: false });
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.confirmando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, `No pudimos actualizar la ${f.titulo}.`), error: true });
        this.cargar(false);
      },
    });
  }

  protected marcar(criterio: Criterio): void {
    const f = this.fase();
    if (!f) return;
    this.enviando.set(`criterio-${criterio.id}`);
    this.aviso.set(null);
    this.service.marcarCriterio(f.proyectoId, f.id, criterio.id).subscribe({
      next: (r) => {
        this.enviando.set(null);
        const lista = r.estadoNuevo === 'LISTA_PARA_RELEASE' ? ' Se cumplieron todos: la fase pasó a Lista para release.' : '';
        this.aviso.set({ texto: `"${criterio.descripcion}" quedó cumplido.${lista}`, error: false });
        this.cargar(false);
      },
      error: (e) => {
        this.enviando.set(null);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos marcar el criterio.'), error: true });
        this.cargar(false);
      },
    });
  }

  private cargar(primeraVez: boolean): void {
    this.service.detalle(Number(this.id()), Number(this.faseId())).subscribe({
      next: (fase) => {
        this.fase.set(fase);
        this.cargando.set(false);
        if (primeraVez) this.abrirConfirmacionPedida(fase);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar la fase.'));
        this.cargando.set(false);
      },
    });
  }

  /** Desde el árbol, "Subir a main" llega con ?accion=SUBIR_A_MAIN para abrir su confirmación aquí. */
  private abrirConfirmacionPedida(fase: DetalleFase): void {
    const pedida = this.route.snapshot.queryParamMap.get('accion') as AccionFase | null;
    if (!pedida) return;
    this.router.navigate([], { queryParams: { accion: null }, queryParamsHandling: 'merge', replaceUrl: true });
    const boton = fase.acciones.find((a) => a.accion === pedida);
    if (boton) this.presionar(boton);
  }
}

/** "Se crearon 3 historias desde JSON. 2 necesitan completar información (punto ámbar)." */
function textoImportacion({ creadas, incompletas }: { creadas: number; incompletas: number }): string {
  const hechas = creadas === 1 ? 'Se creó 1 historia desde JSON.' : `Se crearon ${creadas} historias desde JSON.`;
  if (!incompletas) return hechas;
  return `${hechas} ${incompletas === 1 ? '1 necesita' : `${incompletas} necesitan`} completar información (punto ámbar).`;
}

/** Lo que dice el panel en cada estado y lo que revisa abajo (PDF pág. 20). */
function panelDe(f: DetalleFase): { descripcion: string; revisiones: Revision[] } {
  const desde = f.enEstadoDesde;
  const quien = f.cambioEstadoPor ? ` por ${f.cambioEstadoPor}` : '';
  const motivo = f.motivoEstado ? `: “${f.motivoEstado}”` : '';
  const impedimentos = (accion: AccionFase): Revision[] =>
    (f.acciones.find((a) => a.accion === accion)?.impedimentos ?? []).map((texto) => ({ estado: 'falta', texto }));

  if (f.mantenimiento) {
    return { descripcion: 'Fase permanente para corregir bugs de producción. Nunca se cierra.', revisiones: [] };
  }

  switch (f.estado) {
    case 'PLANEADA': {
      // Arrancan las que ya tienen sus Actividades; las demás, al terminar de planearse.
      const listas = f.historias.filter((h) => h.estado === 'LISTA_DESARROLLO').length;
      const planeandose = f.historias.filter((h) => ['BORRADOR', 'EN_PLANEACION', 'LISTA'].includes(h.estado)).length;
      return {
        descripcion: 'Aún no empieza. Al iniciarla, sus historias Listas para desarrollo arrancan; las demás arrancan solas al terminar sus Actividades.',
        revisiones: [
          ...impedimentos('INICIAR'),
          ...(listas
            ? [{ estado: 'ok' as const, texto: `${listas} ${listas === 1 ? 'historia lista' : 'historias listas'} para desarrollo` }]
            : []),
          ...(planeandose
            ? [{ estado: 'info' as const, texto: `${planeandose} ${planeandose === 1 ? 'historia' : 'historias'} todavía planeándose` }]
            : []),
          ...(f.fechaInicio ? [{ estado: 'info' as const, texto: `Inicio programado: ${diaYMes(f.fechaInicio)}` }] : []),
        ],
      };
    }
    case 'EN_CURSO': {
      const faltan = f.criterios.length - f.criteriosCumplidos;
      return {
        descripcion: f.esResponsable
          ? 'Eres el responsable de esta fase. Pasa sola a Lista para release cuando se cumplan los criterios.'
          : 'Pasa sola a Lista para release cuando todo esté integrado y se cumplan los criterios.',
        revisiones: [
          faltan
            ? { estado: 'falta', texto: `Para cerrar ${faltan === 1 ? 'falta 1 criterio' : `faltan ${faltan} criterios`}` }
            : { estado: 'ok', texto: 'Se cumplen todos los criterios de cierre' },
          ...(f.versionRelease
            ? [{ estado: 'info' as const, texto: `Al cerrar queda lista para crear el tag ${f.versionRelease} y publicar` }]
            : []),
        ],
      };
    }
    case 'PAUSADA': {
      const dias = desde ? Math.floor((Date.now() - desde.getTime()) / 86_400_000) : null;
      return {
        descripcion: `Pausada${quien}${desde ? ` el ${diaYMes(desde)}` : ''}${motivo}.`,
        revisiones: [
          ...(dias !== null
            ? [{ estado: 'info' as const, texto: `Lleva ${dias} ${dias === 1 ? 'día' : 'días'} en pausa` }]
            : []),
          ...(f.reanudarEl ? [{ estado: 'info' as const, texto: `Se prevé reanudar el ${diaYMes(f.reanudarEl)}` }] : []),
        ],
      };
    }
    case 'LISTA_PARA_RELEASE':
      return {
        descripcion: `Todo listo para publicar la versión ${f.versionRelease ?? '(sin versión)'}.`,
        revisiones: [
          ...impedimentos('SUBIR_A_MAIN'),
          {
            estado: f.criteriosCumplidos === f.criterios.length ? 'ok' : 'falta',
            texto: `Criterios de cierre ${f.criteriosCumplidos} de ${f.criterios.length}`,
          },
          {
            estado: f.historiasIntegradas === f.historiasQueCuentan ? 'ok' : 'falta',
            texto: `Historias integradas ${f.historiasIntegradas} de ${f.historiasQueCuentan}`,
          },
        ],
      };
    case 'EN_PUBLICACION':
      return {
        descripcion: `El código ya está en main con el tag ${f.versionRelease}.`,
        revisiones: [
          { estado: 'ok', texto: 'Merge a main hecho' },
          { estado: 'ok', texto: `Tag ${f.versionRelease} en ${f.repositorios}` },
        ],
      };
    case 'PUBLICADA':
      return {
        descripcion: `${f.versionRelease} en producción${desde ? ` desde el ${diaYMes(desde)}` : ''}. Historias y tickets cerrados.`,
        revisiones: [
          ...impedimentos('REVERTIR'),
          { estado: 'info', texto: 'Revertir pide motivo y crea un BUGS' },
        ],
      };
    case 'REVERTIDA':
      return {
        descripcion:
          `Se volvió a ${f.versionAnterior ?? 'la versión anterior'}${desde ? ` el ${diaYMes(desde)}` : ''}${motivo}.` +
          (f.bugReversion ? ` Se creó ${f.bugReversion.codigo}.` : ''),
        revisiones: f.bugReversion
          ? [
              f.bugReversion.abierto
                ? { estado: 'falta', texto: `${f.bugReversion.codigo} sigue abierto` }
                : { estado: 'ok', texto: `${f.bugReversion.codigo} ya está resuelto` },
            ]
          : [],
      };
    case 'CANCELADA':
      return {
        descripcion: `Cancelada${quien}${motivo}.`,
        revisiones: [{ estado: 'info', texto: 'Solo el Admin puede reabrir' }],
      };
    default:
      return { descripcion: '', revisiones: [] };
  }
}
