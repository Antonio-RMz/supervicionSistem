import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { map, Observable } from 'rxjs';
import { ListaEditable } from '../../../../shared/components/lista-editable/lista-editable';
import { TipoTicket } from '../../../../shared/components/tipo-ticket/tipo-ticket';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { FLUJOS, ROL_DE_ACTOR, ramaSugerida, TIPOS_TICKET, TONO_ROL } from '../../../../shared/utils/flujos';
import { NuevoTicketService } from '../../data/nuevo-ticket.service';
import { EdicionTicket, PreparacionTicket } from '../../models/nuevo-ticket.models';

/**
 * Nuevo ticket de trabajo (PDF pág. 19). Vive dentro de una historia Lista, Lista para desarrollo o En desarrollo.
 * Con :ticketId lo edita mientras no empiece; tipo, fase e historia ya no cambian y las subtareas van en el detalle.
 */
@Component({
  selector: 'app-nuevo-ticket-page',
  imports: [ReactiveFormsModule, RouterLink, ListaEditable, TipoTicket],
  templateUrl: './nuevo-ticket-page.html',
  styleUrl: './nuevo-ticket-page.scss',
})
export class NuevoTicketPage implements OnInit {
  private readonly service = inject(NuevoTicketService);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  /** ?historia=ID: viene de "Agregar ticket a HU-xxxx" */
  readonly historia = input<string>();
  /** :ticketId al editar */
  readonly ticketId = input<string>();

  protected readonly editando = computed(() => !!this.ticketId());
  /** "ACTV-07" del ticket que se edita */
  protected readonly codigoActual = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    faseId: [null as number | null, Validators.required],
    historiaId: [null as number | null, Validators.required],
    tipo: ['ACTV'],
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    descripcion: [''],
    moduloId: [null as number | null, Validators.required],
    responsableId: [null as number | null, Validators.required],
    /** Su Analista prueba la historia completa: el ticket no lleva quién lo pruebe. */
    testerId: [null as number | null],
    prioridad: ['MEDIA'],
    estimacionHoras: [null as number | null, [Validators.min(0.5), Validators.max(999)]],
    fechaLimite: [''],
  });

  protected readonly preparacion = signal<PreparacionTicket | null>(null);
  protected readonly capas = signal<string[]>([]);
  protected readonly criterioIds = signal<number[]>([]);
  protected readonly subtareas = signal<string[]>([]);
  protected readonly dependeDe = signal<number[]>([]);

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly copiada = signal(false);

  protected readonly tipos = TIPOS_TICKET;

  // Al editar, fase, historia y tipo van deshabilitados y valueChanges no los trae: se lee todo con getRawValue.
  protected readonly valor = toSignal(this.form.valueChanges.pipe(map(() => this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });

  protected readonly fase = computed(() => this.preparacion()?.fases.find((f) => f.id === this.valor().faseId) ?? null);
  protected readonly historiaElegida = computed(
    () => this.fase()?.historias.find((h) => h.id === this.valor().historiaId) ?? null,
  );

  /** Quien hace el ticket es siempre un Desarrollador del equipo. */
  protected readonly desarrolladores = computed(() =>
    (this.preparacion()?.equipo ?? []).filter((e) => e.roles.includes('DESARROLLADOR')),
  );

  /** Los módulos de la historia; si la historia no dice, los de la fase. */
  protected readonly modulos = computed(() => {
    const fase = this.fase();
    const historia = this.historiaElegida();
    if (!fase) return [];
    return historia?.moduloIds.length ? fase.modulos.filter((m) => historia.moduloIds.includes(m.id)) : fase.modulos;
  });

  protected readonly modulo = computed(() => this.modulos().find((m) => m.id === this.valor().moduloId) ?? null);

  protected readonly numero = computed(() => {
    const actual = this.codigoActual();
    return actual ? Number(actual.split('-')[1]) : (this.preparacion()?.siguientes[this.valor().tipo ?? 'ACTV'] ?? 1);
  });
  protected readonly codigo = computed(
    () => this.codigoActual() ?? `${this.valor().tipo}-${String(this.numero()).padStart(2, '0')}`,
  );
  protected readonly rama = computed(() =>
    ramaSugerida(this.valor().tipo ?? 'ACTV', this.numero(), this.valor().titulo || 'nuevo ticket'),
  );

  protected readonly tonoRol = TONO_ROL;

  /** "Flujo que seguirá", con el tipo de usuario y el nombre de quién actúa en cada paso. */
  protected readonly flujo = computed(() => {
    const p = this.preparacion();
    const v = this.valor();
    const nombre = (id: number | null | undefined) =>
      p?.equipo.find((e) => e.id === id)?.nombreCompleto.split(' ')[0] ?? '¿?';
    return (FLUJOS[v.tipo ?? 'ACTV'] ?? []).map((paso) => ({
      estado: paso.estado,
      rol: paso.actor ? ROL_DE_ACTOR[paso.actor] : null,
      detalle:
        paso.actor === 'responsable' ? `${nombre(v.responsableId)} · ${paso.accion}`
        : paso.actor === 'tester' ? `${nombre(v.testerId)} · ${paso.accion}`
        : paso.actor === 'lider' ? `${nombre(p?.liderId)} · ${paso.accion}`
        : paso.accion,
    }));
  });

  /** De los abiertos, los que aún no se agregaron como dependencia. */
  protected readonly dependenciasDisponibles = computed(() =>
    (this.preparacion()?.abiertos ?? []).filter((t) => !this.dependeDe().includes(t.id)),
  );
  protected readonly dependencias = computed(() =>
    (this.preparacion()?.abiertos ?? []).filter((t) => this.dependeDe().includes(t.id)),
  );

  ngOnInit(): void {
    if (this.editando()) {
      this.service.edicion(Number(this.id()), Number(this.ticketId())).subscribe({
        next: (e) => this.cargarEdicion(e),
        error: (e) => {
          this.error.set(mensajeDeError(e, 'No pudimos abrir el ticket para editarlo.'));
          this.cargando.set(false);
        },
      });
      return;
    }
    this.service.preparar(Number(this.id())).subscribe({
      next: (p) => {
        this.preparacion.set(p);
        const pedida = Number(this.historia());
        const fase = p.fases.find((f) => f.historias.some((h) => h.id === pedida)) ?? p.fases.find((f) => f.historias.length);
        if (fase) this.elegirHistoria(fase.id, fase.historias.find((h) => h.id === pedida)?.id ?? fase.historias[0].id);
        this.form.patchValue({ responsableId: this.primeroCon(p, 'DESARROLLADOR') });
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos preparar el ticket nuevo.'));
        this.cargando.set(false);
      },
    });
  }

  private cargarEdicion(e: EdicionTicket): void {
    this.preparacion.set(e.preparacion);
    this.codigoActual.set(e.codigo);
    const { capas, criterioIds, dependeDe, subtareas: _, ...campos } = e.datos;
    this.form.patchValue(campos);
    this.form.controls.faseId.disable();
    this.form.controls.historiaId.disable();
    this.form.controls.tipo.disable();
    this.capas.set(capas);
    this.criterioIds.set(criterioIds);
    this.dependeDe.set(dependeDe);
    this.cargando.set(false);
  }

  protected elegirFase(faseId: number): void {
    const fase = this.preparacion()?.fases.find((f) => f.id === faseId);
    this.elegirHistoria(faseId, fase?.historias[0]?.id ?? null);
  }

  /** Al cambiar de historia: primer módulo de la historia, y marcados los criterios que nadie cubre aún. */
  protected elegirHistoria(faseId: number, historiaId: number | null): void {
    this.form.patchValue({ faseId, historiaId });
    this.form.controls.moduloId.setValue(this.modulos()[0]?.id ?? null);
    this.criterioIds.set(this.historiaElegida()?.criterios.filter((c) => !c.cubiertoPor).map((c) => c.id) ?? []);
  }

  protected alternarCapa(capa: string): void {
    this.capas.update((v) => (v.includes(capa) ? v.filter((c) => c !== capa) : [...v, capa]));
  }

  protected alternarCriterio(id: number): void {
    this.criterioIds.update((v) => (v.includes(id) ? v.filter((c) => c !== id) : [...v, id]));
  }

  protected agregarDependencia(id: string): void {
    if (id) this.dependeDe.update((ids) => [...ids, Number(id)]);
  }

  protected quitarDependencia(id: number): void {
    this.dependeDe.update((ids) => ids.filter((d) => d !== id));
  }

  protected copiarRama(): void {
    navigator.clipboard?.writeText(this.rama()).then(() => {
      this.copiada.set(true);
      setTimeout(() => this.copiada.set(false), 1500);
    });
  }

  protected invalido(campo: 'titulo' | 'moduloId' | 'responsableId' | 'historiaId' | 'estimacionHoras'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revisa los campos marcados.');
      return;
    }
    const proyectoId = Number(this.id());
    this.enviando.set(true);
    this.error.set(null);
    const datos = {
      ...this.form.getRawValue(),
      capas: this.capas(),
      criterioIds: this.criterioIds(),
      subtareas: this.subtareas(),
      dependeDe: this.dependeDe(),
    };
    const ticketId = Number(this.ticketId());
    const envio: Observable<unknown> = this.editando()
      ? this.service.editar(proyectoId, ticketId, datos)
      : this.service.crear(proyectoId, datos);
    envio.subscribe({
      next: () =>
        this.router.navigate(this.editando() ? ['/proyectos', proyectoId, 'tickets', ticketId] : ['/proyectos', proyectoId, 'fases']),
      error: (e) => {
        this.enviando.set(false);
        this.error.set(
          mensajeDeError(e, this.editando() ? 'No pudimos guardar los cambios. Intenta de nuevo.' : 'No pudimos crear el ticket. Intenta de nuevo.'),
        );
      },
    });
  }

  private primeroCon(p: PreparacionTicket, rol: string): number | null {
    return p.equipo.find((e) => e.roles.includes(rol))?.id ?? null;
  }
}
