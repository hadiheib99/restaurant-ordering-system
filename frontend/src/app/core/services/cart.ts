import { Injectable, computed, signal } from '@angular/core';
import { CartItem } from '../models/cart-item';
import { Meal } from '../models/meal';

/**
 * Client-side shopping-cart state for the customer menu.
 *
 * Uses Angular signals to keep item quantities, total price and total item count
 * reactive without persisting temporary cart data on the backend.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly _items = signal<CartItem[]>([]);

  /** Read-only reactive list of cart items exposed to components. */
  readonly items = this._items.asReadonly();

  /** Reactive monetary total calculated from unit prices and quantities. */
  readonly total = computed(() =>
    this._items().reduce((sum, item) => sum + item.meal.price * item.quantity, 0)
  );

  /** Reactive number of meal units currently in the cart. */
  readonly count = computed(() =>
    this._items().reduce((sum, item) => sum + item.quantity, 0)
  );

  /**
   * Adds a meal to the cart or increments its quantity when already present.
   * @param meal meal selected by the customer
   */
  add(meal: Meal): void {
    const items = this._items();
    const existing = items.find(item => item.meal.id === meal.id);
    if (existing) {
      this._items.set(items.map(item =>
        item.meal.id === meal.id ? { ...item, quantity: item.quantity + 1 } : item));
    } else {
      this._items.set([...items, { meal, quantity: 1 }]);
    }
  }

  /** @param mealId identifier of the meal whose quantity should increase */
  increase(mealId: number): void {
    this._items.update(items => items.map(item =>
      item.meal.id === mealId ? { ...item, quantity: item.quantity + 1 } : item));
  }

  /** Decreases quantity and removes the item automatically when it reaches zero. */
  decrease(mealId: number): void {
    this._items.update(items => items
      .map(item => item.meal.id === mealId ? { ...item, quantity: item.quantity - 1 } : item)
      .filter(item => item.quantity > 0));
  }

  /** Removes one meal completely from the cart. */
  remove(mealId: number): void {
    this._items.update(items => items.filter(item => item.meal.id !== mealId));
  }

  /** Removes every item and resets cart totals. */
  clear(): void {
    this._items.set([]);
  }
}
