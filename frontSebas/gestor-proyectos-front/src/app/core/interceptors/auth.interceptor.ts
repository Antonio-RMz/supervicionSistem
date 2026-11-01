import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, finalize, throwError } from 'rxjs';
import { SessionService } from '../services/session.service';
import { SpinnerService } from '../../shared/services/spinner.service';
import { APP_ROUTES } from '../constants/api.constants';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const router = inject(Router);
  const spinner = inject(SpinnerService);

  const token = session.getToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  spinner.show();

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (true) {
        case error.status === 401:
          session.limpiarSesion();
          router.navigate([APP_ROUTES.LOGIN]);
          break;
        case error.status === 403:
          router.navigate([APP_ROUTES.FORBIDDEN]);
          break;
        case error.status === 0:
          // Sin conexión — el componente mostrará su propio mensaje de error
          break;
      }
      // Propaga el error al subscribe del componente para que
      // cada uno decida cómo mostrarlo (error state, toast, etc.)
      return throwError(() => error);
    }),
    finalize(() => spinner.hide()),
  );
};
