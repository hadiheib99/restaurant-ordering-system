import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';

import { OrderService } from '../../core/services/order';
import { Order } from '../../core/models/order';
import { AuthService } from '../../core/services/auth';

interface ReceiptItemView {
  meal: string;
  quantity: string;
  unitPrice: string;
  subtotal: string;
}

interface ReceiptView {
  orderId: string;
  status: string;
  customer: string;
  createdAt: string;
  totalPrice: string;
  items: ReceiptItemView[];
}

interface ReportStatusView {
  name: string;
  count: string;
}

interface ReportOrderView {
  id: string;
  status: string;
  customer: string;
  totalPrice: string;
  createdAt: string;
}

interface ReportView {
  generatedAt: string;
  totalOrders: string;
  paidRevenue: string;
  statuses: ReportStatusView[];
  orders: ReportOrderView[];
}

/** Shared order-management page used by customers, chefs, waiters and admins. */
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
  readonly canExportReport = this.role === 'ADMIN';

  readonly xmlViewerOpen = signal(false);
  readonly xmlViewerLoading = signal(false);
  readonly xmlViewerTitle = signal('Document Viewer');
  readonly xmlViewerError = signal('');
  readonly receiptView = signal<ReceiptView | null>(null);
  readonly reportView = signal<ReportView | null>(null);

  ngOnInit(): void { this.loadOrders(); }

  loadOrders(): void {
    this.orderService.getOrders().subscribe({
      next: orders => { this.orders.set(orders); this.loading.set(false); },
      error: error => { console.error(error); this.errorMessage.set('Could not load orders.'); this.loading.set(false); }
    });
  }

  updateStatus(order: Order, status: string): void {
    const allowedStatus = this.nextAllowedStatus(order.status);
    if (allowedStatus !== status) return;
    this.applyStatus(order, status);
  }

  canCancel(order: Order): boolean {
    const beforeReady = order.status === 'NEW' || order.status === 'PREPARING';
    return beforeReady && (this.role === 'CUSTOMER' || this.role === 'WAITER' || this.role === 'ADMIN');
  }

  cancelOrder(order: Order): void {
    if (!this.canCancel(order) || !confirm(`Cancel order #${order.id}?`)) return;
    this.applyStatus(order, 'CANCELLED');
  }

  /** Loads receipt XML and converts it to a reader-friendly receipt view. */
  viewReceipt(order: Order): void {
    this.openXmlViewer(`Receipt - Order #${order.id}`);
    this.orderService.getReceiptXml(order.id).subscribe({
      next: blob => this.parseXmlBlob(blob, 'receipt'),
      error: error => this.handleXmlViewerError(error, 'Could not load the receipt.')
    });
  }

  /** Loads report XML and converts it to a reader-friendly report view. */
  viewReport(): void {
    if (!this.canExportReport) return;
    this.openXmlViewer('Restaurant Orders Report');
    this.orderService.getReportXml().subscribe({
      next: blob => this.parseXmlBlob(blob, 'report'),
      error: error => this.handleXmlViewerError(error, 'Could not load the restaurant report.')
    });
  }

  closeXmlViewer(): void {
    this.xmlViewerOpen.set(false);
    this.xmlViewerLoading.set(false);
    this.receiptView.set(null);
    this.reportView.set(null);
    this.xmlViewerError.set('');
  }

  downloadReceipt(order: Order): void {
    this.orderService.getReceiptXml(order.id).subscribe({
      next: blob => this.downloadBlob(blob, `order-${order.id}-receipt.xml`),
      error: error => { console.error(error); alert('Could not export the XML receipt.'); }
    });
  }

  exportReport(): void {
    if (!this.canExportReport) return;
    this.orderService.getReportXml().subscribe({
      next: blob => this.downloadBlob(blob, 'restaurant-orders-report.xml'),
      error: error => { console.error(error); alert('Could not export the XML report.'); }
    });
  }

  deleteOrder(order: Order): void {
    if (!this.canDelete || !confirm(`Delete order #${order.id}?`)) return;
    this.orderService.deleteOrder(order.id).subscribe({
      next: () => this.orders.update(orders => orders.filter(current => current.id !== order.id)),
      error: error => { console.error(error); alert('Could not delete the order'); }
    });
  }

  nextAllowedStatus(status: string): string | null {
    if (this.role === 'ADMIN') return this.nextStatus(status);
    if (this.role === 'CHEF') {
      if (status === 'NEW') return 'PREPARING';
      if (status === 'PREPARING') return 'READY';
      return null;
    }
    if (this.role === 'WAITER') {
      if (status === 'READY') return 'SERVED';
      if (status === 'SERVED') return 'PAID';
      return null;
    }
    return null;
  }

  private applyStatus(order: Order, status: string): void {
    this.orderService.updateStatus(order.id, status).subscribe({
      next: updatedOrder => this.orders.update(orders =>
        orders.map(current => current.id === updatedOrder.id ? updatedOrder : current)),
      error: error => {
        console.error(error);
        alert(status === 'CANCELLED'
          ? 'This order can no longer be cancelled.'
          : 'You do not have permission to perform this status change.');
      }
    });
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

  private openXmlViewer(title: string): void {
    this.xmlViewerTitle.set(title);
    this.receiptView.set(null);
    this.reportView.set(null);
    this.xmlViewerError.set('');
    this.xmlViewerLoading.set(true);
    this.xmlViewerOpen.set(true);
  }

  private parseXmlBlob(blob: Blob, expectedType: 'receipt' | 'report'): void {
    void blob.text()
      .then(xml => {
        const document = new DOMParser().parseFromString(xml, 'application/xml');
        if (document.querySelector('parsererror')) throw new Error('Invalid XML document');

        if (expectedType === 'receipt') this.receiptView.set(this.toReceiptView(document));
        else this.reportView.set(this.toReportView(document));

        this.xmlViewerLoading.set(false);
      })
      .catch(error => this.handleXmlViewerError(error, 'Could not read the XML document.'));
  }

  private toReceiptView(document: Document): ReceiptView {
    const root = document.documentElement;
    const items = Array.from(document.querySelectorAll('items > item')).map(item => ({
      meal: this.elementText(item, 'meal'),
      quantity: this.elementText(item, 'quantity'),
      unitPrice: this.elementText(item, 'unitPrice'),
      subtotal: this.elementText(item, 'subtotal')
    }));

    return {
      orderId: root.getAttribute('orderId') ?? '-',
      status: this.text(document, 'status'),
      customer: this.text(document, 'customer'),
      createdAt: this.text(document, 'createdAt'),
      totalPrice: this.text(document, 'totalPrice'),
      items
    };
  }

  private toReportView(document: Document): ReportView {
    const root = document.documentElement;
    const statuses = Array.from(document.querySelectorAll('summary > status')).map(status => ({
      name: status.getAttribute('name') ?? '-',
      count: status.getAttribute('count') ?? '0'
    }));
    const orders = Array.from(document.querySelectorAll('orders > order')).map(order => ({
      id: order.getAttribute('id') ?? '-',
      status: order.getAttribute('status') ?? '-',
      customer: this.elementText(order, 'customer'),
      totalPrice: this.elementText(order, 'totalPrice'),
      createdAt: this.elementText(order, 'createdAt')
    }));

    return {
      generatedAt: root.getAttribute('generatedAt') ?? '-',
      totalOrders: this.text(document, 'totalOrders'),
      paidRevenue: this.text(document, 'paidRevenue'),
      statuses,
      orders
    };
  }

  private text(document: Document, selector: string): string {
    return document.querySelector(selector)?.textContent?.trim() || '-';
  }

  private elementText(element: Element, selector: string): string {
    return element.querySelector(selector)?.textContent?.trim() || '-';
  }

  private handleXmlViewerError(error: unknown, message: string): void {
    console.error(error);
    this.xmlViewerLoading.set(false);
    this.xmlViewerError.set(message);
  }

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }

  goBack(): void { void this.router.navigateByUrl(this.authService.defaultRoute()); }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
