import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { User, UserRole } from '../models/user';

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

interface JwtPayload {
  sub?: string;
  role?: UserRole;
  exp?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly tokenKey = 'restaurant_token';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(tap(response => this.storeToken(response.token)));
  }

  requestRegistration(request: RegisterRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/register/request`, request);
  }

  verifyRegistration(email: string, code: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/register/verify`, { email, code })
      .pipe(tap(response => this.storeToken(response.token)));
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRole(): UserRole | null {
    return this.getPayload()?.role ?? null;
  }

  getEmail(): string | null {
    return this.getPayload()?.sub ?? null;
  }

  isLoggedIn(): boolean {
    const payload = this.getPayload();
    if (!payload || !payload.exp) {
      return false;
    }

    const active = payload.exp * 1000 > Date.now();
    if (!active) {
      this.logout();
    }
    return active;
  }

  hasAnyRole(...roles: UserRole[]): boolean {
    const role = this.getRole();
    return role !== null && roles.includes(role);
  }

  defaultRoute(): string {
    switch (this.getRole()) {
      case 'ADMIN':
        return '/admin';
      case 'WAITER':
      case 'CHEF':
        return '/orders';
      case 'CUSTOMER':
      default:
        return '/menu';
    }
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private getPayload(): JwtPayload | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const payloadPart = token.split('.')[1];
      const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
      return JSON.parse(atob(padded)) as JwtPayload;
    } catch {
      return null;
    }
  }
}
