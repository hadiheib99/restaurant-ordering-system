import { Injectable, computed, signal } from '@angular/core';
import { CartItem } from '../models/cart-item';
import { Meal } from '../models/meal';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private readonly _items = signal<CartItem[]>([]);

  readonly items = this._items.asReadonly();

  readonly total = computed(() =>
    this._items().reduce(
      (sum, item) => sum + item.meal.price * item.quantity,
      0
    )
  );

  readonly count = computed(() =>
    this._items().reduce(
      (sum, item) => sum + item.quantity,
      0
    )
  );

  add(meal: Meal): void {
    const items = this._items();

    const existing = items.find(
      item => item.meal.id === meal.id
    );

    if (existing) {
      this._items.set(
        items.map(item =>
          item.meal.id === meal.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      );
    } else {
      this._items.set([
        ...items,
        { meal, quantity: 1 }
      ]);
    }
  }

  increase(mealId: number): void {
    this._items.update(items =>
      items.map(item =>
        item.meal.id === mealId
          ? { ...item, quantity: item.quantity + 1 }
          : item
      )
    );
  }

  decrease(mealId: number): void {
    this._items.update(items =>
      items
        .map(item =>
          item.meal.id === mealId
            ? { ...item, quantity: item.quantity - 1 }
            : item
        )
        .filter(item => item.quantity > 0)
    );
  }

  remove(mealId: number): void {
    this._items.update(items =>
      items.filter(item => item.meal.id !== mealId)
    );
  }

  clear(): void {
    this._items.set([]);
  }
}
