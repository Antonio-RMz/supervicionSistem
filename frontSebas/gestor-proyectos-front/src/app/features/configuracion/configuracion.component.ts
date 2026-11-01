import { Component } from '@angular/core';

@Component({
  selector: 'app-configuracion',
  standalone: true,
  templateUrl: './configuracion.component.html',
  styleUrl: './configuracion.component.scss',
})
export class ConfiguracionComponent {
  readonly pageTitle = 'Configuración';
  readonly pageIcon = 'bi-gear-fill';
  readonly pageDesc = 'Configuración del sistema';
}
