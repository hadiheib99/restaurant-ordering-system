import { HttpInterceptorFn } from '@angular/common/http';

/**
 * HTTP interceptor that attaches the stored JWT to protected API requests.
 *
 * Login and registration requests intentionally bypass token attachment so a
 * stale/expired token cannot interfere with starting a new authentication
 * session. Other requests receive an `Authorization: Bearer` header when a
 * token exists in local storage.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register')
  ) {
    return next(req);
  }

  const token = localStorage.getItem('restaurant_token');
  if (token) {
    const authenticatedRequest = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(authenticatedRequest);
  }

  return next(req);
};
