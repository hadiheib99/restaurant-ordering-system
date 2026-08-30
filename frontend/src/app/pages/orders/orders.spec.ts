import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Orders } from './orders';
import { OrderService } from '../../core/services/order';
import { AuthService } from '../../core/services/auth';

describe('Orders', () => {
  let component: Orders;
  let fixture: ComponentFixture<Orders>;

  const receiptXml = '<?xml version="1.0"?><receipt orderId="1"><status>NEW</status></receipt>';

  const orderService = {
    getOrders: () => of([]),
    updateStatus: () => of({}),
    getReceiptXml: () => of(new Blob([receiptXml], { type: 'application/xml' })),
    getReportXml: () => of(new Blob(['<report><totalOrders>1</totalOrders></report>'], { type: 'application/xml' })),
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

  it('opens an XML receipt in the in-app reader', async () => {
    component.viewReceipt({ id: 1 } as never);
    await fixture.whenStable();

    expect(component.xmlViewerOpen()).toBe(true);
    expect(component.xmlViewerLoading()).toBe(false);
    expect(component.xmlViewerTitle()).toContain('Order #1');
    expect(component.xmlViewerContent()).toContain('<receipt orderId="1">');
    expect(component.xmlViewerContent()).toContain('  <status>NEW</status>');
  });

  it('clears XML reader state when closed', async () => {
    component.viewReceipt({ id: 1 } as never);
    await fixture.whenStable();
    component.closeXmlViewer();

    expect(component.xmlViewerOpen()).toBe(false);
    expect(component.xmlViewerContent()).toBe('');
    expect(component.xmlViewerError()).toBe('');
  });
});
