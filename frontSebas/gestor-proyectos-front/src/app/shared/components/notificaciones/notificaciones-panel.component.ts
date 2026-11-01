import { Component, input, output } from '@angular/core';
import { Notificacion, NotificacionTipo } from '../../models/notificacion.model';

@Component({
  selector: 'app-notificaciones-panel',
  standalone: true,
  imports: [],
  templateUrl: './notificaciones-panel.component.html',
  styleUrl: './notificaciones-panel.component.scss',
})
export class NotificacionesPanelComponent {
  readonly open            = input(false);
  readonly notificaciones  = input<Notificacion[]>([]);

  readonly cerrar = output<void>();

  protected iconoPorTipo(tipo: NotificacionTipo): string {
    const map: Record<NotificacionTipo, string> = {
      proyecto: 'bi-folder-fill',
      estado:   'bi-arrow-repeat',
      usuario:  'bi-person-fill',
      sistema:  'bi-gear-fill',
    };
    return map[tipo];
  }

  protected colorPorTipo(tipo: NotificacionTipo): string {
    const map: Record<NotificacionTipo, string> = {
      proyecto: 'icon-proyecto',
      estado:   'icon-estado',
      usuario:  'icon-usuario',
      sistema:  'icon-sistema',
    };
    return map[tipo];
  }
}
