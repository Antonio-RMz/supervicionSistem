import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SpinnerService {
  private activeRequests = 0;
  private readonly _loading = signal(false);

  readonly loading = this._loading.asReadonly();

  show(): void {
    this.activeRequests++;
    this._loading.set(true);
  }

  hide(): void {
    this.activeRequests = Math.max(0, this.activeRequests - 1);
    if (this.activeRequests === 0) this._loading.set(false);
  }
}
