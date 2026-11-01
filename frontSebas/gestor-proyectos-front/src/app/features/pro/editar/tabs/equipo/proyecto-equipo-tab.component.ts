import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-proyecto-equipo-tab',
  standalone: true,
  imports: [],
  templateUrl: './proyecto-equipo-tab.component.html',
  styleUrl: './proyecto-equipo-tab.component.scss',
})
export class ProyectoEquipoTabComponent {
  @Input() proyectoId = 0;

  // TODO: inyectar ProyectoEquipoService cuando la API esté disponible
  // y cargar miembros con getByProyecto(this.proyectoId)
}
