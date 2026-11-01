import { ErrorHandler, inject, Injectable, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { APP_ROUTES } from '../constants/api.constants';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private readonly router = inject(Router);
  private readonly zone = inject(NgZone);

  handleError(error: unknown): void {
    console.error('[GlobalErrorHandler]', error);

    // Los errores HTTP los manejan el interceptor y los propios componentes.
    // Si GlobalErrorHandler los captura también, pisa el manejo del componente
    // y navega a /500 aunque el subscribe tenga su propio error handler.
    if (error instanceof HttpErrorResponse) return;

    this.zone.run(() => this.router.navigate([APP_ROUTES.SERVER_ERROR]));
  }
}
