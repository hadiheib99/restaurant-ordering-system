import { TestBed } from '@angular/core/testing';

import { OrderStatus } from './order-status';

describe('OrderStatus', () => {
  let service: OrderStatus;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrderStatus);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
