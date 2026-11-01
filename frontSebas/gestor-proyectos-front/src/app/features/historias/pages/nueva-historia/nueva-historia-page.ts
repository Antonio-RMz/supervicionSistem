import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { map, Observable } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { ListaEditable } from '../../../../shared/components/lista-editable/lista-editable';
import { Etiqueta } from '../../../../shared/models/etiqueta.model';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { etiquetaHistoria } from '../../../../shared/utils/tickets';
import { HistoriasService } from '../../data/historias.service';
import { FaseOpcion, HistoriaGuardada, PreparacionHistoria } from '../../models/historias.models';

const BORRADOR: Etiqueta = { texto: 'Borrador', tono: 'gris' };

/**
 * Nueva historia de usuario (PDF pág. 18) y Editar historia (con :historiaId en la ruta).
 * Nace como Borrador con lo básico (fase, título, Analista y prioridad); el resto se captura al editarla ya
 * En planeación. Al editar no cambia de fase (para eso está "Mover de fase") y una historia Lista no puede
 * quedar incompleta.
 */
@Component({
  selector: 'app-nueva-historia-page',
  imports: [ReactiveFormsModule, RouterLink, ChipModulo, EtiquetaEstado, ListaEditable],
  templateUrl: './nueva-historia-page.html',
  styleUrl: './nueva-historia-page.scss',
})
export class NuevaHistoriaPage implements OnInit {
  private readonly service = inject(HistoriasService);
  private readonly router = inject(Router);
  private readonly usuario = inject(AuthService).usuario;

  /** :id del proyecto */
  readonly id = input.required<string>();
  /** ?fase=ID: viene de "Nueva historia en Fase N" */
  readonly fase = input<string>();
  /** :historiaId al editar; vacío en Nueva historia */
  readonly historiaId = input<string>();

  protected readonly editando = computed(() => !!this.historiaId());
  /** Lo básico al crearla y mientras es Borrador; el formulario completo, desde En planeación. */
  protected readonly completo = computed(() => this.editando() && !!this.guardada() && this.guardada()!.estado !== 'BORRADOR');
  /** Cómo estaba la historia antes de editarla. */
  protected readonly guardada = signal<HistoriaGuardada | null>(null);
  protected readonly etiqueta = computed(() => {
    const g = this.guardada();
    return g ? etiquetaHistoria(g.estado) : BORRADOR;
  });

  protected readonly form = inject(NonNullableFormBuilder).group({
    faseId: [null as number | null, Validators.required],
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    como: ['', Validators.maxLength(200)],
    quiero: ['', Validators.maxLength(300)],
    para: ['', Validators.maxLength(300)],
    figmaUrl: ['', Validators.maxLength(300)],
    disenoAprobado: [false],
    analistaId: [null as number | null, Validators.required],
    prioridad: ['MEDIA'],
    puntos: [null as number | null],
  });

  protected readonly preparacion = signal<PreparacionHistoria | null>(null);
  protected readonly criterios = signal<string[]>([]);
  protected readonly moduloIds = signal<number[]>([]);

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** "Crear historia" ya se intentó: se marcan también los campos que pide la historia completa. */
  protected readonly intentoCompleto = signal(false);

  protected readonly puntosEscala = [1, 2, 3, 5, 8, 13];

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  protected readonly faseElegida = computed<FaseOpcion | null>(
    () => this.preparacion()?.fases.find((f) => f.id === this.valor().faseId) ?? null,
  );

  /** Si el enlace es de Figma, se muestra la tarjeta con "Abrir en Figma". */
  protected readonly enlaceFigma = computed(() => {
    const url = (this.valor().figmaUrl ?? '').trim();
    if (!url) return null;
    return url.startsWith('http') ? url : `https://${url}`;
  });

  ngOnInit(): void {
    if (this.editando()) {
      this.cargarEdicion();
      return;
    }
    this.service.preparar(Number(this.id())).subscribe({
      next: (p) => {
        this.preparacion.set(p);
        const faseInicial = p.fases.find((f) => f.id === Number(this.fase())) ?? p.fases[0];
        if (faseInicial) this.elegirFase(faseInicial.id);
        // Quien la crea (Analista o Líder) queda como responsable; si no, el primer Analista.
        const yo = p.analistas.find((a) => a.id === this.usuario()?.id);
        this.form.controls.analistaId.setValue(yo?.id ?? p.analistas.find((a) => a.esAnalista)?.id ?? p.analistas[0]?.id ?? null);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos preparar la historia nueva.'));
        this.cargando.set(false);
      },
    });
  }

  private cargarEdicion(): void {
    this.service.prepararEdicion(Number(this.id()), Number(this.historiaId())).subscribe({
      next: ({ preparacion, historia }) => {
        this.preparacion.set(preparacion);
        this.guardada.set(historia);
        const { criterios, moduloIds, borrador, ...datos } = historia.datos;
        this.form.patchValue(datos);
        this.criterios.set(criterios);
        this.moduloIds.set(moduloIds);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos abrir la historia para editarla.'));
        this.cargando.set(false);
      },
    });
  }

  /** Al cambiar de fase se toman sus módulos (todos marcados). */
  protected elegirFase(faseId: number): void {
    const fase = this.preparacion()?.fases.find((f) => f.id === faseId);
    this.form.patchValue({ faseId });
    this.moduloIds.set(fase?.modulos.map((m) => m.id) ?? []);
  }

  protected alternarModulo(id: number): void {
    this.moduloIds.update((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]));
  }

  protected elegirPuntos(puntos: number): void {
    this.form.controls.puntos.setValue(this.form.controls.puntos.value === puntos ? null : puntos);
  }

  protected invalido(campo: 'faseId' | 'titulo' | 'analistaId'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  /** Lo que "Crear historia" pide además del borrador. */
  protected falta(campo: 'como' | 'quiero' | 'para' | 'criterios' | 'modulos'): boolean {
    if (!this.intentoCompleto()) return false;
    if (campo === 'criterios') return !this.criterios().length;
    if (campo === 'modulos') return !this.moduloIds().length;
    return !this.form.controls[campo].value.trim();
  }

  /** Al crearla y mientras se define puede quedar incompleta; una historia Lista, no. */
  protected guardarHistoria(): void {
    this.guardar(!this.editando() || ['BORRADOR', 'EN_PLANEACION'].includes(this.guardada()?.estado ?? ''));
  }

  private guardar(borrador: boolean): void {
    this.intentoCompleto.set(!borrador);
    const incompleta = !borrador && (['como', 'quiero', 'para', 'criterios', 'modulos'] as const).some((c) => this.falta(c));
    if (this.form.invalid || incompleta) {
      this.form.markAllAsTouched();
      this.error.set(
        incompleta && this.form.valid
          ? 'Ya está Lista: no puede quedar incompleta. Completa lo marcado.'
          : 'Revisa los campos marcados.',
      );
      return;
    }
    if (this.form.controls.disenoAprobado.value && !this.enlaceFigma()) {
      this.error.set('Para marcar el diseño como aprobado, pon el enlace de Figma.');
      return;
    }
    const proyectoId = Number(this.id());
    const datos = {
      ...this.form.getRawValue(),
      borrador,
      criterios: this.criterios(),
      moduloIds: this.moduloIds(),
    };
    this.enviando.set(true);
    this.error.set(null);
    const historiaId = this.historiaId();
    // Recién creada se abre su detalle: ahí se inicia su planeación.
    const guardado: Observable<number> = historiaId
      ? this.service.editar(proyectoId, Number(historiaId), datos).pipe(map(() => Number(historiaId)))
      : this.service.crear(proyectoId, datos);
    guardado.subscribe({
      next: (id) => this.router.navigate(['/proyectos', proyectoId, 'historias', id]),
      error: (e) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos guardar la historia. Intenta de nuevo.'));
      },
    });
  }
}
