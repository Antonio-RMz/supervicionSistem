import { Component, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FormularioLogin } from '../../../../core/auth/formulario-login';

/** Acceso del equipo al Gestor de Proyectos (PDF página 4). */
@Component({
  selector: 'app-login-equipo-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-equipo-page.html',
  styleUrl: './login-equipo-page.scss',
})
export class LoginEquipoPage {
  protected readonly login = new FormularioLogin();
  protected readonly ayudaPassword = signal(false);
  protected readonly cadena = ['Proyecto', 'Fase', 'Historia', 'Ticket'];
}
