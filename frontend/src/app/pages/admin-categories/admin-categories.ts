import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Category, CategoryRequest } from '../../core/models/category';
import { CategoryService } from '../../core/services/category';

@Component({
  selector: 'app-admin-categories',
  imports: [FormsModule],
  templateUrl: './admin-categories.html',
  styleUrl: './admin-categories.scss',
})
export class AdminCategories {
  private readonly categoryService = inject(CategoryService);

  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly editingId = signal<number | null>(null);

  form: CategoryRequest = this.emptyForm();

  constructor() {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading.set(true);
    this.categoryService.getCategories().subscribe({
      next: categories => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load categories.');
        this.loading.set(false);
      }
    });
  }

  saveCategory(): void {
    if (!this.form.name.trim()) {
      this.errorMessage.set('Category name is required.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    const id = this.editingId();
    const request = { ...this.form };
    const operation = id === null
      ? this.categoryService.createCategory(request)
      : this.categoryService.updateCategory(id, request);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.loadCategories();
      },
      error: error => {
        this.saving.set(false);
        this.errorMessage.set(
          error.status === 409
            ? 'A category with this name already exists.'
            : 'Could not save the category.'
        );
      }
    });
  }

  editCategory(category: Category): void {
    this.editingId.set(category.id);
    this.form = {
      name: category.name,
      description: category.description ?? ''
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteCategory(category: Category): void {
    if (!confirm(`Delete ${category.name}? Meals using this category may prevent deletion.`)) {
      return;
    }

    this.categoryService.deleteCategory(category.id).subscribe({
      next: () => this.categories.update(items =>
        items.filter(item => item.id !== category.id)
      ),
      error: () => this.errorMessage.set(
        'Could not delete this category. Make sure no meals are using it.'
      )
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  private emptyForm(): CategoryRequest {
    return {
      name: '',
      description: ''
    };
  }
}
