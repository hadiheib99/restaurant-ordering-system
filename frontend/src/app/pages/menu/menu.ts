import { Component, inject, OnInit, signal } from '@angular/core';
import { MealService } from '../../core/services/meal';
import { CartService } from '../../core/services/cart';
import { Meal } from '../../core/models/meal';
import { OrderService } from '../../core/services/order';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.html',
  styleUrl: './menu.scss'
})
export class Menu implements OnInit {

  private readonly mealService = inject(MealService);
  private readonly orderService = inject(OrderService);
  readonly cartService = inject(CartService);

  readonly meals = signal<Meal[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

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

  addToCart(meal: Meal): void {
    this.cartService.add(meal);
  }
  placeOrder(): void {
    const request = {
      customerId: 2,
      waiterId: 3,
      items: this.cartService.items().map(item => ({
        mealId: item.meal.id,
        quantity: item.quantity
      }))
    };

    this.orderService.createOrder(request).subscribe({
      next: order => {
        console.log('Order created:', order);

        this.cartService.clear();

        alert(`Order #${order.id} created successfully`);
      },

      error: error => {
        console.error('Failed to create order:', error);
        alert('Could not place the order');
      }
    });
  }
}
