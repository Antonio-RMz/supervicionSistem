import { Component, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FormularioLogin } from '../../../../core/auth/formulario-login';

/** Login del Portal de avance (PDF página 1). */
@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  protected readonly login = new FormularioLogin();
  protected readonly ayudaPassword = signal(false);
}
