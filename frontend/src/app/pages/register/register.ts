import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth';

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

  readonly codeRequested = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly infoMessage = signal('');

  readonly registerForm = this.formBuilder.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\+[1-9]\d{7,14}$/)]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  readonly verifyForm = this.formBuilder.nonNullable.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{4,10}$/)]]
  });

  requestCode(): void {
    if (this.registerForm.invalid || this.loading()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.infoMessage.set('');

    this.authService.requestRegistration(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.codeRequested.set(true);
        this.infoMessage.set('Verification code sent to your phone.');
      },
      error: error => {
        this.loading.set(false);
        this.errorMessage.set(this.errorText(error, 'Could not start registration.'));
      }
    });
  }

  verifyCode(): void {
    if (this.verifyForm.invalid || this.loading()) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.verifyRegistration(
      this.registerForm.controls.email.value,
      this.verifyForm.controls.code.value
    ).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/menu']);
      },
      error: error => {
        this.loading.set(false);
        this.errorMessage.set(this.errorText(error, 'Invalid or expired verification code.'));
      }
    });
  }

  editDetails(): void {
    this.codeRequested.set(false);
    this.verifyForm.reset();
    this.errorMessage.set('');
    this.infoMessage.set('');
  }

  private errorText(error: any, fallback: string): string {
    return error?.error?.message ?? error?.error?.error ?? fallback;
  }
}
