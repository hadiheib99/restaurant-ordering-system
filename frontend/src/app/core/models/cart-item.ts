import { Meal } from './meal';

export interface CartItem {
  meal: Meal;
  quantity: number;
}
