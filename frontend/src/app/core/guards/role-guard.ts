import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { UserRole } from '../models/user';
import { AuthService } from '../services/auth';

/**
 * Route guard that enforces authentication and role-specific Angular pages.
 *
 * Routes may provide an allowed `roles` array in route data. Unauthenticated
 * users are sent to login, while authenticated users with the wrong role are
 * redirected to their own role's default page.
 */
export const roleGuard: CanActivateFn = route => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const roles = (route.data?.['roles'] ?? []) as UserRole[];
  if (roles.length === 0 || authService.hasAnyRole(...roles)) {
    return true;
  }

  return router.createUrlTree([authService.defaultRoute()]);
};
