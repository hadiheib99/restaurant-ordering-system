import { HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';

import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('adds the bearer token to protected API requests', () => {
    localStorage.setItem('restaurant_token', 'jwt-token');
    const request = new HttpRequest('GET', 'http://localhost:8080/api/orders');
    let forwarded: HttpRequest<unknown> | undefined;

    authInterceptor(request, req => {
      forwarded = req;
      return of(new HttpResponse());
    }).subscribe();

    expect(forwarded?.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('does not attach a stale token to login', () => {
    localStorage.setItem('restaurant_token', 'old-token');
    const request = new HttpRequest('POST', 'http://localhost:8080/api/auth/login', {});
    let forwarded: HttpRequest<unknown> | undefined;

    authInterceptor(request, req => {
      forwarded = req;
      return of(new HttpResponse());
    }).subscribe();

    expect(forwarded?.headers.has('Authorization')).toBe(false);
  });

  it('does not attach a stale token to registration', () => {
    localStorage.setItem('restaurant_token', 'old-token');
    const request = new HttpRequest('POST', 'http://localhost:8080/api/auth/register', {});
    let forwarded: HttpRequest<unknown> | undefined;

    authInterceptor(request, req => {
      forwarded = req;
      return of(new HttpResponse());
    }).subscribe();

    expect(forwarded?.headers.has('Authorization')).toBe(false);
  });
});
