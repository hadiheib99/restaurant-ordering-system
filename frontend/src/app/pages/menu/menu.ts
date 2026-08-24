import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MealService } from '../../core/services/meal';
import { CartService } from '../../core/services/cart';
import { Meal } from '../../core/models/meal';
import { OrderService } from '../../core/services/order';
import { AuthService } from '../../core/services/auth';

/**
 * Customer-facing menu component.
 *
 * Displays available meals, supports text/category filtering, manages the
 * shopping cart and sends new orders to the backend REST API. The component
 * also shows loading, error and success states and provides navigation to the
 * customer's order history.
 */
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
  readonly successMessage = signal('');
  readonly searchTerm = signal('');
  readonly selectedCategory = signal('All');

  /** Meals that are currently available for customer ordering. */
  readonly availableMeals = computed(() => this.meals().filter(meal => meal.available));

  /** Distinct category names used by the menu filter controls. */
  readonly categories = computed(() => [
    'All',
    ...Array.from(new Set(this.availableMeals().map(meal => meal.categoryName))).sort()
  ]);

  /** Meals that match both the selected category and current search term. */
  readonly filteredMeals = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const category = this.selectedCategory();

    return this.availableMeals().filter(meal => {
      const matchesCategory = category === 'All' || meal.categoryName === category;
      const matchesSearch = !term ||
        meal.name.toLowerCase().includes(term) ||
        meal.description?.toLowerCase().includes(term) ||
        meal.categoryName.toLowerCase().includes(term);

      return matchesCategory && matchesSearch;
    });
  });

  /** Loads the menu when the component is initialized. */
  ngOnInit(): void {
    this.loadMeals();
  }

  /**
   * Requests all meals from the backend and updates the menu state.
   * Displays an error message when the REST request fails.
   */
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

  /**
   * Updates the free-text menu search value.
   * @param event input event emitted by the search field
   */
  updateSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  /**
   * Selects the category used by the menu filter.
   * @param category category name to display
   */
  selectCategory(category: string): void {
    this.selectedCategory.set(category);
  }

  /** Resets both text and category filters to their default values. */
  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedCategory.set('All');
  }

  /**
   * Adds one meal to the customer's cart.
   * @param meal meal selected by the customer
   */
  addToCart(meal: Meal): void {
    this.cartService.add(meal);
  }

  /**
   * Creates a new order from the current cart.
   *
   * The method first obtains the authenticated customer, converts cart items
   * into an order request, sends the request through {@link OrderService},
   * clears the cart after success and displays a temporary confirmation.
   */
  placeOrder(): void {
    if (this.cartService.items().length === 0 || this.placingOrder()) return;

    this.placingOrder.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

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
            this.successMessage.set(`Order #${order.id} was added successfully!`);
            window.setTimeout(() => this.successMessage.set(''), 5000);
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

  /** Hides the order-success notification immediately. */
  dismissSuccess(): void {
    this.successMessage.set('');
  }

  /** Navigates the authenticated customer to the order-history page. */
  viewOrders(): void {
    void this.router.navigate(['/orders']);
  }

  /** Clears authentication/cart state and returns the user to the login page. */
  logout(): void {
    this.authService.logout();
    this.cartService.clear();
    void this.router.navigate(['/login']);
  }
}
