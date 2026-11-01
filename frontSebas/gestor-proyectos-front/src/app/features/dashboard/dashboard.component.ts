import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  protected readonly session = inject(SessionService);

  ngOnInit(): void {
    this.session.deseleccionarProyecto();
  }
}
