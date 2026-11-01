import { Component } from '@angular/core';

@Component({
  selector: 'app-sol',
  standalone: true,
  templateUrl: './sol.component.html',
  styleUrl: './sol.component.scss',
})
export class SolComponent {
  readonly pageTitle = 'Solicitudes';
  readonly pageIcon = 'bi-file-earmark-text';
  readonly pageDesc = 'Gestión de solicitudes';
}
