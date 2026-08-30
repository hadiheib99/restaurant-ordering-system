import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Orders } from './orders';
import { OrderService } from '../../core/services/order';
import { AuthService } from '../../core/services/auth';

describe('Orders', () => {
  let component: Orders;
  let fixture: ComponentFixture<Orders>;

  const receiptXml = `<?xml version="1.0"?>
    <receipt orderId="1">
      <status>NEW</status>
      <customer>John Smith</customer>
      <createdAt>2026-08-30T19:00:00</createdAt>
      <items>
        <item>
          <meal>Margherita Pizza</meal>
          <quantity>2</quantity>
          <unitPrice>40.00</unitPrice>
          <subtotal>80.00</subtotal>
        </item>
      </items>
      <totalPrice>80.00</totalPrice>
    </receipt>`;

  const reportXml = `<?xml version="1.0"?>
    <restaurantReport generatedAt="2026-08-30T19:10:00Z">
      <summary>
        <totalOrders>3</totalOrders>
        <paidRevenue>120.00</paidRevenue>
        <status name="NEW" count="1"/>
        <status name="PAID" count="2"/>
      </summary>
      <orders>
        <order id="1" status="PAID">
          <customer>John Smith</customer>
          <totalPrice>80.00</totalPrice>
          <createdAt>2026-08-30T19:00:00</createdAt>
        </order>
      </orders>
    </restaurantReport>`;

  const orderService = {
    getOrders: () => of([]),
    updateStatus: () => of({}),
    getReceiptXml: () => of(new Blob([receiptXml], { type: 'application/xml' })),
    getReportXml: () => of(new Blob([reportXml], { type: 'application/xml' })),
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

  it('parses XML receipt into reader-friendly data', async () => {
    component.viewReceipt({ id: 1 } as never);
    await fixture.whenStable();

    expect(component.xmlViewerOpen()).toBe(true);
    expect(component.xmlViewerLoading()).toBe(false);
    expect(component.receiptView()?.customer).toBe('John Smith');
    expect(component.receiptView()?.items[0].meal).toBe('Margherita Pizza');
    expect(component.receiptView()?.totalPrice).toBe('80.00');
  });

  it('clears document reader state when closed', async () => {
    component.viewReceipt({ id: 1 } as never);
    await fixture.whenStable();
    component.closeXmlViewer();

    expect(component.xmlViewerOpen()).toBe(false);
    expect(component.receiptView()).toBeNull();
    expect(component.reportView()).toBeNull();
    expect(component.xmlViewerError()).toBe('');
  });
});
