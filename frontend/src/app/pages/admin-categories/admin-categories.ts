import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Category, CategoryRequest } from '../../core/models/category';
import { CategoryService } from '../../core/services/category';

/**
 * Administrator page for creating, editing and deleting meal categories.
 * Maintains form/edit state with Angular signals and refreshes server data after
 * successful mutations.
 */
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

  /** Initializes the page by loading categories from the backend. */
  constructor() {
    this.loadCategories();
  }

  /** Loads all categories and updates loading/error state. */
  loadCategories(): void {
    this.loading.set(true);
    this.categoryService.getCategories().subscribe({
      next: categories => { this.categories.set(categories); this.loading.set(false); },
      error: () => { this.errorMessage.set('Could not load categories.'); this.loading.set(false); }
    });
  }

  /** Validates the form and creates a category or updates the currently edited category. */
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
      next: () => { this.saving.set(false); this.cancelEdit(); this.loadCategories(); },
      error: error => {
        this.saving.set(false);
        this.errorMessage.set(error.status === 409
          ? 'A category with this name already exists.'
          : 'Could not save the category.');
      }
    });
  }

  /** @param category category whose values should be loaded into the edit form */
  editCategory(category: Category): void {
    this.editingId.set(category.id);
    this.form = { name: category.name, description: category.description ?? '' };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Confirms and deletes a category when it is not referenced by meals. */
  deleteCategory(category: Category): void {
    if (!confirm(`Delete ${category.name}? Meals using this category may prevent deletion.`)) return;
    this.categoryService.deleteCategory(category.id).subscribe({
      next: () => this.categories.update(items => items.filter(item => item.id !== category.id)),
      error: () => this.errorMessage.set('Could not delete this category. Make sure no meals are using it.')
    });
  }

  /** Leaves edit mode and restores a blank category form. */
  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  /** @returns initial empty category form model */
  private emptyForm(): CategoryRequest {
    return { name: '', description: '' };
  }
}
