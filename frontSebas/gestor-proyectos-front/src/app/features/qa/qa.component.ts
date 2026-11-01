import { Component } from '@angular/core';

@Component({
  selector: 'app-qa',
  standalone: true,
  templateUrl: './qa.component.html',
  styleUrl: './qa.component.scss',
})
export class QaComponent {
  readonly pageTitle = 'Control de Calidad';
  readonly pageIcon = 'bi-patch-check';
  readonly pageDesc = 'Gestión de QA y pruebas';
}
