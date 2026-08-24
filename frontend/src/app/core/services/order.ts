import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Order } from '../models/order';

/** One meal/quantity pair sent when creating an order. */
interface OrderItemRequest {
  mealId: number;
  quantity: number;
}

/** Data required by the backend to create a restaurant order. */
export interface OrderRequest {
  customerId: number;
  waiterId?: number;
  items: OrderItemRequest[];
}

/**
 * REST client for order creation, status updates, deletion and XML exports.
 * Authentication headers are added centrally by the JWT HTTP interceptor.
 */
@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/orders';

  /** @returns orders visible to the authenticated role */
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  /** @param request new order data @returns created order */
  createOrder(request: OrderRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, request);
  }

  /** @param orderId order identifier @param status requested lifecycle status @returns updated order */
  updateStatus(orderId: number, status: string): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${orderId}/status?value=${status}`, {});
  }

  /** @param orderId order identifier @returns XML receipt as a downloadable Blob */
  getReceiptXml(orderId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${orderId}/receipt.xml`, { responseType: 'blob' });
  }

  /** @returns administrator restaurant report as an XML Blob */
  getReportXml(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/report.xml`, { responseType: 'blob' });
  }

  /** @param orderId order identifier @returns completion observable for deletion */
  deleteOrder(orderId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${orderId}`);
  }
}
