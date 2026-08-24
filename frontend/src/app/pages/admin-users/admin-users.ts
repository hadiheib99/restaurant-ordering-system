import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { User, UserRequest, UserRole } from '../../core/models/user';
import { UserService } from '../../core/services/user';

/**
 * Administrator page for restaurant account management.
 *
 * Provides user creation/editing, role assignment, account enable/disable and
 * deletion while keeping form, loading and error state reactive.
 */
@Component({
  selector: 'app-admin-users',
  imports: [FormsModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss',
})
export class AdminUsers {
  private readonly userService = inject(UserService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly editingId = signal<number | null>(null);
  /** Roles an administrator may assign to managed accounts. */
  readonly roles: UserRole[] = ['ADMIN', 'WAITER', 'CUSTOMER', 'CHEF'];
  form: UserRequest = this.emptyForm();

  /** Loads users immediately when the component is created. */
  constructor() { this.loadUsers(); }

  /** Requests all users from the backend and updates page state. */
  loadUsers(): void {
    this.loading.set(true);
    this.userService.getUsers().subscribe({
      next: users => { this.users.set(users); this.loading.set(false); },
      error: () => { this.errorMessage.set('Could not load users.'); this.loading.set(false); }
    });
  }

  /** Validates form fields and creates a new user or updates the selected account. */
  saveUser(): void {
    if (!this.form.username.trim() || !this.form.email.trim() || !this.form.firstName.trim() || !this.form.lastName.trim()) {
      this.errorMessage.set('Username, name and email are required.');
      return;
    }

    const editing = this.editingId() !== null;
    const passwordProvided = this.form.password.trim().length > 0;
    if ((!editing || passwordProvided) && this.form.password.length < 6) {
      this.errorMessage.set('Password must contain at least 6 characters.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');
    const id = this.editingId();
    const operation = id === null
      ? this.userService.createUser({ ...this.form })
      : this.userService.updateUser(id, { ...this.form });

    operation.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.loadUsers(); },
      error: error => {
        this.saving.set(false);
        this.errorMessage.set(error.status === 400
          ? 'Could not save user. Check the fields and make sure username and email are unique.'
          : 'Could not save user.');
      }
    });
  }

  /** @param user account whose values should be loaded into the edit form */
  editUser(user: User): void {
    this.editingId.set(user.id);
    this.form = {
      username: user.username, password: '', firstName: user.firstName,
      lastName: user.lastName, email: user.email, phone: user.phone ?? '', role: user.role
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Enables a disabled account or disables an enabled account. */
  toggleEnabled(user: User): void {
    this.userService.setEnabled(user.id, !user.enabled).subscribe({
      next: updated => this.users.update(items => items.map(item => item.id === updated.id ? updated : item)),
      error: () => this.errorMessage.set('Could not update account status.')
    });
  }

  /** Confirms and deletes the selected user when backend integrity rules allow it. */
  deleteUser(user: User): void {
    if (!confirm(`Delete ${user.firstName} ${user.lastName}?`)) return;
    this.userService.deleteUser(user.id).subscribe({
      next: () => this.users.update(items => items.filter(item => item.id !== user.id)),
      error: () => this.errorMessage.set('Could not delete this user. They may be referenced by existing orders.')
    });
  }

  /** Leaves edit mode and restores a blank form. */
  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  /** @returns default model for creating a customer account */
  private emptyForm(): UserRequest {
    return { username: '', password: '', firstName: '', lastName: '', email: '', phone: '', role: 'CUSTOMER' };
  }
}
