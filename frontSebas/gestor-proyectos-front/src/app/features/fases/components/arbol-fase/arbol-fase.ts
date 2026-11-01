import { NgTemplateOutlet } from '@angular/common';
import { afterNextRender, Component, computed, ElementRef, inject, Injector, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { TIPOS_TICKET } from '../../../../shared/utils/flujos';
import { etiquetaEstadoTicket, etiquetaHistoria } from '../../../../shared/utils/tickets';
import { ProyectosDetalleService } from '../../../proyectos/data/proyectos-detalle.service';
import { FaseArbol, HistoriaArbol, TicketArbol } from '../../../proyectos/models/proyectos.models';

/** Las cuatro etapas de la leyenda; un ticket fuera del camino (cancelado, rechazado) no entra en ninguna. */
type Etapa = 'por-hacer' | 'en-progreso' | 'en-qa' | 'cerrada';

const ETAPAS: { etapa: Etapa; texto: string; tono: string }[] = [
  { etapa: 'por-hacer', texto: 'Por hacer', tono: 'gris' },
  { etapa: 'en-progreso', texto: 'En progreso', tono: 'morado' },
  { etapa: 'en-qa', texto: 'En QA', tono: 'naranja' },
  { etapa: 'cerrada', texto: 'Cerrada', tono: 'verde' },
];

const ESTADOS_DE_ETAPA: Record<Etapa, string[]> = {
  'por-hacer': ['BORRADOR', 'POR_HACER', 'REPORTADO', 'CONFIRMADO', 'PROPUESTA'],
  'en-progreso': ['EN_PROGRESO', 'EN_CORRECCION', 'EN_REVISION', 'BLOQUEADA'],
  // PR aprobado: espera a que QA pruebe la historia completa.
  'en-qa': ['EN_QA', 'APROBADA', 'APROBADO', 'APROBADA_QA'],
  'cerrada': ['INTEGRADA', 'INTEGRADO', 'PUBLICADO', 'CERRADA', 'CERRADO'],
};

type Seleccion = { tipo: 'fase' } | { tipo: 'historia'; id: number } | { tipo: 'ticket'; id: number };

const ZOOM_MINIMO = 50;
const ZOOM_MAXIMO = 150;

/**
 * Vista árbol de la fase: la fase, sus historias y los tickets de cada historia, conectados.
 * Clic en un nodo lo muestra en el panel; doble clic abre su detalle. La flecha de cada historia la contrae.
 */
@Component({
  selector: 'app-arbol-fase',
  imports: [NgTemplateOutlet, RouterLink, Avatar, BarraAvance, ChipModulo, EtiquetaEstado, TipoTicket],
  templateUrl: './arbol-fase.html',
  styleUrl: './arbol-fase.scss',
})
export class ArbolFase implements OnInit {
  private readonly service = inject(ProyectosDetalleService);
  private readonly router = inject(Router);
  private readonly usuario = inject(AuthService).usuario;
  private readonly elemento = inject(ElementRef<HTMLElement>);
  private readonly injector = inject(Injector);

  readonly proyectoId = input.required<number>();
  readonly faseId = input.required<number>();
  /** Desde el detalle de una historia o un ticket: el árbol abre con ese nodo elegido y a la vista. */
  readonly historiaId = input<number | null>(null);
  readonly ticketId = input<number | null>(null);

  protected readonly arbol = signal<FaseArbol | null>(null);
  protected readonly puedeCrearTicket = signal(false);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly contraidas = signal<Set<number>>(new Set());
  protected readonly seleccion = signal<Seleccion>({ tipo: 'fase' });
  protected readonly filtroTipo = signal('');
  protected readonly filtroEtapa = signal<Etapa | ''>('');
  protected readonly soloMios = signal(false);
  protected readonly zoom = signal(100);
  /** Cuánto se ha movido el diagrama dentro del lienzo, en píxeles de pantalla. */
  protected readonly desplazamiento = signal({ x: 0, y: 0 });
  /** Dónde empezó a arrastrarse el fondo con la manita; null si no se está arrastrando. */
  protected readonly arrastre = signal<{ x: number; y: number; desdeX: number; desdeY: number } | null>(null);

  /** Con zoom, el translate también se escala: se compensa para que el diagrama siga al cursor. */
  protected readonly transformacion = computed(() => {
    const { x, y } = this.desplazamiento();
    const escala = this.zoom() / 100;
    return `translate(${x / escala}px, ${y / escala}px)`;
  });

  protected readonly etapas = ETAPAS;
  protected readonly tipos = TIPOS_TICKET;
  protected readonly etiquetaHistoria = etiquetaHistoria;
  protected readonly etiquetaEstadoTicket = etiquetaEstadoTicket;

  protected readonly filtrando = computed(() => !!this.filtroTipo() || !!this.filtroEtapa() || this.soloMios());

  /** Las historias con sus tickets ya filtrados; con algún filtro, solo las que tienen algo que mostrar. */
  protected readonly historias = computed(() => {
    const a = this.arbol();
    if (!a) return [];
    const conTickets = a.historias.map((h) => ({ historia: h, tickets: h.tickets.filter((t) => this.pasa(t)) }));
    return this.filtrando() ? conTickets.filter((h) => h.tickets.length) : conTickets;
  });

  /** Tickets de la fase que no cuelgan de una historia (bugs de mantenimiento...). */
  protected readonly sueltos = computed(() => (this.arbol()?.sueltos ?? []).filter((t) => this.pasa(t)));

  protected readonly historiaElegida = computed(() => {
    const s = this.seleccion();
    return s.tipo === 'historia' ? (this.arbol()?.historias.find((h) => h.id === s.id) ?? null) : null;
  });

  protected readonly ticketElegido = computed(() => {
    const s = this.seleccion();
    if (s.tipo !== 'ticket') return null;
    const a = this.arbol();
    for (const h of a?.historias ?? []) {
      const t = h.tickets.find((x) => x.id === s.id);
      if (t) return { ticket: t, historia: h as HistoriaArbol | null };
    }
    const suelto = a?.sueltos.find((x) => x.id === s.id);
    return suelto ? { ticket: suelto, historia: null } : null;
  });

  ngOnInit(): void {
    forkJoin([this.service.arbol(this.proyectoId()), this.service.resumen(this.proyectoId())]).subscribe({
      next: ([arbol, resumen]) => {
        this.arbol.set(arbol.find((f) => f.fase.id === this.faseId()) ?? null);
        this.puedeCrearTicket.set(resumen.puedeCrearTicket);
        this.cargando.set(false);
        this.elegirInicial();
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos cargar el árbol de la fase.'));
        this.cargando.set(false);
      },
    });
  }

  private elegirInicial(): void {
    const ticketId = this.ticketId();
    const historiaId = this.historiaId();
    if (ticketId) this.seleccion.set({ tipo: 'ticket', id: ticketId });
    else if (historiaId) this.seleccion.set({ tipo: 'historia', id: historiaId });
    else return;
    // Ya pintado, se centra el nodo elegido dentro del lienzo.
    afterNextRender(() => this.centrarElegido(), { injector: this.injector });
  }

  protected etapaDe(estado: string): Etapa | null {
    return ETAPAS.find((e) => ESTADOS_DE_ETAPA[e.etapa].includes(estado))?.etapa ?? null;
  }

  protected contraida(historiaId: number): boolean {
    return this.contraidas().has(historiaId);
  }

  protected alternar(historiaId: number, evento: Event): void {
    evento.stopPropagation();
    this.contraidas.update((s) => {
      const nuevo = new Set(s);
      if (!nuevo.delete(historiaId)) nuevo.add(historiaId);
      return nuevo;
    });
  }

  protected expandirTodo(): void {
    this.contraidas.set(new Set());
  }

  protected contraerTodo(): void {
    this.contraidas.set(new Set(this.arbol()?.historias.map((h) => h.id) ?? []));
  }

  protected acercar(paso: number): void {
    this.zoom.update((z) => Math.min(ZOOM_MAXIMO, Math.max(ZOOM_MINIMO, z + paso)));
  }

  /** Con la manita: solo desde el fondo, para que el clic en un nodo, botón o enlace siga funcionando. */
  protected empezarArrastre(evento: PointerEvent): void {
    if (evento.button !== 0 || (evento.target as HTMLElement).closest('.nodo, button, a')) return;
    (evento.currentTarget as HTMLElement).setPointerCapture(evento.pointerId);
    const { x, y } = this.desplazamiento();
    this.arrastre.set({ x: evento.clientX, y: evento.clientY, desdeX: x, desdeY: y });
  }

  protected arrastrar(evento: PointerEvent): void {
    const inicio = this.arrastre();
    if (!inicio) return;
    this.desplazamiento.set({
      x: inicio.desdeX + evento.clientX - inicio.x,
      y: inicio.desdeY + evento.clientY - inicio.y,
    });
  }

  protected soltar(evento: PointerEvent): void {
    if (!this.arrastre()) return;
    (evento.currentTarget as HTMLElement).releasePointerCapture(evento.pointerId);
    this.arrastre.set(null);
  }

  /** La rueda mueve el diagrama (Shift, de lado); con Ctrl acerca o aleja. */
  protected rueda(evento: WheelEvent): void {
    evento.preventDefault();
    if (evento.ctrlKey) {
      this.acercar(evento.deltaY < 0 ? 10 : -10);
      return;
    }
    const lado = evento.shiftKey ? evento.deltaY : evento.deltaX;
    const abajo = evento.shiftKey ? 0 : evento.deltaY;
    this.desplazamiento.update(({ x, y }) => ({ x: x - lado, y: y - abajo }));
  }

  /** De vuelta al inicio: sin moverlo y al 100%. */
  protected centrar(): void {
    this.desplazamiento.set({ x: 0, y: 0 });
    this.zoom.set(100);
  }

  /** Mueve el diagrama para que el nodo elegido quede al centro del lienzo. */
  private centrarElegido(): void {
    const raiz = this.elemento.nativeElement as HTMLElement;
    const lienzo = raiz.querySelector('.lienzo')?.getBoundingClientRect();
    const nodo = raiz.querySelector('.nodo.elegido')?.getBoundingClientRect();
    if (!lienzo || !nodo) return;
    this.desplazamiento.update(({ x, y }) => ({
      x: x + lienzo.left + lienzo.width / 2 - (nodo.left + nodo.width / 2),
      y: y + lienzo.top + lienzo.height / 2 - (nodo.top + nodo.height / 2),
    }));
  }

  protected elegir(seleccion: Seleccion): void {
    this.seleccion.set(seleccion);
  }

  protected elegido(tipo: Seleccion['tipo'], id?: number): boolean {
    const s = this.seleccion();
    return s.tipo === tipo && (s.tipo === 'fase' || s.id === id);
  }

  protected abrir(ruta: (string | number)[]): void {
    this.router.navigate(['/proyectos', this.proyectoId(), ...ruta]);
  }

  /** Sus Actividades, o sus bugs y mejoras. */
  protected cuantos(h: HistoriaArbol, actividades: boolean): number {
    return h.tickets.filter((t) => (t.tipo === 'ACTV') === actividades).length;
  }

  /** "Por hacer", "Te toca: Iniciar", "Le toca a Ana · Analista" */
  protected siguientePaso(t: TicketArbol): string {
    if (t.accion) return `Te toca: ${t.accion}`;
    if (t.quien) return `Le toca a ${t.quien}`;
    return 'Nadie tiene acción pendiente';
  }

  private pasa(t: TicketArbol): boolean {
    const yo = this.usuario()?.id;
    return (!this.filtroTipo() || t.tipo === this.filtroTipo())
      && (!this.filtroEtapa() || this.etapaDe(t.estado) === this.filtroEtapa())
      && (!this.soloMios() || t.responsableId === yo || !!t.accion);
  }
}
