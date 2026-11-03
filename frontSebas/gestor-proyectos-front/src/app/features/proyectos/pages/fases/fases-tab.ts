import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Avatar } from '../../../../shared/components/avatar/avatar';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { etiquetaHistoria } from '../../../../shared/utils/tickets';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';
import { ProyectosDetalleService } from '../../data/proyectos-detalle.service';
import { FaseArbol } from '../../models/proyectos.models';

/** Lista: cada fase se despliega con sus historias. Tarjetas: solo las fases, para verlas de un vistazo. */
type Vista = 'lista' | 'tarjetas';

/** La vista elegida se recuerda en este navegador. */
const CLAVE_VISTA = 'gestor.fases.vista';

/**
 * Pestaña Fases e historias: una lista de las fases del plan; cada una se despliega y muestra sus historias.
 * También se puede ver como tarjetas, solo las fases. Las actividades de cada historia se ven al entrar a ella.
 */
@Component({
  selector: 'app-fases-tab',
  imports: [RouterLink, Avatar, BarraAvance, ChipModulo, EtiquetaEstado],
  templateUrl: './fases-tab.html',
  styleUrl: './fases-tab.scss',
})
export class FasesTab {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly service = inject(ProyectosDetalleService);

  private readonly arbol = signal<FaseArbol[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);
  /** Las fases desplegadas. */
  protected readonly abiertas = signal<ReadonlySet<number>>(new Set());
  protected readonly vista = signal<Vista>(leerVista());

  /** Mantenimiento tiene su propia pantalla: aquí solo las fases del plan. */
  protected readonly fases = computed(() => this.arbol().filter((f) => !f.fase.mantenimiento));

  protected readonly etiquetaHistoria = etiquetaHistoria;

  constructor() {
    effect(() => {
      const id = this.store.resumen()?.id;
      if (id) untracked(() => this.cargar(id));
    });
  }

  protected alternar(id: number): void {
    this.abiertas.update((abiertas) => {
      const nuevas = new Set(abiertas);
      if (!nuevas.delete(id)) nuevas.add(id);
      return nuevas;
    });
  }

  protected cambiarVista(vista: Vista): void {
    this.vista.set(vista);
    try {
      localStorage.setItem(CLAVE_VISTA, vista);
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
  }

  /** Igual que en el back: las fases cerradas o canceladas ya no reciben historias. */
  protected aceptaHistorias(estado: string): boolean {
    return ['PLANEADA', 'EN_CURSO', 'PAUSADA'].includes(estado);
  }

  private cargar(id: number): void {
    this.service.arbol(id).subscribe({
      next: (arbol) => {
        this.arbol.set(arbol);
        this.cargando.set(false);
        // Al entrar se despliega la fase en curso (o la última).
        if (!this.abiertas().size) {
          const fases = this.fases();
          const actual = fases.find((f) => f.fase.id === this.store.resumen()?.faseActual?.id) ?? fases.at(-1);
          if (actual) this.abiertas.set(new Set([actual.fase.id]));
        }
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }
}

function leerVista(): Vista {
  try {
    // Por defecto, tarjetas; la lista solo si se eligió.
    return localStorage.getItem(CLAVE_VISTA) === 'lista' ? 'lista' : 'tarjetas';
  } catch {
    return 'tarjetas';
  }
}
