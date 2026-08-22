import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Order } from '../models/order';

interface OrderItemRequest {
  mealId: number;
  quantity: number;
}

export interface OrderRequest {
  customerId: number;
  waiterId?: number;
  items: OrderItemRequest[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/orders';

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  createOrder(request: OrderRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, request);
  }

  updateStatus(orderId: number, status: string): Observable<Order> {
    return this.http.patch<Order>(
      `${this.apiUrl}/${orderId}/status?value=${status}`,
      {}
    );
  }

  deleteOrder(orderId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${orderId}`);
  }
}
