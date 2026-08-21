import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import { OrderService } from '../../core/services/order';
import { Order } from '../../core/models/order';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.html',
  styleUrl: './orders.scss'
})
export class Orders implements OnInit {

  private readonly orderService = inject(OrderService);

  readonly orders = signal<Order[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

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
    this.orderService
      .updateStatus(order.id, status)
      .subscribe({
        next: updatedOrder => {
          this.orders.update(orders =>
            orders.map(current =>
              current.id === updatedOrder.id
                ? updatedOrder
                : current
            )
          );
        },

        error: error => {
          console.error(error);
          alert('Could not update order status');
        }
      });
  }

  nextStatus(status: string): string | null {
    switch (status) {
      case 'NEW':
        return 'PREPARING';

      case 'PREPARING':
        return 'READY';

      case 'READY':
        return 'SERVED';

      case 'SERVED':
        return 'PAID';

      default:
        return null;
    }
  }
}
