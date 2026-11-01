import { Component, inject } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9000;">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="toast show align-items-center border-0 mb-2"
          [class]="toastClass(toast.type)"
          role="alert"
        >
          <div class="d-flex">
            <div class="toast-body d-flex align-items-start gap-2">
              <i class="bi fs-5 flex-shrink-0" [class]="iconClass(toast.type)"></i>
              <div>
                <div class="fw-semibold">{{ toast.title }}</div>
                <div class="small opacity-90">{{ toast.message }}</div>
              </div>
            </div>
            <button
              type="button"
              class="btn-close btn-close-white me-2 m-auto"
              (click)="toastService.remove(toast.id)"
            ></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);

  toastClass(type: string): string {
    const map: Record<string, string> = {
      success: 'text-bg-success',
      error: 'text-bg-danger',
      warning: 'text-bg-warning',
      info: 'text-bg-info',
    };
    return map[type] ?? 'text-bg-secondary';
  }

  iconClass(type: string): string {
    const map: Record<string, string> = {
      success: 'bi-check-circle-fill',
      error: 'bi-x-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
      info: 'bi-info-circle-fill',
    };
    return map[type] ?? 'bi-bell-fill';
  }
}
