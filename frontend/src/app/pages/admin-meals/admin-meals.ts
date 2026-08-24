import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Meal } from '../../core/models/meal';
import { MealRequest, MealService } from '../../core/services/meal';

/** Minimal category representation used by the meal editor select control. */
interface Category {
  id: number;
  name: string;
  description?: string;
}

/**
 * Administrator page for complete meal/menu management.
 *
 * Supports meal creation, editing, deletion, availability toggling, image URLs
 * and category assignment. Server state is reloaded after successful saves.
 */
@Component({
  selector: 'app-admin-meals',
  imports: [FormsModule],
  templateUrl: './admin-meals.html',
  styleUrl: './admin-meals.scss',
})
export class AdminMeals {
  private readonly mealService = inject(MealService);
  private readonly http = inject(HttpClient);

  readonly meals = signal<Meal[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly editingId = signal<number | null>(null);
  form: MealRequest = this.emptyForm();

  /** Loads meals and categories when the admin page is created. */
  constructor() {
    this.loadMeals();
    this.loadCategories();
  }

  /** Loads all meals from the backend REST API. */
  loadMeals(): void {
    this.loading.set(true);
    this.mealService.getMeals().subscribe({
      next: meals => { this.meals.set(meals); this.loading.set(false); },
      error: () => { this.errorMessage.set('Could not load meals.'); this.loading.set(false); }
    });
  }

  /** Loads categories used by the meal edit/create form. */
  loadCategories(): void {
    this.http.get<Category[]>('/api/categories').subscribe({
      next: categories => this.categories.set(categories),
      error: () => this.errorMessage.set('Could not load categories.')
    });
  }

  /** Validates the form and creates or updates a meal through {@link MealService}. */
  saveMeal(): void {
    if (!this.form.name.trim() || !this.form.categoryId || this.form.price < 0) {
      this.errorMessage.set('Please enter a name, price and category.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');
    const id = this.editingId();
    const request = { ...this.form, imageUrl: this.form.imageUrl?.trim() || null };
    const operation = id === null
      ? this.mealService.createMeal(request)
      : this.mealService.updateMeal(id, request);

    operation.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.loadMeals(); },
      error: () => { this.saving.set(false); this.errorMessage.set('Could not save the meal.'); }
    });
  }

  /** @param meal meal whose values should be copied into the editor */
  editMeal(meal: Meal): void {
    this.editingId.set(meal.id);
    this.form = {
      name: meal.name,
      description: meal.description ?? '',
      price: meal.price,
      categoryId: meal.categoryId,
      available: meal.available,
      imageUrl: meal.imageUrl ?? ''
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Toggles whether customers may order the selected meal. */
  toggleAvailability(meal: Meal): void {
    const request: MealRequest = {
      name: meal.name,
      description: meal.description ?? '',
      price: meal.price,
      categoryId: meal.categoryId,
      available: !meal.available,
      imageUrl: meal.imageUrl ?? null
    };
    this.mealService.updateMeal(meal.id, request).subscribe({
      next: updated => this.meals.update(items => items.map(item => item.id === updated.id ? updated : item)),
      error: () => this.errorMessage.set('Could not change meal availability.')
    });
  }

  /** Confirms and permanently deletes a meal. */
  deleteMeal(meal: Meal): void {
    if (!confirm(`Delete ${meal.name}?`)) return;
    this.mealService.deleteMeal(meal.id).subscribe({
      next: () => this.meals.update(items => items.filter(item => item.id !== meal.id)),
      error: () => this.errorMessage.set('Could not delete the meal.')
    });
  }

  /** Leaves edit mode and clears the meal form. */
  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  /** @returns default values for a new meal form */
  private emptyForm(): MealRequest {
    return { name: '', description: '', price: 0, categoryId: 0, available: true, imageUrl: '' };
  }
}
