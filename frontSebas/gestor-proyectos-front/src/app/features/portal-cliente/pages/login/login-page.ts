import { Component, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FormularioLogin } from '../../../../core/auth/formulario-login';
import { LogoInova } from '../../../../shared/components/logo-inova/logo-inova';
import { FondoCircuito } from '../../../../shared/components/fondo-circuito/fondo-circuito';

/** Login del Portal de avance (PDF página 1). */
@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink, LogoInova, FondoCircuito],
  templateUrl: './login-page.html',
  styleUrl: '../../../../core/auth/login.scss',
})
export class LoginPage {
  protected readonly login = new FormularioLogin();
  protected readonly ayudaPassword = signal(false);
}
