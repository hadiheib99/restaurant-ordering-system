import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  // Login and registration are public. Do not send a stale JWT with them.
  if (
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register')
  ) {
    return next(req);
  }

  const token = localStorage.getItem('restaurant_token');

  if (token) {
    const authenticatedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next(authenticatedRequest);
  }

  return next(req);
};
