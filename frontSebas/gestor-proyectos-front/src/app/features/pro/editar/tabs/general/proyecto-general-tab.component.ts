import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TipoProyecto, EstadoProyecto, PrioridadProyecto } from '../../../../../core/models/configuracion.model';

@Component({
  selector: 'app-proyecto-general-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './proyecto-general-tab.component.html',
  styleUrl: './proyecto-general-tab.component.scss',
})
export class ProyectoGeneralTabComponent {
  @Input({ required: true }) form!: FormGroup;
  @Input() cargandoCatalogos = false;
  @Input() tiposProyecto:       TipoProyecto[]      = [];
  @Input() estadosProyecto:     EstadoProyecto[]     = [];
  @Input() prioridadesProyecto: PrioridadProyecto[]  = [];

  protected hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
}
