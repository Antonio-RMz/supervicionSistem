import { Component } from '@angular/core';

@Component({
  selector: 'app-his',
  standalone: true,
  templateUrl: './his.component.html',
  styleUrl: './his.component.scss',
})
export class HisComponent {
  readonly pageTitle = 'Historial';
  readonly pageIcon = 'bi-clock-history';
  readonly pageDesc = 'Historial de actividades';
}
