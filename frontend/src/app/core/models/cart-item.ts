import { Meal } from './meal';

/** One client-side shopping-cart row containing a meal and selected quantity. */
export interface CartItem {
  /** Meal selected from the menu. */
  meal: Meal;
  /** Number of units currently selected. */
  quantity: number;
}
