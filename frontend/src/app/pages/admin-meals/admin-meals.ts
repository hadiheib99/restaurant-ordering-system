import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Meal } from '../../core/models/meal';
import { MealRequest, MealService } from '../../core/services/meal';

interface Category {
  id: number;
  name: string;
  description?: string;
}

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

  constructor() {
    this.loadMeals();
    this.loadCategories();
  }

  loadMeals(): void {
    this.loading.set(true);
    this.mealService.getMeals().subscribe({
      next: meals => {
        this.meals.set(meals);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load meals.');
        this.loading.set(false);
      }
    });
  }

  loadCategories(): void {
    this.http.get<Category[]>('http://localhost:8080/api/categories').subscribe({
      next: categories => this.categories.set(categories),
      error: () => this.errorMessage.set('Could not load categories.')
    });
  }

  saveMeal(): void {
    if (!this.form.name.trim() || !this.form.categoryId || this.form.price < 0) {
      this.errorMessage.set('Please enter a name, price and category.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');
    const id = this.editingId();
    const request = { ...this.form };
    const operation = id === null
      ? this.mealService.createMeal(request)
      : this.mealService.updateMeal(id, request);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.loadMeals();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Could not save the meal.');
      }
    });
  }

  editMeal(meal: Meal): void {
    this.editingId.set(meal.id);
    this.form = {
      name: meal.name,
      description: meal.description ?? '',
      price: meal.price,
      categoryId: meal.categoryId,
      available: meal.available
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  toggleAvailability(meal: Meal): void {
    const request: MealRequest = {
      name: meal.name,
      description: meal.description ?? '',
      price: meal.price,
      categoryId: meal.categoryId,
      available: !meal.available
    };

    this.mealService.updateMeal(meal.id, request).subscribe({
      next: updated => this.meals.update(items =>
        items.map(item => item.id === updated.id ? updated : item)
      ),
      error: () => this.errorMessage.set('Could not change meal availability.')
    });
  }

  deleteMeal(meal: Meal): void {
    if (!confirm(`Delete ${meal.name}?`)) {
      return;
    }

    this.mealService.deleteMeal(meal.id).subscribe({
      next: () => this.meals.update(items => items.filter(item => item.id !== meal.id)),
      error: () => this.errorMessage.set('Could not delete the meal.')
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form = this.emptyForm();
  }

  private emptyForm(): MealRequest {
    return {
      name: '',
      description: '',
      price: 0,
      categoryId: 0,
      available: true
    };
  }
}
