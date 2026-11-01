import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';
import { APP_ROUTES } from '../constants/api.constants';

export const noAuthGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  if (session.isAuthenticated) {
    return router.createUrlTree([APP_ROUTES.DASHBOARD]);
  }

  return true;
};
