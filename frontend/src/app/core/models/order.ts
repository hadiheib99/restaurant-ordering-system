/** One line item returned as part of an order response. */
export interface OrderItem {
  id: number;
  mealId: number;
  mealName: string;
  quantity: number;
  /** Price captured when the order was created. */
  unitPrice: number;
  subtotal: number;
}

/** Complete order representation displayed to customer and staff roles. */
export interface Order {
  id: number;
  customerId: number;
  customerName: string;
  waiterId: number | null;
  waiterName: string | null;
  /** Current backend lifecycle status such as NEW, READY or PAID. */
  status: string;
  totalPrice: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}
