import { Component, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FormularioLogin } from '../../../../core/auth/formulario-login';
import { LogoInova } from '../../../../shared/components/logo-inova/logo-inova';
import { FondoCircuito } from '../../../../shared/components/fondo-circuito/fondo-circuito';

/** Acceso del equipo al Gestor de Proyectos (PDF página 4). */
@Component({
  selector: 'app-login-equipo-page',
  imports: [ReactiveFormsModule, RouterLink, LogoInova, FondoCircuito],
  templateUrl: './login-equipo-page.html',
  styleUrl: '../../../../core/auth/login.scss',
})
export class LoginEquipoPage {
  protected readonly login = new FormularioLogin();
  protected readonly ayudaPassword = signal(false);
}
