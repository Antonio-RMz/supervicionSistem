import { Component, inject } from '@angular/core';
import { SpinnerService } from '../../services/spinner.service';

@Component({
  selector: 'app-spinner',
  standalone: true,
  template: `
    @if (spinner.loading()) {
      <div class="spinner-overlay">
        <div class="spinner-box">
          <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
            <span class="visually-hidden">Cargando...</span>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .spinner-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.35);
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .spinner-box {
      background: #fff;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 8px 32px rgba(0,0,0,0.18);
    }
  `],
})
export class SpinnerComponent {
  protected readonly spinner = inject(SpinnerService);
}
