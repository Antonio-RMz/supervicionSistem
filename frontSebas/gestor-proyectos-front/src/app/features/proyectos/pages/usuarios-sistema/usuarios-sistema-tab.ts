import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';
import { UsuariosSistemaService } from '../../data/usuarios-sistema.service';
import { UsuarioDelSistema } from '../../models/usuarios-sistema.models';

/** Igual que en el back (UsuariosDelSistema.MAXIMO). */
const MAXIMO = 30;

/**
 * Pestaña Usuarios del sistema: quiénes usan el software que se construye (Administrador, Escuela, Alumno...).
 * Son el "Como" de las historias y van en el prompt de la IA. Se edita toda la lista y se guarda de una vez.
 */
@Component({
  selector: 'app-usuarios-sistema-tab',
  templateUrl: './usuarios-sistema-tab.html',
  styleUrl: './usuarios-sistema-tab.scss',
})
export class UsuariosSistemaTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly service = inject(UsuariosSistemaService);

  protected readonly maximo = MAXIMO;
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal(false);
  protected readonly puedeEditar = signal(false);
  /** Lo guardado, para saber si hay cambios. */
  private readonly guardados = signal<UsuarioDelSistema[]>([]);
  protected readonly usuarios = signal<UsuarioDelSistema[]>([]);

  protected readonly hayCambios = computed(() => JSON.stringify(this.usuarios()) !== JSON.stringify(this.guardados()));
  /** El primer nombre vacío o repetido, para no dejar guardar. */
  protected readonly problema = computed(() => {
    const vistos = new Set<string>();
    for (const u of this.usuarios()) {
      const nombre = u.nombre.trim().toLowerCase();
      if (!nombre) return 'Escribe el nombre de cada usuario o quita la fila vacía.';
      if (vistos.has(nombre)) return `"${u.nombre.trim()}" está repetido.`;
      vistos.add(nombre);
    }
    return null;
  });

  private proyectoId = 0;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargar(id));
    });
  }

  protected agregar(): void {
    this.usuarios.update((lista) => [...lista, { nombre: '', descripcion: '' }]);
  }

  protected cambiar(i: number, campo: keyof UsuarioDelSistema, valor: string): void {
    this.usuarios.update((lista) => lista.map((u, j) => (j === i ? { ...u, [campo]: valor } : u)));
  }

  protected quitar(i: number): void {
    this.usuarios.update((lista) => lista.filter((_, j) => j !== i));
  }

  /** Sube o baja una fila: el orden es el que se ve en el "Como" y en el prompt. */
  protected mover(i: number, paso: number): void {
    this.usuarios.update((lista) => {
      const nueva = [...lista];
      [nueva[i], nueva[i + paso]] = [nueva[i + paso], nueva[i]];
      return nueva;
    });
  }

  protected descartar(): void {
    this.usuarios.set(this.guardados());
  }

  protected guardar(): void {
    if (this.problema() || this.enviando()) return;
    this.enviando.set(true);
    this.aviso.set(null);
    this.service.guardar(this.proyectoId, this.usuarios()).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.set({ texto: 'Se guardaron los usuarios del sistema.', error: false });
        this.cargar(this.proyectoId);
      },
      error: (e) => {
        this.enviando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos guardar los usuarios del sistema.'), error: true });
      },
    });
  }

  private cargar(id: number): void {
    this.proyectoId = id;
    this.service.consultar(id).subscribe({
      next: (u) => {
        this.guardados.set(u.usuarios);
        this.usuarios.set(u.usuarios);
        this.puedeEditar.set(u.puedeEditar);
        this.cargando.set(false);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar los usuarios del sistema.'));
        this.cargando.set(false);
      },
    });
  }
}
