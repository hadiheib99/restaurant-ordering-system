import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Orders } from './orders';
import { OrderService } from '../../core/services/order';
import { AuthService } from '../../core/services/auth';

describe('Orders', () => {
  let component: Orders;
  let fixture: ComponentFixture<Orders>;

  const orderService = {
    getOrders: () => of([]),
    updateStatus: () => of({}),
    deleteOrder: () => of(void 0)
  };

  const authService = {
    getRole: () => 'CHEF',
    defaultRoute: () => '/orders',
    logout: () => undefined
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Orders],
      providers: [
        provideRouter([]),
        { provide: OrderService, useValue: orderService },
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads without making a real network request', () => {
    expect(component).toBeTruthy();
    expect(component.loading()).toBe(false);
  });

  it('allows a chef to prepare and ready orders only', () => {
    expect(component.nextAllowedStatus('NEW')).toBe('PREPARING');
    expect(component.nextAllowedStatus('PREPARING')).toBe('READY');
    expect(component.nextAllowedStatus('READY')).toBeNull();
  });
});
