import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';

import { OrderService } from '../../core/services/order';
import { Order } from '../../core/models/order';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.html',
  styleUrl: './orders.scss'
})
export class Orders implements OnInit {

  private readonly orderService = inject(OrderService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly orders = signal<Order[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly role = this.authService.getRole();
  readonly canDelete = this.role === 'ADMIN';

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe({
      next: orders => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: error => {
        console.error(error);
        this.errorMessage.set('Could not load orders.');
        this.loading.set(false);
      }
    });
  }

  updateStatus(order: Order, status: string): void {
    const allowedStatus = this.nextAllowedStatus(order.status);

    if (allowedStatus !== status) {
      return;
    }

    this.orderService.updateStatus(order.id, status).subscribe({
      next: updatedOrder => {
        this.orders.update(orders =>
          orders.map(current =>
            current.id === updatedOrder.id ? updatedOrder : current
          )
        );
      },
      error: error => {
        console.error(error);
        alert('You do not have permission to perform this status change.');
      }
    });
  }

  deleteOrder(order: Order): void {
    if (!this.canDelete || !confirm(`Delete order #${order.id}?`)) {
      return;
    }

    this.orderService.deleteOrder(order.id).subscribe({
      next: () => this.orders.update(orders =>
        orders.filter(current => current.id !== order.id)
      ),
      error: error => {
        console.error(error);
        alert('Could not delete the order');
      }
    });
  }

  nextAllowedStatus(status: string): string | null {
    if (this.role === 'ADMIN') {
      return this.nextStatus(status);
    }

    if (this.role === 'CHEF') {
      if (status === 'NEW') {
        return 'PREPARING';
      }
      if (status === 'PREPARING') {
        return 'READY';
      }
      return null;
    }

    if (this.role === 'WAITER') {
      if (status === 'READY') {
        return 'SERVED';
      }
      if (status === 'SERVED') {
        return 'PAID';
      }
      return null;
    }

    return null;
  }

  private nextStatus(status: string): string | null {
    switch (status) {
      case 'NEW': return 'PREPARING';
      case 'PREPARING': return 'READY';
      case 'READY': return 'SERVED';
      case 'SERVED': return 'PAID';
      default: return null;
    }
  }

  goBack(): void {
    void this.router.navigateByUrl(this.authService.defaultRoute());
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
