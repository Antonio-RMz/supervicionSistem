import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { ProyectoService } from '../../../../../core/services/proyecto.service';
import { ResponsableDisponibleResponse } from '../../../../../core/models/ticket.model';

const AVATAR_COLORS = [
  '#4361ee', '#7209b7', '#f72585', '#3a0ca3',
  '#0d9488', '#e67e22', '#ef4444', '#8e44ad',
  '#0891b2', '#65a30d',
];

const ROL_BADGE: Record<string, { bg: string; color: string }> = {
  'LÍDER_DE_PROYECTO':  { bg: '#ede9fe', color: '#7c3aed' },
  'ANALISTA_COMERCIAL': { bg: '#fef3c7', color: '#d97706' },
  'ANALISTA':           { bg: '#fef3c7', color: '#d97706' },
};
const ROL_BADGE_DEFAULT = { bg: '#f3f4f6', color: '#374151' };

@Component({
  selector: 'app-asignar-analista-modal',
  standalone: true,
  imports: [],
  templateUrl: './asignar-analista-modal.component.html',
  styleUrl: './asignar-analista-modal.component.scss',
})
export class AsignarAnalistaModalComponent implements OnInit {
  readonly ticketId   = input.required<number>();
  readonly proyectoId = input.required<number>();

  readonly cerrar                  = output<void>();
  readonly responsableSeleccionado = output<number>();

  private readonly proyectoSvc = inject(ProyectoService);

  readonly responsables   = signal<ResponsableDisponibleResponse[]>([]);
  readonly cargando       = signal(true);
  readonly errorCarga     = signal<string | null>(null);
  readonly busqueda       = signal('');
  readonly seleccionadoId = signal<number | null>(null);

  readonly listaFiltrada = computed(() => {
    const q    = this.busqueda().toLowerCase().trim();
    const todos = this.responsables();
    if (!q) return todos;
    return todos.filter(r =>
      r.nombreCompleto.toLowerCase().includes(q) ||
      r.correo.toLowerCase().includes(q)         ||
      r.nombreRol.toLowerCase().includes(q),
    );
  });

  ngOnInit(): void {
    const pId = this.proyectoId();
    console.log('Datos recibidos por el modal:', { ticketId: this.ticketId(), proyectoId: pId });

    if (!pId || pId <= 0) {
      console.error('proyectoId inválido:', pId);
      this.errorCarga.set('No fue posible identificar el proyecto del ticket.');
      this.cargando.set(false);
      return;
    }

    this.cargarResponsables();
  }

  protected cargarResponsables(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.seleccionadoId.set(null);

    const pId = this.proyectoId();

    console.log(
      'URL responsables:',
      `/api/v1/proyectos/${pId}/responsables-disponibles`,
    );

    this.proyectoSvc.obtenerResponsablesDisponibles(pId).subscribe({
      next: lista => {
        console.group('Carga de responsables');
        console.log('Proyecto:', pId);
        console.log('Cantidad de responsables:', lista.length);
        console.table(lista);
        console.groupEnd();

        this.responsables.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.errorCarga.set('No fue posible obtener los responsables disponibles.');
        this.cargando.set(false);
      },
    });
  }

  protected onOverlayClick(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('aa-overlay')) this.cerrar.emit();
  }

  protected iniciales(nombreCompleto: string): string {
    const partes = nombreCompleto.trim().split(/\s+/);
    return partes.length >= 2
      ? `${partes[0][0]}${partes[1][0]}`.toUpperCase()
      : nombreCompleto.substring(0, 2).toUpperCase();
  }

  protected avatarColor(id: number): string {
    return AVATAR_COLORS[id % AVATAR_COLORS.length];
  }

  protected rolBadge(codigoRol: string): { bg: string; color: string } {
    return ROL_BADGE[codigoRol] ?? ROL_BADGE_DEFAULT;
  }

  protected setBusqueda(e: Event): void {
    this.busqueda.set((e.target as HTMLInputElement).value);
  }

  protected confirmar(): void {
    const id = this.seleccionadoId();
    if (id === null) return;
    this.responsableSeleccionado.emit(id);
  }
}
