import { Component, effect, inject, signal, untracked } from '@angular/core';
import { Observable } from 'rxjs';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { Modal } from '../../../../shared/components/modal/modal';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { MODULOS, TIPOS_MODULO } from '../../../../shared/utils/proyectos';
import { EquipoService } from '../../data/equipo.service';
import { ModulosService } from '../../data/modulos.service';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';
import { DatosModulo, ModulosProyecto, TarjetaModulo } from '../../models/modulos.models';

/**
 * Pestaña Módulos (PDF pág. 35): una tabla con cada parte del sistema, su tecnología, repositorio, versión en cada
 * ambiente, último build y tickets abiertos. Puede haber varios del mismo tipo (microservicios, varias apps...).
 * El Líder (o un Admin) agrega, edita y quita módulos.
 */
@Component({
  selector: 'app-modulos-tab',
  imports: [Modal],
  templateUrl: './modulos-tab.html',
  styleUrl: './modulos-tab.scss',
})
export class ModulosTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly service = inject(ModulosService);
  private readonly equipos = inject(EquipoService);

  protected readonly modulos = signal<ModulosProyecto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal(false);

  /** El formulario abierto: id null = agregar */
  protected readonly editando = signal<{ id: number | null; datos: DatosModulo } | null>(null);
  protected readonly intentado = signal(false);
  protected readonly personas = signal<{ id: number; nombre: string }[]>([]);
  protected readonly tipos = TIPOS_MODULO.map((tipo) => ({ tipo, info: MODULOS[tipo] }));

  private proyectoId = 0;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargar(id));
    });
  }

  protected abrirAgregar(tipo = 'API'): void {
    this.abrir(null, { tipo, nombre: '', tecnologia: '', repositorio: '', ramaBase: 'develop', responsableId: null });
  }

  protected abrirEditar(m: TarjetaModulo): void {
    this.abrir(m.id, {
      tipo: m.tipo,
      nombre: m.nombre,
      tecnologia: m.tecnologia ?? '',
      repositorio: m.repositorio ?? '',
      ramaBase: m.ramaBase,
      responsableId: m.responsable?.id ?? null,
    });
  }

  protected cambiar<K extends keyof DatosModulo>(campo: K, valor: DatosModulo[K]): void {
    const e = this.editando();
    if (e) this.editando.set({ ...e, datos: { ...e.datos, [campo]: valor } });
  }

  protected guardar(): void {
    const e = this.editando();
    if (!e) return;
    this.intentado.set(true);
    if (!e.datos.nombre.trim()) return;
    this.ejecutar(this.service.guardar(this.proyectoId, e.id, e.datos),
      e.id === null ? `Se agregó el repositorio ${e.datos.nombre.trim()}.` : `Se guardó ${e.datos.nombre.trim()}.`,
      () => this.editando.set(null));
  }

  protected quitar(m: TarjetaModulo): void {
    this.ejecutar(this.service.quitar(this.proyectoId, m.id), `Se quitó el repositorio ${m.nombre}.`);
  }

  private abrir(id: number | null, datos: DatosModulo): void {
    this.aviso.set(null);
    this.intentado.set(false);
    this.editando.set({ id, datos });
    if (!this.personas().length) {
      this.equipos.equipo(this.proyectoId).subscribe((e) =>
        this.personas.set(e.miembros.map((m) => ({ id: m.id, nombre: m.nombreCompleto }))));
    }
  }

  private ejecutar(peticion: Observable<void>, exito: string, alTerminar?: () => void): void {
    this.enviando.set(true);
    this.aviso.set(null);
    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        alTerminar?.();
        this.aviso.set({ texto: exito, error: false });
        this.cargar(this.proyectoId);
        this.store.recargar(); // el número de la pestaña y los chips del encabezado
      },
      error: (err) => {
        this.enviando.set(false);
        this.aviso.set({ texto: mensajeDeError(err, 'No pudimos guardar el repositorio.'), error: true });
      },
    });
  }

  private cargar(id: number): void {
    this.proyectoId = id;
    this.service.modulos(id).subscribe({
      next: (m) => {
        this.modulos.set(m);
        this.cargando.set(false);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar los repositorios.'));
        this.cargando.set(false);
      },
    });
  }
}
