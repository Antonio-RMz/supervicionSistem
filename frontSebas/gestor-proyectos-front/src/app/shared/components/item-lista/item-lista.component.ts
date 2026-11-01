import { Component, input, output } from '@angular/core';
import { ListaItem } from '../../models/lista-item.model';

@Component({
  selector: 'app-item-lista',
  standalone: true,
  templateUrl: './item-lista.component.html',
  styleUrl: './item-lista.component.scss',
})
export class ItemListaComponent {
  readonly items       = input.required<ListaItem[]>();
  readonly icono       = input('bi-flag-fill');
  readonly puedeCrear  = input(false);
  readonly labelNuevo  = input('Nuevo');
  readonly labelVacio  = input('Esta sección aún no tiene elementos');

  readonly itemClick = output<ListaItem>();
  readonly nuevo     = output<void>();
}
