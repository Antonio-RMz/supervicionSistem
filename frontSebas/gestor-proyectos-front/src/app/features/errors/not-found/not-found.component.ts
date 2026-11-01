import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="error-page">
      <div class="error-code text-primary">404</div>
      <div class="error-icon"><i class="bi bi-search text-primary"></i></div>
      <h1 class="error-title">Página No Encontrada</h1>
      <p class="error-desc">La página que busca no existe o ha sido movida. Verifique la URL o regrese al inicio.</p>
      <a routerLink="/dashboard" class="btn btn-primary btn-lg">
        <i class="bi bi-house me-2"></i>Ir al Dashboard
      </a>
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
export class NotFoundComponent {}
