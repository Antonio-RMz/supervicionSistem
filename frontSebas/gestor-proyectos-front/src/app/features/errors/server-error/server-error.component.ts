import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-server-error',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="error-page">
      <div class="error-code text-secondary">500</div>
      <div class="error-icon"><i class="bi bi-exclamation-triangle-fill text-secondary"></i></div>
      <h1 class="error-title">Error Interno del Servidor</h1>
      <p class="error-desc">Ocurrió un error inesperado. Nuestro equipo ha sido notificado. Por favor intente más tarde.</p>
      <div class="d-flex gap-2 flex-wrap justify-content-center">
        <button class="btn btn-secondary btn-lg" onclick="location.reload()">
          <i class="bi bi-arrow-clockwise me-2"></i>Reintentar
        </button>
        <a routerLink="/dashboard" class="btn btn-outline-secondary btn-lg">
          <i class="bi bi-house me-2"></i>Dashboard
        </a>
      </div>
    </div>
  `,
  styles: [`
    .error-page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; background: #f0f2f5; }
    .error-code { font-size: 7rem; font-weight: 900; line-height: 1; opacity: 0.15; }
    .error-icon { font-size: 3.5rem; margin: -1.5rem 0 1rem; }
    .error-title { font-size: 1.75rem; font-weight: 700; color: #14172b; margin-bottom: .75rem; }
    .error-desc { color: #6c757d; font-size: 1rem; max-width: 440px; line-height: 1.7; margin-bottom: 2rem; }
  `],
})
export class ServerErrorComponent {}
