import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-proyecto-configuracion-tab',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './proyecto-configuracion-tab.component.html',
  styleUrl: './proyecto-configuracion-tab.component.scss',
})
export class ProyectoConfiguracionTabComponent {
  @Input({ required: true }) form!: FormGroup;

  protected hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
}
