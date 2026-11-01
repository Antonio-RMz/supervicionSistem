import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Etiqueta } from '../../../../shared/models/etiqueta.model';
import { etiquetaDeRol } from '../../../../shared/utils/roles';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { CatalogoAccesosStore } from '../../data/catalogo-accesos.store';
import { UsuariosService } from '../../data/usuarios.service';
import { Accesos, Permiso, UsuarioFila } from '../../models/usuarios.models';
import { NuevoUsuarioModal } from './nuevo-usuario/nuevo-usuario-modal';
import { PanelAccesos } from './panel-accesos/panel-accesos';

type Pestana = 'usuarios' | 'roles' | 'matriz' | 'flujos';

const NOMBRE_GRUPO: Record<string, string> = {
  ADMINISTRACION: 'Administración',
  PROYECTO: 'Proyecto',
  FASE: 'Fases',
  HISTORIA: 'Historias',
  TICKET: 'Tickets',
  RELEASE: 'Releases',
};

const ACTIVO: Etiqueta = { texto: 'Activo', tono: 'verde' };
const INACTIVO: Etiqueta = { texto: 'Inactivo', tono: 'gris' };

/** Usuarios y roles (PDF pág. 6): tabla de personas, panel de accesos y alta de usuarios. */
@Component({
  selector: 'app-usuarios-page',
  imports: [RouterLink, Avatar, EtiquetaEstado, PanelAccesos, NuevoUsuarioModal],
  templateUrl: './usuarios-page.html',
  styleUrl: './usuarios-page.scss',
})
export class UsuariosPage {
  private readonly service = inject(UsuariosService);
  protected readonly catalogo = inject(CatalogoAccesosStore);
  protected readonly proyectos = inject(ProyectoActualStore).proyectos;

  /** ?editar=ID abre el panel de esa persona (viene de "Editar roles y permisos" en su detalle). */
  readonly editar = input<string>();

  protected readonly usuarios = signal<UsuarioFila[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  protected readonly pestana = signal<Pestana>('usuarios');
  protected readonly busqueda = signal('');
  protected readonly seleccionadoId = signal<number | null>(null);
  protected readonly creando = signal(false);

  protected readonly filtrados = computed(() => {
    const texto = normalizar(this.busqueda());
    if (!texto) return this.usuarios();
    return this.usuarios().filter(
      (u) => normalizar(u.nombreCompleto).includes(texto) || u.correo.toLowerCase().includes(texto),
    );
  });

  protected readonly seleccionado = computed(
    () => this.usuarios().find((u) => u.id === this.seleccionadoId()) ?? null,
  );

  /** Matriz de permisos: los permisos agrupados como en el catálogo. */
  protected readonly grupos = computed(() => {
    const grupos = new Map<string, Permiso[]>();
    for (const permiso of this.catalogo.permisos()) {
      grupos.set(permiso.grupo, [...(grupos.get(permiso.grupo) ?? []), permiso]);
    }
    return [...grupos].map(([grupo, permisos]) => ({ nombre: NOMBRE_GRUPO[grupo] ?? grupo, permisos }));
  });

  protected readonly etiquetaDeRol = etiquetaDeRol;
  protected readonly activo = ACTIVO;
  protected readonly inactivo = INACTIVO;

  constructor() {
    this.catalogo.cargar();
    this.cargar();

    effect(() => {
      const id = Number(this.editar());
      if (id) this.seleccionadoId.set(id);
    });
  }

  protected elegir(usuario: UsuarioFila): void {
    this.seleccionadoId.set(this.seleccionadoId() === usuario.id ? null : usuario.id);
  }

  /** Al guardar en el panel, la fila muestra los roles nuevos sin volver a pedir la lista. */
  protected alGuardar(id: number, accesos: Accesos): void {
    this.usuarios.update((lista) =>
      lista.map((u) => (u.id === id ? { ...u, roles: accesos.roles, etiquetasRol: accesos.roles.map(etiquetaDeRol) } : u)),
    );
    this.seleccionadoId.set(null);
  }

  protected alCrear(usuario: UsuarioFila): void {
    this.creando.set(false);
    this.usuarios.update((lista) =>
      [...lista, usuario].sort((a, b) => a.nombreCompleto.localeCompare(b.nombreCompleto, 'es')),
    );
    this.pestana.set('usuarios');
    this.busqueda.set('');
  }

  protected tienePermiso(permisos: string[], codigo: string): boolean {
    return permisos.includes(codigo);
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.service.listar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }
}

/** Minúsculas y sin acentos, para que "lopez" encuentre a "López". */
function normalizar(texto: string): string {
  return texto.trim().toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
}
