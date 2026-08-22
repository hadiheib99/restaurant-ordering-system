import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { OrderService } from './order';

describe('OrderService', () => {
  let service: OrderService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OrderService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads orders', () => {
    service.getOrders().subscribe();
    const request = http.expectOne('/api/orders');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates an order with the supplied items', () => {
    const payload = { customerId: 2, items: [{ mealId: 1, quantity: 2 }] };
    service.createOrder(payload).subscribe();

    const request = http.expectOne('/api/orders');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({});
  });

  it('updates order status using the status endpoint', () => {
    service.updateStatus(10, 'READY').subscribe();

    const request = http.expectOne('/api/orders/10/status?value=READY');
    expect(request.request.method).toBe('PATCH');
    request.flush({});
  });

  it('deletes an order', () => {
    service.deleteOrder(10).subscribe();
    const request = http.expectOne('/api/orders/10');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
