import { Component } from '@angular/core';

@Component({
  selector: 'app-sesiones',
  standalone: true,
  templateUrl: './sesiones.component.html',
  styleUrl: './sesiones.component.scss',
})
export class SesionesComponent {
  readonly pageTitle = 'Sesiones Activas';
  readonly pageIcon = 'bi-display';
  readonly pageDesc = 'Monitoreo de sesiones activas';
}
