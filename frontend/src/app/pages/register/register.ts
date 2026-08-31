import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth';

/**
 * Public customer-registration page.
 *
 * Validates profile/contact/password input, calls the backend registration API,
 * stores the returned JWT through {@link AuthService} and opens the customer menu
 * after a successful registration.
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal('');

  /** Reactive registration form mirroring backend customer-registration fields. */
  readonly registerForm = this.formBuilder.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  /** Validates and submits a new customer account to the authentication service. */
  submit(): void {
    if (this.loading()) {
      return;
    }

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage.set('Please complete all required fields correctly.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.authService.register(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/menu']);
      },
      error: error => {
        this.loading.set(false);

        if (error.status === 0) {
          this.errorMessage.set('The server could not be reached. Make sure Spring Boot is running.');
          return;
        }

        const backendMessage =
          error?.error?.message ??
          (typeof error?.error === 'string' ? error.error : undefined);

        if (backendMessage) {
          this.errorMessage.set(backendMessage);
          return;
        }

        if (error.status === 409) {
          this.errorMessage.set('This email or username is already registered.');
          return;
        }

        this.errorMessage.set('Could not create account. Please check your details and try again.');
      }
    });
  }
}
