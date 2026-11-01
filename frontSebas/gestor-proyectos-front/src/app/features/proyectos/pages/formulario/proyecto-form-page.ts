import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormArray, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, map, Observable } from 'rxjs';
import { AuthService } from '../../../../core/auth/auth.service';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { fechaCorta } from '../../../../shared/utils/fechas';
import {
  etiquetaProyecto,
  infoModulo,
  MODULOS,
  ORDEN_ESTADOS_PROYECTO,
  TIPOS_MODULO,
} from '../../../../shared/utils/proyectos';
import { etiquetaDeRol } from '../../../../shared/utils/roles';
import { ClientesService } from '../../../clientes/data/clientes.service';
import { Cliente } from '../../../clientes/models/clientes.models';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { ProyectosDetalleService } from '../../data/proyectos-detalle.service';
import { DatosProyecto, ModuloForm, Persona } from '../../models/proyectos.models';

/** Al nombre del módulo se le sugiere "<proyecto>-api", "<proyecto>-web"... */
const SUFIJOS: Record<string, string> = {
  API: 'api', WEB: 'web', MOVIL: 'app', ESCRITORIO: 'desktop', BASE_DATOS: 'db', INFRAESTRUCTURA: 'infra',
};

/**
 * Nuevo proyecto (PDF pág. 16). Con :id en la ruta sirve para editarlo: entonces también se puede cambiar el estado.
 * El proyecto nace en Planeación; las fases e historias se agregan después, desde el proyecto.
 */
@Component({
  selector: 'app-proyecto-form-page',
  imports: [ReactiveFormsModule, RouterLink, Avatar, EtiquetaEstado],
  templateUrl: './proyecto-form-page.html',
  styleUrl: './proyecto-form-page.scss',
})
export class ProyectoFormPage implements OnInit {
  private readonly service = inject(ProyectosDetalleService);
  private readonly clientesService = inject(ClientesService);
  private readonly proyectoActual = inject(ProyectoActualStore);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuario = inject(AuthService).usuario;

  /** :id de la ruta al editar; vacío al crear */
  readonly id = input<string>();

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    /** Interno: es del propio equipo y no lleva cliente. */
    interno: [false],
    clienteId: [null as number | null, Validators.required],
    prioridad: ['MEDIA'],
    estado: ['PLANEACION'],
    fechaInicio: [''],
    fechaEntrega: [''],
    descripcion: [''],
    liderId: [null as number | null, Validators.required],
    modulos: this.fb.array<ReturnType<ProyectoFormPage['grupoModulo']>>([]),
    reglas: this.fb.group({
      permitirAutovalidacion: [true],
      comentarioAlRechazar: [true],
      ramaAlIniciar: [true],
      notificarClienteFase: [false],
    }),
  });

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly codigo = signal<string | null>(null);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly personas = signal<Persona[]>([]);
  protected readonly miembroIds = signal<number[]>([]);

  protected readonly editando = computed(() => !!this.id());
  /** El Líder edita su proyecto, pero el equipo lo arma el Administrador. */
  protected readonly armaEquipo = computed(() => this.usuario()?.roles.includes('ADMIN') ?? false);
  protected readonly estados = ORDEN_ESTADOS_PROYECTO.map((e) => ({ codigo: e, texto: etiquetaProyecto(e).texto }));
  protected readonly tiposModulo = TIPOS_MODULO.map((tipo) => ({ tipo, ...MODULOS[tipo] }));
  protected readonly infoModulo = infoModulo;
  protected readonly etiquetaDeRol = etiquetaDeRol;

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  protected readonly miembros = computed(() => {
    const ids = this.miembroIds();
    return this.personas().filter((p) => ids.includes(p.id));
  });

  /** Solo quien tiene el rol Líder puede liderar; al editar se conserva el que ya tiene. */
  protected readonly lideres = computed(() =>
    this.personas().filter((p) => (p.activo && p.roles.includes('LIDER')) || p.id === this.valor().liderId),
  );

  protected readonly disponibles = computed(() => {
    const ids = this.miembroIds();
    return this.personas().filter((p) => p.activo && !ids.includes(p.id));
  });

  /** Los inactivos no aparecen al crear; al editar se conserva el que ya tiene. */
  protected readonly clientesElegibles = computed(() => {
    const actual = this.valor().clienteId;
    return this.clientes().filter((c) => c.activo || c.id === actual);
  });

  /** Tipos de módulo que aún no tiene: se ofrecen como "Sugeridos". */
  protected readonly sugeridos = computed(() => {
    const usados = (this.valor().modulos ?? []).map((m) => m.tipo);
    return this.tiposModulo.filter((t) => !usados.includes(t.tipo));
  });

  /** La tarjeta "Resumen" de la derecha. */
  protected readonly resumen = computed(() => {
    const v = this.valor();
    const cliente = this.clientes().find((c) => c.id === v.clienteId);
    const lider = this.personas().find((p) => p.id === v.liderId);
    const modulos = (v.modulos ?? []).map((m) => m.nombre?.trim() || infoModulo(m.tipo ?? '').corto);
    const inicio = v.fechaInicio ? fechaCorta(new Date(v.fechaInicio + 'T00:00')) : '';
    const entrega = v.fechaEntrega ? fechaCorta(new Date(v.fechaEntrega + 'T00:00')) : '';
    return {
      nombre: v.nombre?.trim() || 'Proyecto sin nombre',
      iniciales: this.codigo() ?? iniciales(v.nombre ?? ''),
      etiqueta: etiquetaProyecto(v.estado ?? 'PLANEACION'),
      cliente: v.interno ? 'Interno (sin cliente)' : (cliente?.nombreCorto ?? '—'),
      prioridad: { ALTA: 'Alta', MEDIA: 'Media', BAJA: 'Baja' }[v.prioridad ?? 'MEDIA'] ?? '—',
      fechas: inicio || entrega ? `${inicio || '…'} – ${entrega || '…'}` : '—',
      lider: lider?.nombreCompleto ?? '—',
      equipo: `${this.miembroIds().length} ${this.miembroIds().length === 1 ? 'persona' : 'personas'}`,
      modulos: modulos.length ? `${modulos.length} · ${modulos.join(', ')}` : 'Ninguno',
      autovalidacion: v.reglas?.permitirAutovalidacion ? 'Permitida' : 'No permitida',
    };
  });

  constructor() {
    // Interno: no se elige cliente (y el campo no cuenta como pendiente).
    this.form.controls.interno.valueChanges.subscribe((interno) => {
      const cliente = this.form.controls.clienteId;
      if (interno) {
        cliente.setValue(null);
        cliente.disable({ emitEvent: false });
      } else {
        cliente.enable({ emitEvent: false });
      }
    });
  }

  get modulos(): FormArray {
    return this.form.controls.modulos;
  }

  ngOnInit(): void {
    const id = this.id() ? Number(this.id()) : null;
    forkJoin({
      clientes: this.clientesService.listar(),
      personas: this.service.personas(),
    }).subscribe({
      next: ({ clientes, personas }) => {
        this.clientes.set(clientes.clientes);
        this.personas.set(personas);
        if (id) {
          this.cargarProyecto(id);
        } else {
          this.valoresIniciales(personas);
          this.cargando.set(false);
        }
      },
      error: () => {
        this.error.set('No pudimos cargar los clientes y el equipo. Intenta recargar la página.');
        this.cargando.set(false);
      },
    });
  }

  // ─── Equipo ───────────────────────────────────────────────────────────────

  /** El Líder siempre queda en el equipo. */
  protected elegirLider(id: string): void {
    const liderId = Number(id) || null;
    this.form.controls.liderId.setValue(liderId);
    if (liderId && !this.miembroIds().includes(liderId)) this.miembroIds.update((ids) => [liderId, ...ids]);
  }

  protected agregarMiembro(id: string): void {
    if (id) this.miembroIds.update((ids) => [...ids, Number(id)]);
  }

  protected quitarMiembro(id: number): void {
    if (id === this.form.controls.liderId.value) return;
    this.miembroIds.update((ids) => ids.filter((m) => m !== id));
  }

  // ─── Módulos ──────────────────────────────────────────────────────────────

  protected agregarModulo(tipo?: string): void {
    const elegido = tipo ?? this.sugeridos()[0]?.tipo ?? 'API';
    this.modulos.push(this.grupoModulo({
      id: null,
      tipo: elegido,
      nombre: this.nombreSugerido(elegido),
      tecnologia: '',
      repositorio: '',
      ramaBase: 'develop',
    }));
  }

  protected quitarModulo(indice: number): void {
    this.modulos.removeAt(indice);
  }

  // ─── Guardar ──────────────────────────────────────────────────────────────

  protected invalido(campo: 'nombre' | 'clienteId' | 'liderId'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revisa los campos marcados.');
      return;
    }
    const { interno, ...v } = this.form.getRawValue();
    const datos: DatosProyecto = {
      ...v,
      clienteId: interno ? null : v.clienteId,
      estado: this.editando() ? v.estado : null,
      miembroIds: this.miembroIds(),
      modulos: v.modulos as ModuloForm[],
    };
    const id = this.id() ? Number(this.id()) : null;
    this.enviando.set(true);
    this.error.set(null);
    // Al editar se queda en el mismo proyecto; al crear, el back regresa el id nuevo.
    const peticion: Observable<number> = id
      ? this.service.actualizar(id, datos).pipe(map(() => id))
      : this.service.crear(datos);
    peticion.subscribe({
      next: (proyectoId) => {
        this.proyectoActual.recargar();
        this.router.navigate(['/proyectos', proyectoId]);
      },
      error: (e: unknown) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos guardar el proyecto. Intenta de nuevo.'));
      },
    });
  }

  private grupoModulo(m: ModuloForm) {
    return this.fb.group({
      id: [m.id as number | null],
      tipo: [m.tipo, Validators.required],
      nombre: [m.nombre, [Validators.required, Validators.maxLength(60)]],
      tecnologia: [m.tecnologia],
      repositorio: [m.repositorio],
      ramaBase: [m.ramaBase],
    });
  }

  /** "portal-api"; si ya hay uno con ese nombre (otro microservicio), "portal-api-2". */
  private nombreSugerido(tipo: string): string {
    const base = `${slug(this.form.controls.nombre.value) || 'proyecto'}-${SUFIJOS[tipo] ?? tipo.toLowerCase()}`;
    const usados = new Set(this.modulos.controls.map((g) => (g.value.nombre ?? '').toLowerCase()));
    let nombre = base;
    for (let n = 2; usados.has(nombre); n++) nombre = `${base}-${n}`;
    return nombre;
  }

  /** Nuevo: el Líder es quien lo crea si tiene ese rol; la Autovalidación y demás, como en el PDF. */
  private valoresIniciales(personas: Persona[]): void {
    const yo = personas.find((p) => p.id === this.usuario()?.id);
    if (yo?.roles.includes('LIDER')) this.elegirLider(String(yo.id));
  }

  private cargarProyecto(id: number): void {
    this.service.configuracion(id).subscribe({
      next: ({ codigo, datos }) => {
        this.codigo.set(codigo);
        this.form.patchValue({
          ...datos,
          interno: datos.clienteId === null,
          estado: datos.estado ?? 'PLANEACION',
          modulos: [],
        });
        datos.modulos.forEach((m) => this.modulos.push(this.grupoModulo(m)));
        this.miembroIds.set([
          datos.liderId!,
          ...datos.miembroIds.filter((m) => m !== datos.liderId),
        ]);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos cargar el proyecto.'));
        this.cargando.set(false);
      },
    });
  }
}

/** "Portal de Clientes" -> "portal-de-clientes"... recortado a las dos primeras palabras: "portal-clientes" */
function slug(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .split(/[^a-z0-9]+/)
    .filter((p) => p && !['de', 'del', 'la', 'las', 'el', 'los', 'y'].includes(p))
    .slice(0, 2)
    .join('-');
}

function iniciales(nombre: string): string {
  const palabras = nombre.trim().split(/\s+/).filter((p) => p && !['de', 'del', 'la', 'el', 'y'].includes(p.toLowerCase()));
  if (!palabras.length) return '?';
  if (palabras.length === 1) return palabras[0].slice(0, 2).toUpperCase();
  return (palabras[0][0] + palabras[1][0]).toUpperCase();
}
