import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { Observable } from 'rxjs';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { avisoFlotante } from '../../../../shared/components/avisos/avisos.service';
import { Modal } from '../../../../shared/components/modal/modal';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { EquipoService } from '../../data/equipo.service';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';
import { Candidato, EquipoProyecto, MiembroEquipo } from '../../models/equipo.models';

/**
 * Pestaña Equipo (PDF pág. 34): quién trabaja en el proyecto, con qué rol y cuánta carga tiene.
 * El Líder (o un Admin) agrega miembros, los quita y reasigna sus tickets si alguien se ausenta.
 */
@Component({
  selector: 'app-equipo-tab',
  imports: [RouterLink, Avatar, Modal],
  host: { '(document:click)': 'menuAbierto.set(null)' },
  templateUrl: './equipo-tab.html',
  styleUrl: './equipo-tab.scss',
})
export class EquipoTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly service = inject(EquipoService);
  private readonly usuario = inject(AuthService).usuario;
  /** "Gestionar" lleva a Usuarios y roles, que solo ve el Admin. */
  protected readonly esAdmin = computed(() => this.usuario()?.roles.includes('ADMIN') ?? false);

  protected readonly equipo = signal<EquipoProyecto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  /** El resultado de cada acción, como aviso flotante. */
  protected readonly aviso = avisoFlotante();
  protected readonly enviando = signal(false);
  protected readonly menuAbierto = signal<number | null>(null);

  /** Agregar miembro */
  protected readonly agregando = signal(false);
  protected readonly candidatos = signal<Candidato[]>([]);
  protected readonly elegido = signal<number | null>(null);

  /** Reasignar tickets de alguien */
  protected readonly reasignando = signal<MiembroEquipo | null>(null);
  protected readonly responsableNuevo = signal<number | null>(null);
  protected readonly testerNuevo = signal<number | null>(null);
  protected readonly otros = computed(() =>
    (this.equipo()?.miembros ?? []).filter((m) => m.id !== this.reasignando()?.id),
  );

  private proyectoId = 0;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargar(id));
    });
  }

  protected alternarMenu(id: number, evento: Event): void {
    evento.stopPropagation();
    this.menuAbierto.set(this.menuAbierto() === id ? null : id);
  }

  protected abrirAgregar(): void {
    this.aviso.set(null);
    this.elegido.set(null);
    this.service.candidatos(this.proyectoId).subscribe({
      next: (c) => {
        this.candidatos.set(c);
        this.agregando.set(true);
      },
      error: (e) => this.aviso.set({ texto: mensajeDeError(e, 'No pudimos cargar a las personas del equipo.'), error: true }),
    });
  }

  protected agregar(): void {
    const id = this.elegido();
    if (id === null) return;
    const nombre = this.candidatos().find((c) => c.id === id)?.nombreCompleto ?? '';
    this.ejecutar(this.service.agregar(this.proyectoId, id), `${nombre} se agregó al proyecto.`, () => this.agregando.set(false));
  }

  protected quitar(m: MiembroEquipo): void {
    this.menuAbierto.set(null);
    this.ejecutar(this.service.quitar(this.proyectoId, m.id), `${m.nombreCompleto} salió del proyecto.`);
  }

  protected abrirReasignar(m: MiembroEquipo): void {
    this.menuAbierto.set(null);
    this.aviso.set(null);
    this.responsableNuevo.set(null);
    this.testerNuevo.set(null);
    this.reasignando.set(m);
  }

  protected reasignar(): void {
    const m = this.reasignando();
    if (!m || (this.responsableNuevo() === null && this.testerNuevo() === null)) return;
    this.enviando.set(true);
    this.service.reasignar(this.proyectoId, m.id, this.responsableNuevo(), this.testerNuevo()).subscribe({
      next: (n) => {
        this.enviando.set(false);
        this.reasignando.set(null);
        this.aviso.set({ texto: `Se reasignaron ${n} ${n === 1 ? 'ticket' : 'tickets'} de ${m.nombre}.`, error: false });
        this.cargar(this.proyectoId);
      },
      error: (e) => {
        this.enviando.set(false);
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos reasignar los tickets.'), error: true });
        this.reasignando.set(null);
      },
    });
  }

  protected numero(valor: string): number | null {
    return valor ? Number(valor) : null;
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
        this.store.recargar(); // el número de la pestaña "Equipo · 3"
      },
      error: (e) => {
        this.enviando.set(false);
        alTerminar?.();
        this.aviso.set({ texto: mensajeDeError(e, 'No pudimos actualizar el equipo.'), error: true });
      },
    });
  }

  private cargar(id: number): void {
    this.proyectoId = id;
    this.service.equipo(id).subscribe({
      next: (e) => {
        this.equipo.set(e);
        this.cargando.set(false);
      },
      error: (e) => {
        this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar el equipo.'));
        this.cargando.set(false);
      },
    });
  }
}
