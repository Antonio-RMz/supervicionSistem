import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { ListaEditable } from '../../../../shared/components/lista-editable/lista-editable';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { fechaCorta } from '../../../../shared/utils/fechas';
import { etiquetaFase } from '../../../../shared/utils/proyectos';
import { FasesService } from '../../data/fases.service';
import { FaseGuardada, PreparacionFase } from '../../models/fases.models';

const VERSION = /^v\d+\.\d+\.\d+$/;

/**
 * Nueva fase (PDF pág. 17) y Editar fase (con :faseId en la ruta). Nace Planeada: se inicia desde su detalle cuando
 * ya tiene historias. Al editar no se pueden quitar los módulos que ya usan sus historias o tickets.
 */
@Component({
  selector: 'app-nueva-fase-page',
  imports: [ReactiveFormsModule, RouterLink, Avatar, EtiquetaEstado, ListaEditable],
  templateUrl: './nueva-fase-page.html',
  styleUrl: './nueva-fase-page.scss',
})
export class NuevaFasePage implements OnInit {
  private readonly service = inject(FasesService);
  private readonly router = inject(Router);

  /** :id del proyecto */
  readonly id = input.required<string>();
  /** :faseId al editar; vacío en Nueva fase */
  readonly faseId = input<string>();

  protected readonly editando = computed(() => !!this.faseId());
  /** Cómo estaba la fase antes de editarla. */
  protected readonly guardada = signal<FaseGuardada | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    objetivo: [''],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
    responsableId: [null as number | null, Validators.required],
    versionRelease: ['', Validators.pattern(VERSION)],
    prioridad: ['MEDIA'],
  });

  protected readonly preparacion = signal<PreparacionFase | null>(null);
  protected readonly moduloIds = signal<number[]>([]);
  protected readonly criteriosManuales = signal<string[]>(['Visto bueno del cliente en la demo']);

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly faltanModulos = signal(false);

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  protected readonly resumen = computed(() => {
    const p = this.preparacion();
    const v = this.valor();
    if (!p) return null;
    const inicio = v.fechaInicio ? new Date(v.fechaInicio + 'T00:00') : null;
    const fin = v.fechaFin ? new Date(v.fechaFin + 'T00:00') : null;
    const semanas = inicio && fin && fin >= inicio ? Math.ceil((fin.getTime() - inicio.getTime()) / (7 * 86_400_000)) : null;
    const modulos = p.modulos.filter((m) => this.moduloIds().includes(m.id)).map((m) => m.corto);
    const criterios = p.criteriosAutomaticos.length + this.criteriosManuales().length;
    return {
      nombre: v.nombre?.trim() || 'Fase sin nombre',
      etiqueta: etiquetaFase(this.guardada()?.estado ?? 'PLANEADA'),
      fechas: inicio || fin ? `${inicio ? fechaCorta(inicio) : '…'} – ${fin ? fechaCorta(fin) : '…'}` : '—',
      duracion: semanas === null ? '—' : `${semanas} ${semanas === 1 ? 'semana' : 'semanas'}`,
      responsable: p.equipo.find((e) => e.id === v.responsableId)?.nombreCompleto ?? '—',
      release: v.versionRelease?.trim() || '—',
      modulos: modulos.length ? modulos.join(', ') : 'Ninguno',
      criterios: `${criterios} (${this.criteriosManuales().length} ${this.criteriosManuales().length === 1 ? 'manual' : 'manuales'})`,
    };
  });

  ngOnInit(): void {
    if (this.editando()) {
      this.cargarEdicion();
      return;
    }
    this.service.preparar(Number(this.id())).subscribe({
      next: (p) => {
        this.preparacion.set(p);
        this.form.patchValue({
          fechaInicio: p.inicioSugerido,
          responsableId: p.liderId,
          versionRelease: p.versionSugerida,
        });
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos preparar la fase nueva.'));
        this.cargando.set(false);
      },
    });
  }

  private cargarEdicion(): void {
    this.service.prepararEdicion(Number(this.id()), Number(this.faseId())).subscribe({
      next: ({ preparacion, fase }) => {
        this.preparacion.set(preparacion);
        this.guardada.set(fase);
        const { moduloIds, criteriosManuales, ...datos } = fase.datos;
        this.form.patchValue(datos);
        this.moduloIds.set(moduloIds);
        this.criteriosManuales.set(criteriosManuales);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos abrir la fase para editarla.'));
        this.cargando.set(false);
      },
    });
  }

  /** Al editar, el módulo que la fase ya tenía y que usan sus historias o tickets no se puede quitar. */
  protected enUso(id: number): boolean {
    const g = this.guardada();
    return !!g && g.datos.moduloIds.includes(id) && g.modulosEnUso.includes(id);
  }

  protected alternarModulo(id: number): void {
    if (this.enUso(id) && this.moduloIds().includes(id)) return;
    this.moduloIds.update((ids) => (ids.includes(id) ? ids.filter((m) => m !== id) : [...ids, id]));
    this.faltanModulos.set(false);
  }

  protected invalido(campo: 'nombre' | 'fechaInicio' | 'fechaFin' | 'responsableId' | 'versionRelease'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected finAntesDeInicio(): boolean {
    const { fechaInicio, fechaFin } = this.form.getRawValue();
    return !!fechaInicio && !!fechaFin && fechaFin < fechaInicio;
  }

  protected guardar(): void {
    this.faltanModulos.set(!this.moduloIds().length);
    if (this.form.invalid || this.finAntesDeInicio() || this.faltanModulos()) {
      this.form.markAllAsTouched();
      this.error.set('Revisa los campos marcados.');
      return;
    }
    const proyectoId = Number(this.id());
    const datos = { ...this.form.getRawValue(), moduloIds: this.moduloIds(), criteriosManuales: this.criteriosManuales() };
    this.enviando.set(true);
    this.error.set(null);
    const faseId = this.faseId();
    const guardado: Observable<unknown> = faseId
      ? this.service.editar(proyectoId, Number(faseId), datos)
      : this.service.crear(proyectoId, datos);
    guardado.subscribe({
      next: () => this.router.navigate(faseId ? ['/proyectos', proyectoId, 'fases', faseId] : ['/proyectos', proyectoId, 'fases']),
      error: (e) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, faseId ? 'No pudimos guardar los cambios.' : 'No pudimos crear la fase. Intenta de nuevo.'));
      },
    });
  }
}
