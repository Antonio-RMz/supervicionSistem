import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { BarraAvance } from '../../../../shared/components/barra-avance/barra-avance';
import { ChipModulo } from '../../../../shared/components/chip-modulo/chip-modulo';
import { EtiquetaEstado } from '../../../../shared/components/etiqueta-estado/etiqueta-estado';
import { Tono } from '../../../../shared/models/etiqueta.model';
import { fechaCorta } from '../../../../shared/utils/fechas';
import { PortalClienteStore } from '../../data/portal-cliente.store';
import { MiProyecto } from '../../models/portal.models';

/** Colores que se van turnando entre las tarjetas. */
const TONOS: Tono[] = ['morado', 'turquesa', 'naranja', 'azul'];

@Component({
  selector: 'app-mis-proyectos-page',
  imports: [RouterLink, BarraAvance, ChipModulo, EtiquetaEstado],
  templateUrl: './mis-proyectos-page.html',
  styleUrl: './mis-proyectos-page.scss',
})
export class MisProyectosPage {
  private readonly router = inject(Router);
  protected readonly store = inject(PortalClienteStore);
  protected readonly usuario = inject(AuthService).usuario;

  protected readonly resumen = computed(() => {
    const total = this.store.proyectos().length;
    const empresa = this.usuario()?.clienteNombre ?? 'Tu empresa';
    return `${empresa} tiene ${total} ${total === 1 ? 'proyecto' : 'proyectos'} con nosotros. Elige uno para ver cómo va.`;
  });

  constructor() {
    // Si el cliente tiene un solo proyecto, esta pantalla se salta y entra directo a su Avance.
    this.store.cargar().then((proyectos) => {
      if (proyectos.length === 1) {
        this.router.navigate(['/portal/proyectos', proyectos[0].id], { replaceUrl: true });
      }
    }).catch(() => undefined);
  }

  protected tono(indice: number): Tono {
    return TONOS[indice % TONOS.length];
  }

  /** "31 jul 2026", o "Inicia 1 sep 2026" si la fase todavía no empieza. */
  protected proximaEntrega(proyecto: MiProyecto): string {
    const fase = proyecto.faseActual;
    if (!fase) return 'Sin entregas pendientes';
    return fase.estado === 'PROXIMA' ? `Inicia ${fechaCorta(fase.fechaInicio)}` : fechaCorta(fase.fechaFin);
  }

  protected reintentar(): void {
    this.store.cargar().catch(() => undefined);
  }
}
