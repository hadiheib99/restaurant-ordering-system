import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { User, UserRequest, UserRole } from '../../core/models/user';
import { UserService } from '../../core/services/user';

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
  readonly roles: UserRole[] = ['ADMIN', 'WAITER', 'CUSTOMER', 'CHEF'];

  form: UserRequest = this.emptyForm();

  constructor() {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService.getUsers().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load users.');
        this.loading.set(false);
      }
    });
  }

  saveUser(): void {
    if (!this.form.username.trim() || !this.form.email.trim() || !this.form.firstName.trim() || !this.form.lastName.trim()) {
      this.errorMessage.set('Username, name and email are required.');
      return;
    }

    if (this.form.password.length < 6) {
      this.errorMessage.set('Password must contain at least 6 characters.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    const id = this.editingId();
    const request = { ...this.form };
    const operation = id === null
      ? this.userService.createUser(request)
      : this.userService.updateUser(id, request);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.loadUsers();
      },
      error: error => {
        this.saving.set(false);
        this.errorMessage.set(
          error.status === 400
            ? 'Could not save user. Check that username and email are unique.'
            : 'Could not save user.'
        );
      }
    });
  }

  editUser(user: User): void {
    this.editingId.set(user.id);
    this.form = {
      username: user.username,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      phone: user.phone ?? '',
      role: user.role
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  toggleEnabled(user: User): void {
    this.userService.setEnabled(user.id, !user.enabled).subscribe({
      next: updated => this.users.update(items =>
        items.map(item => item.id === updated.id ? updated : item)
      ),
      error: () => this.errorMessage.set('Could not update account status.')
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Delete ${user.firstName} ${user.lastName}?`)) {
      return;
    }

    this.userService.deleteUser(user.id).subscribe({
      next: () => this.users.update(items => items.filter(item => item.id !== user.id)),
      error: () => this.errorMessage.set('Could not delete this user. They may be referenced by existing orders.')
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  private emptyForm(): UserRequest {
    return {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      role: 'CUSTOMER'
    };
  }
}
