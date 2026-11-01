import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();

  success(message: string, title = 'Éxito'): void {
    this.add('success', title, message);
  }

  error(message: string, title = 'Error'): void {
    this.add('error', title, message);
  }

  warning(message: string, title = 'Advertencia'): void {
    this.add('warning', title, message);
  }

  info(message: string, title = 'Información'): void {
    this.add('info', title, message);
  }

  remove(id: string): void {
    this._toasts.update((ts) => ts.filter((t) => t.id !== id));
  }

  private add(type: ToastType, title: string, message: string, duration = 5000): void {
    const id = crypto.randomUUID();
    this._toasts.update((ts) => [...ts, { id, type, title, message }]);
    setTimeout(() => this.remove(id), duration);
  }
}
