import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MealService } from '../../core/services/meal';
import { CartService } from '../../core/services/cart';
import { Meal } from '../../core/models/meal';
import { OrderService } from '../../core/services/order';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.html',
  styleUrl: './menu.scss'
})
export class Menu implements OnInit {

  private readonly mealService = inject(MealService);
  private readonly orderService = inject(OrderService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly cartService = inject(CartService);

  readonly meals = signal<Meal[]>([]);
  readonly loading = signal(true);
  readonly placingOrder = signal(false);
  readonly errorMessage = signal('');
  readonly searchTerm = signal('');
  readonly selectedCategory = signal('All');

  readonly categories = computed(() => [
    'All',
    ...Array.from(new Set(this.meals().map(meal => meal.categoryName))).sort()
  ]);

  readonly filteredMeals = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const category = this.selectedCategory();

    return this.meals().filter(meal => {
      const matchesCategory = category === 'All' || meal.categoryName === category;
      const matchesSearch = !term ||
        meal.name.toLowerCase().includes(term) ||
        meal.description?.toLowerCase().includes(term) ||
        meal.categoryName.toLowerCase().includes(term);

      return matchesCategory && matchesSearch;
    });
  });

  ngOnInit(): void {
    this.loadMeals();
  }

  loadMeals(): void {
    this.mealService.getMeals().subscribe({
      next: meals => {
        this.meals.set(meals);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load menu.');
        this.loading.set(false);
      }
    });
  }

  updateSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  selectCategory(category: string): void {
    this.selectedCategory.set(category);
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedCategory.set('All');
  }

  addToCart(meal: Meal): void {
    this.cartService.add(meal);
  }

  placeOrder(): void {
    if (this.cartService.items().length === 0 || this.placingOrder()) return;

    this.placingOrder.set(true);
    this.errorMessage.set('');

    this.authService.getCurrentUser().subscribe({
      next: user => {
        const request = {
          customerId: user.id,
          items: this.cartService.items().map(item => ({
            mealId: item.meal.id,
            quantity: item.quantity
          }))
        };

        this.orderService.createOrder(request).subscribe({
          next: order => {
            this.placingOrder.set(false);
            this.cartService.clear();
            alert(`Order #${order.id} created successfully`);
          },
          error: error => {
            this.placingOrder.set(false);
            console.error('Failed to create order:', error);
            this.errorMessage.set('Could not place the order. Please try again.');
          }
        });
      },
      error: () => {
        this.placingOrder.set(false);
        this.errorMessage.set('Could not identify the logged-in customer. Please log in again.');
      }
    });
  }

  viewOrders(): void {
    void this.router.navigate(['/orders']);
  }

  logout(): void {
    this.authService.logout();
    this.cartService.clear();
    void this.router.navigate(['/login']);
  }
}
