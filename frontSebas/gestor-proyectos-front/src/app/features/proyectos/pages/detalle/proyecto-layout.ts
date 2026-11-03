import { Component, effect, inject, input, untracked } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { IconoProyecto } from '../../components/icono-proyecto/icono-proyecto';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { ProyectoActualStore } from '../../../gestor/data/proyecto-actual.store';
import { ProyectoDetalleStore } from '../../data/proyecto-detalle.store';

/** Encabezado y pestañas del detalle de un proyecto (PDF págs. 14-15). Cada pestaña es una ruta hija. */
@Component({
  selector: 'app-proyecto-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, IconoProyecto, BarraAvance, ChipModulo, EtiquetaEstado],
  providers: [ProyectoDetalleStore],
  templateUrl: './proyecto-layout.html',
  styleUrl: './proyecto-layout.scss',
})
export class ProyectoLayout {
  protected readonly store = inject(ProyectoDetalleStore);
  private readonly proyectoActual = inject(ProyectoActualStore);

  /** :id de la ruta */
  readonly id = input.required<string>();

  constructor() {
    effect(() => {
      const id = Number(this.id());
      untracked(() => {
        this.store.cargar(id);
        // El selector de arriba muestra el proyecto que estás viendo.
        this.proyectoActual.elegir(id);
      });
    });
  }
}
