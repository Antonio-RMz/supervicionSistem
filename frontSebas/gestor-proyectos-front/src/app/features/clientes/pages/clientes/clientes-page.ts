import { Component, computed, inject, signal } from '@angular/core';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Etiqueta } from '../../../../shared/models/etiqueta.model';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ClientesService } from '../../data/clientes.service';
import { Cliente, FiltroEstado } from '../../models/clientes.models';
import { ClienteModal } from './cliente-modal/cliente-modal';

const ACTIVO: Etiqueta = { texto: 'Activo', tono: 'verde' };
const INACTIVO: Etiqueta = { texto: 'Inactivo', tono: 'gris' };

/** Clientes (PDF pág. 9). Todo el equipo los ve; solo quien tiene "cliente.gestionar" los cambia. */
@Component({
  selector: 'app-clientes-page',
  imports: [Avatar, EtiquetaEstado, ClienteModal],
  templateUrl: './clientes-page.html',
  styleUrl: './clientes-page.scss',
  // Un clic en cualquier otro lado cierra el menú "⋯" abierto.
  host: { '(document:click)': 'cerrarMenu()' },
})
export class ClientesPage {
  private readonly service = inject(ClientesService);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly puedeGestionar = signal(false);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);
  protected readonly errorAccion = signal<string | null>(null);

  protected readonly busqueda = signal('');
  protected readonly estado = signal<FiltroEstado>('todos');
  protected readonly menuAbierto = signal<number | null>(null);
  /** Desactivar pide un segundo clic en el mismo menú. */
  protected readonly confirmandoDesactivar = signal(false);

  /** undefined = modal cerrado; null = cliente nuevo; un cliente = editarlo */
  protected readonly enModal = signal<Cliente | null | undefined>(undefined);

  protected readonly filtrados = computed(() => {
    const texto = normalizar(this.busqueda());
    const estado = this.estado();
    return this.clientes().filter((c) => {
      if (estado === 'activos' && !c.activo) return false;
      if (estado === 'inactivos' && c.activo) return false;
      if (!texto) return true;
      return [c.nombreCorto, c.razonSocial, c.ciudad, c.contactoNombre, c.contactoCorreo]
        .some((campo) => normalizar(campo).includes(texto));
    });
  });

  protected readonly activo = ACTIVO;
  protected readonly inactivo = INACTIVO;

  constructor() {
    this.service.listar().subscribe({
      next: ({ clientes, puedeGestionar }) => {
        this.clientes.set(clientes);
        this.puedeGestionar.set(puedeGestionar);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  protected alternarMenu(id: number, evento: Event): void {
    evento.stopPropagation();
    this.confirmandoDesactivar.set(false);
    this.menuAbierto.set(this.menuAbierto() === id ? null : id);
  }

  protected cerrarMenu(): void {
    this.menuAbierto.set(null);
    this.confirmandoDesactivar.set(false);
  }

  protected editar(cliente: Cliente): void {
    this.cerrarMenu();
    this.enModal.set(cliente);
  }

  protected cambiarEstado(cliente: Cliente): void {
    if (cliente.activo && !this.confirmandoDesactivar()) {
      this.confirmandoDesactivar.set(true);
      return;
    }
    this.cerrarMenu();
    this.errorAccion.set(null);
    this.service.cambiarEstado(cliente.id, !cliente.activo).subscribe({
      next: (actualizado) => this.reemplazar(actualizado),
      error: (e) => this.errorAccion.set(mensajeDeError(e, 'No pudimos cambiar el estado del cliente.')),
    });
  }

  protected alGuardar(guardado: Cliente): void {
    this.enModal.set(undefined);
    if (this.clientes().some((c) => c.id === guardado.id)) {
      this.reemplazar(guardado);
    } else {
      this.clientes.update((lista) => ordenar([...lista, guardado]));
    }
  }

  private reemplazar(cliente: Cliente): void {
    this.clientes.update((lista) => ordenar(lista.map((c) => (c.id === cliente.id ? cliente : c))));
  }
}

/** Como el back: activos primero, luego por nombre. */
function ordenar(clientes: Cliente[]): Cliente[] {
  return [...clientes].sort(
    (a, b) => Number(b.activo) - Number(a.activo) || a.nombreCorto.localeCompare(b.nombreCorto, 'es'),
  );
}

/** Minúsculas y sin acentos, para que "bajio" encuentre "Bajío". */
function normalizar(texto: string): string {
  return texto.trim().toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
}
