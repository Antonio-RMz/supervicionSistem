import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from '../../../../../shared/components/modal/modal';
import { mensajeDeError } from '../../../../../shared/utils/errores';
import { ClientesService } from '../../../data/clientes.service';
import { Cliente, GIROS } from '../../../models/clientes.models';

/** Persona moral (12) o física (13): letras, fecha AAMMDD y homoclave. Igual que en el back. */
const RFC = /^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$/i;

/** Nuevo cliente (PDF pág. 10). Con un cliente en `cliente` sirve para editarlo. */
@Component({
  selector: 'app-cliente-modal',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './cliente-modal.html',
  styleUrl: './cliente-modal.scss',
})
export class ClienteModal implements OnInit {
  private readonly service = inject(ClientesService);

  /** null = cliente nuevo */
  readonly cliente = input<Cliente | null>(null);

  readonly cerrar = output<void>();
  readonly guardado = output<Cliente>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    nombreCorto: ['', Validators.maxLength(80)],
    rfc: ['', Validators.pattern(RFC)],
    giro: [''],
    ciudad: ['', Validators.maxLength(80)],
    contactoNombre: ['', Validators.maxLength(120)],
    contactoCorreo: ['', [Validators.email, Validators.maxLength(120)]],
    contactoTelefono: ['', Validators.maxLength(20)],
    notas: [''],
    activo: [true],
  });

  protected readonly giros = GIROS;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly editando = computed(() => this.cliente() !== null);

  ngOnInit(): void {
    const cliente = this.cliente();
    if (cliente) this.form.setValue({
      razonSocial: cliente.razonSocial,
      nombreCorto: cliente.nombreCorto,
      rfc: cliente.rfc,
      giro: cliente.giro,
      ciudad: cliente.ciudad,
      contactoNombre: cliente.contactoNombre,
      contactoCorreo: cliente.contactoCorreo,
      contactoTelefono: cliente.contactoTelefono,
      notas: cliente.notas,
      activo: cliente.activo,
    });
  }

  protected invalido(campo: 'razonSocial' | 'rfc' | 'contactoNombre' | 'contactoCorreo'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const datos = this.form.getRawValue();
    const cliente = this.cliente();
    this.enviando.set(true);
    this.error.set(null);
    (cliente ? this.service.actualizar(cliente.id, datos) : this.service.crear(datos)).subscribe({
      next: (guardado) => this.guardado.emit(guardado),
      error: (e) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos guardar el cliente. Intenta de nuevo.'));
      },
    });
  }
}
