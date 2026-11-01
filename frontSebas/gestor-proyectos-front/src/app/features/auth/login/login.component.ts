import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { APP_ROUTES } from '../../../core/constants/api.constants';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showPassword = signal(false);
  protected readonly currentYear = new Date().getFullYear();

  protected readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control?.invalid && (control.dirty || control.touched));
  }

  protected togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.loginForm.value;

    this.auth.login({ email: email!, password: password! }).subscribe({
      next: () => this.router.navigate([APP_ROUTES.DASHBOARD]),
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Credenciales incorrectas. Verifique su email y contraseña.');
        } else if (err.status === 0) {
          this.errorMessage.set('No se puede conectar al servidor. Intente más tarde.');
        } else {
          this.errorMessage.set('Ocurrió un error inesperado. Intente nuevamente.');
        }
      },
    });
  }
}
