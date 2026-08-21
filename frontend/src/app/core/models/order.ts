export interface OrderItem {
  id: number;
  mealId: number;
  mealName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerId: number;
  customerName: string;
  waiterId: number | null;
  waiterName: string | null;
  status: string;
  totalPrice: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}
