import { Component, input, output } from '@angular/core';
import { Fase } from '../../../../core/models/proyecto.model';
import { EstadoBadgeComponent } from '../../../../shared/components/estado-badge/estado-badge.component';

@Component({
  selector: 'app-fase-list',
  standalone: true,
  imports: [EstadoBadgeComponent],
  templateUrl: './fase-list.component.html',
  styleUrl: './fase-list.component.scss',
})
export class FaseListComponent {
  readonly fases        = input.required<Fase[]>();
  readonly puedeEditar  = input(false);
  readonly nuevaFase    = output<void>();
  readonly accion       = output<{ fase: Fase; tipo: 'editar' | 'eliminar' }>();

  protected menuAbierto: number | null = null;

  protected toggleMenu(id: number): void {
    this.menuAbierto = this.menuAbierto === id ? null : id;
  }

  protected cerrarMenu(): void {
    this.menuAbierto = null;
  }
}
