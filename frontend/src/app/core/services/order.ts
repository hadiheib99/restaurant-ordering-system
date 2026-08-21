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
  private readonly apiUrl = 'http://localhost:8080/api/orders';
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  updateStatus(
    orderId: number,
    status: string
  ): Observable<Order> {
    return this.http.patch<Order>(
      `${this.apiUrl}/${orderId}/status?value=${status}`,
      {}
    );}
  createOrder(request: OrderRequest): Observable<any> {
    return this.http.post(this.apiUrl, request);

  }
}
