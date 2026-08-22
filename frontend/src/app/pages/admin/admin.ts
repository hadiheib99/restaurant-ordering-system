import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/services/auth';
import { MealService } from '../../core/services/meal';
import { OrderService } from '../../core/services/order';
import { UserService } from '../../core/services/user';
import { Order } from '../../core/models/order';

@Component({
  selector: 'app-admin',
  imports: [RouterLink],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin {
  private readonly authService = inject(AuthService);
  private readonly mealService = inject(MealService);
  private readonly orderService = inject(OrderService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  readonly loadingStats = signal(true);
  readonly statsError = signal('');
  readonly orders = signal<Order[]>([]);
  readonly totalMeals = signal(0);
  readonly availableMeals = signal(0);
  readonly totalCustomers = signal(0);

  readonly activeOrders = computed(() =>
    this.orders().filter(order => order.status !== 'PAID').length
  );

  readonly paidOrders = computed(() =>
    this.orders().filter(order => order.status === 'PAID').length
  );

  readonly revenue = computed(() =>
    this.orders()
      .filter(order => order.status === 'PAID')
      .reduce((sum, order) => sum + Number(order.totalPrice), 0)
  );

  readonly todayOrders = computed(() => {
    const today = new Date().toDateString();
    return this.orders().filter(order => new Date(order.createdAt).toDateString() === today).length;
  });

  constructor() {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loadingStats.set(true);
    this.statsError.set('');

    forkJoin({
      meals: this.mealService.getMeals(),
      orders: this.orderService.getOrders(),
      users: this.userService.getUsers()
    }).subscribe({
      next: ({ meals, orders, users }) => {
        this.totalMeals.set(meals.length);
        this.availableMeals.set(meals.filter(meal => meal.available).length);
        this.orders.set(orders);
        this.totalCustomers.set(users.filter(user => user.role === 'CUSTOMER').length);
        this.loadingStats.set(false);
      },
      error: () => {
        this.statsError.set('Dashboard statistics could not be loaded.');
        this.loadingStats.set(false);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
