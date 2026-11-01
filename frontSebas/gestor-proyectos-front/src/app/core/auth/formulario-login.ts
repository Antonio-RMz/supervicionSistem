import { HttpErrorResponse } from '@angular/common/http';
import { inject, signal } from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { rutaInicio } from './auth.guards';
import { AuthService } from './auth.service';

/**
 * Lógica del formulario de login, compartida por el login del equipo y el del cliente.
 * Cada pantalla solo pone su diseño. Se crea en un campo del componente: `new FormularioLogin()`.
 */
export class FormularioLogin {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = inject(NonNullableFormBuilder).group({
    correo: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    recordarme: [false],
  });

  readonly mostrarPassword = signal(false);
  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { correo, password, recordarme } = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);

    this.auth.iniciarSesion(correo, password, recordarme).subscribe({
      // Cada quien a su inicio: el equipo al Dashboard, el cliente a su portal.
      next: (sesion) => this.router.navigateByUrl(rutaInicio(sesion.usuario)),
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.error.set(
          e.status === 401 ? 'Correo o contraseña incorrectos.' : 'No pudimos conectarnos. Intenta de nuevo en un momento.',
        );
      },
    });
  }

  invalido(campo: 'correo' | 'password'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }
}
