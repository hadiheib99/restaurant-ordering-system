import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { User, UserRole } from '../models/user';

/** Data required to register a new customer account. */
export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

/** JWT claims used by the Angular client for role and expiration checks. */
interface JwtPayload {
  sub?: string;
  role?: UserRole;
  exp?: number;
}

/**
 * Central Angular service for authentication and authorization state.
 *
 * Handles login and registration REST calls, stores the JWT token in local
 * storage, extracts role/email claims, verifies token expiration and provides
 * the default route for each user role.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/auth';
  private readonly tokenKey = 'restaurant_token';

  /**
   * Authenticates a user and stores the returned JWT.
   * @param request email/password login data
   * @returns observable containing the authentication response
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(tap(response => this.storeToken(response.token)));
  }

  /**
   * Registers a new customer and stores the JWT returned by the backend.
   * @param request customer registration data
   * @returns observable containing the authentication response
   */
  register(request: RegisterRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/register`, request)
      .pipe(tap(response => this.storeToken(response.token)));
  }

  /** @returns observable containing the currently authenticated user */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  /** @returns JWT stored in the browser, or null when no token exists */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /** @returns authenticated role extracted from the JWT */
  getRole(): UserRole | null {
    return this.getPayload()?.role ?? null;
  }

  /** @returns authenticated email address stored in the JWT subject claim */
  getEmail(): string | null {
    return this.getPayload()?.sub ?? null;
  }

  /**
   * Determines whether the browser currently holds a non-expired JWT.
   * Expired tokens are removed automatically.
   * @returns true when the current token is valid and active
   */
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

  /**
   * Checks whether the current user has any role in the supplied list.
   * @param roles accepted application roles
   * @returns true when the current role is included
   */
  hasAnyRole(...roles: UserRole[]): boolean {
    const role = this.getRole();
    return role !== null && roles.includes(role);
  }

  /**
   * Resolves the landing page for the currently authenticated role.
   * @returns Angular route for ADMIN, WAITER, CHEF or CUSTOMER
   */
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

  /** Removes the locally stored JWT and ends the client session. */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  /** Stores a JWT returned by the backend authentication API. */
  private storeToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  /**
   * Decodes the JWT payload without validating its signature.
   * Signature validation remains the responsibility of the Spring Security
   * backend; the frontend only reads claims needed for UX decisions.
   */
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
