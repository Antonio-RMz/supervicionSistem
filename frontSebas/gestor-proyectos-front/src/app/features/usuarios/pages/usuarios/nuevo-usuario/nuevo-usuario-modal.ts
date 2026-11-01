import { Component, computed, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Modal } from '../../../../../shared/components/modal/modal';
import { mensajeDeError } from '../../../../../shared/utils/errores';
import { ordenarRoles } from '../../../../../shared/utils/roles';
import { ProyectoResumen } from '../../../../gestor/models/proyecto.models';
import { UsuariosService } from '../../../data/usuarios.service';
import { Rol, UsuarioFila } from '../../../models/usuarios.models';

const LARGO_MINIMO_PASSWORD = 8;
/** Sin 0/O ni 1/l/I para que se pueda dictar sin confusiones. */
const CARACTERES = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789';

/** Alta de una persona del equipo (PDF pág. 8). La cuenta queda lista para entrar; no se envían invitaciones. */
@Component({
  selector: 'app-nuevo-usuario-modal',
  imports: [ReactiveFormsModule, Modal],
  templateUrl: './nuevo-usuario-modal.html',
  styleUrl: './nuevo-usuario-modal.scss',
})
export class NuevoUsuarioModal {
  private readonly service = inject(UsuariosService);

  readonly roles = input.required<Rol[]>();
  readonly proyectos = input.required<ProyectoResumen[]>();

  readonly cerrar = output<void>();
  readonly creado = output<UsuarioFila>();

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(80)]],
      apellidos: ['', Validators.maxLength(120)],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      password: ['', [Validators.required, Validators.minLength(LARGO_MINIMO_PASSWORD)]],
      confirmar: ['', Validators.required],
      cambiarPassword: [true],
      roles: [[] as string[], Validators.required],
      proyectoIds: [[] as number[]],
      activo: [true],
    },
    { validators: passwordsIguales },
  );

  protected readonly largoMinimo = LARGO_MINIMO_PASSWORD;
  protected readonly mostrarPassword = signal(false);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  private readonly valor = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  protected readonly rolesElegidos = computed(() => this.valor().roles ?? []);
  protected readonly proyectosElegidos = computed(() => {
    const ids = this.valor().proyectoIds ?? [];
    return this.proyectos().filter((p) => ids.includes(p.id));
  });
  protected readonly proyectosLibres = computed(() => {
    const ids = this.valor().proyectoIds ?? [];
    return this.proyectos().filter((p) => !ids.includes(p.id));
  });

  protected invalido(campo: 'nombre' | 'correo' | 'password' | 'confirmar' | 'roles'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected noCoinciden(): boolean {
    return this.form.hasError('noCoinciden') && this.form.controls.confirmar.touched;
  }

  protected generar(): void {
    const azar = crypto.getRandomValues(new Uint32Array(12));
    const password = Array.from(azar, (n) => CARACTERES[n % CARACTERES.length]).join('');
    this.form.patchValue({ password, confirmar: password });
    this.form.controls.password.markAsTouched();
    this.mostrarPassword.set(true);
  }

  protected alternarRol(codigo: string, marcado: boolean): void {
    const roles = this.form.controls.roles;
    roles.setValue(marcado ? ordenarRoles([...roles.value, codigo]) : roles.value.filter((r) => r !== codigo));
    roles.markAsTouched();
  }

  protected agregarProyecto(id: string): void {
    if (!id) return;
    const proyectos = this.form.controls.proyectoIds;
    proyectos.setValue([...proyectos.value, Number(id)]);
  }

  protected quitarProyecto(id: number): void {
    const proyectos = this.form.controls.proyectoIds;
    proyectos.setValue(proyectos.value.filter((p) => p !== id));
  }

  protected crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { confirmar: _, ...nuevo } = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.service.crear(nuevo).subscribe({
      next: (usuario) => this.creado.emit(usuario),
      error: (e) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(e, 'No pudimos crear el usuario. Intenta de nuevo.'));
      },
    });
  }
}

function passwordsIguales(form: AbstractControl): ValidationErrors | null {
  return form.get('password')?.value === form.get('confirmar')?.value ? null : { noCoinciden: true };
}
