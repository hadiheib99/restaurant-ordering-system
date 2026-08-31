import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth';

/**
 * Authentication page for existing restaurant users.
 *
 * Validates email/password input, calls the authentication REST service, stores
 * the returned JWT through {@link AuthService} and redirects each role to its
 * appropriate landing page.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal('');

  /** Reactive login form with required email and password validation. */
  readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  /** Redirects users who already have a valid JWT instead of showing login again. */
  constructor() {
    if (this.authService.isLoggedIn()) {
      void this.router.navigateByUrl(this.authService.defaultRoute());
    }
  }

  /** Validates and submits the login form, then redirects according to the JWT role. */
  submit(): void {
    if (this.loginForm.invalid || this.loading()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigateByUrl(this.authService.defaultRoute());
      },
      error: error => {
        this.loading.set(false);

        if (error.status === 401) {
          this.errorMessage.set('Invalid email or password.');
          return;
        }

        if (error.status === 0) {
          this.errorMessage.set('The server could not be reached. Make sure Spring Boot is running.');
          return;
        }

        this.errorMessage.set(
          error?.error?.message ?? 'Unable to sign in. Please try again.'
        );
      }
    });
  }
}
