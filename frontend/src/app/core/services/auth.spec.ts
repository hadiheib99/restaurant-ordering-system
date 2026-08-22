import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('stores the token after login', () => {
    service.login({ email: 'customer@example.com', password: 'Password123' }).subscribe();

    const request = http.expectOne('http://localhost:8080/api/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({ token: 'header.payload.signature' });

    expect(service.getToken()).toBe('header.payload.signature');
  });

  it('registers a customer and stores the returned token', () => {
    const payload = {
      username: 'customer2',
      password: 'Password123',
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@example.com',
      phone: '0501234567'
    };

    service.register(payload).subscribe();

    const request = http.expectOne('http://localhost:8080/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ token: 'new-token' });

    expect(service.getToken()).toBe('new-token');
  });

  it('logs out by removing the token', () => {
    localStorage.setItem('restaurant_token', 'token');
    service.logout();
    expect(service.getToken()).toBeNull();
  });

  it('returns the correct default route for a JWT role', () => {
    const payload = btoa(JSON.stringify({ role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('restaurant_token', `header.${payload}.signature`);

    expect(service.defaultRoute()).toBe('/admin');
    expect(service.isLoggedIn()).toBe(true);
  });
});
