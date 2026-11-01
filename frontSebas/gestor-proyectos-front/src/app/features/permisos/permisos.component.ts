import { Component } from '@angular/core';

@Component({
  selector: 'app-permisos',
  standalone: true,
  templateUrl: './permisos.component.html',
  styleUrl: './permisos.component.scss',
})
export class PermisosComponent {
  readonly pageTitle = 'Permisos';
  readonly pageIcon = 'bi-key-fill';
  readonly pageDesc = 'Control de permisos del sistema';
}
