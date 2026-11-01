import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-proyecto-extras-tab',
  standalone: true,
  imports: [],
  templateUrl: './proyecto-extras-tab.component.html',
  styleUrl: './proyecto-extras-tab.component.scss',
})
export class ProyectoExtrasTabComponent {
  @Input() proyectoId = 0;

  // TODO: inyectar servicios de tablas relacionadas cuando las APIs estén disponibles
}
