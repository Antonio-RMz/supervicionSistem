import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { mensajeDeError } from '../../../../shared/utils/errores';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { ProyectosDetalleService } from '../../../proyectos/data/proyectos-detalle.service';
import { FaseResumen } from '../../../proyectos/models/proyectos.models';

/** Tarjetas (por defecto) o tabla. */
type Vista = 'tarjetas' | 'tabla';

/** La vista elegida se recuerda en este navegador. */
const CLAVE_VISTA = 'gestor.lista-fases.vista';

/**
 * Menú Fases: las fases del plan del proyecto elegido arriba, para buscarlas y abrir cada una.
 * Mantenimiento no aparece: tiene su propia pantalla.
 */
@Component({
  selector: 'app-lista-fases-page',
  imports: [RouterLink, BarraAvance, ChipModulo, EtiquetaEstado],
  templateUrl: './lista-fases-page.html',
  styleUrl: './lista-fases-page.scss',
})
export class ListaFasesPage {
  protected readonly proyectos = inject(ProyectoActualStore);
  private readonly service = inject(ProyectosDetalleService);

  protected readonly fases = signal<FaseResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly texto = signal('');
  protected readonly estado = signal('');
  protected readonly vista = signal<Vista>(leerVista());

  /** Los estados que hay, para el filtro. */
  protected readonly estados = computed(() => [...new Map(this.fases().map((f) => [f.estado, f.etiqueta.texto])).entries()]);

  protected readonly filtradas = computed(() => {
    const texto = this.texto().trim().toLowerCase();
    return this.fases().filter((f) =>
      (!texto || f.titulo.toLowerCase().includes(texto) || (f.versionRelease ?? '').toLowerCase().includes(texto))
      && (!this.estado() || f.estado === this.estado()));
  });

  constructor() {
    // Al cambiar el proyecto de arriba se cargan sus fases y se limpian los filtros.
    effect(() => {
      const id = this.proyectos.actual()?.id;
      untracked(() => {
        if (id) this.cargar(id);
        else this.cargando.set(false);
      });
    });
  }

  protected limpiar(): void {
    this.texto.set('');
    this.estado.set('');
  }

  protected cambiarVista(vista: Vista): void {
    this.vista.set(vista);
    try {
      localStorage.setItem(CLAVE_VISTA, vista);
    } catch {
      // Sin almacenamiento (modo privado): solo se pierde la preferencia.
    }
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.limpiar();
    this.service.arbol(id).subscribe({
      next: (arbol) => {
        if (this.proyectos.actual()?.id !== id) return; // llegó tarde: ya se eligió otro proyecto
        this.fases.set(arbol.map((f) => f.fase).filter((f) => !f.mantenimiento));
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No pudimos cargar las fases.'));
        this.cargando.set(false);
      },
    });
  }
}

function leerVista(): Vista {
  try {
    return localStorage.getItem(CLAVE_VISTA) === 'tabla' ? 'tabla' : 'tarjetas';
  } catch {
    return 'tarjetas';
  }
}
