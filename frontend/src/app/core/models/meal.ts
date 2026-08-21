export interface Meal {
  id: number;
  name: string;
  description: string;
  price: number;
  available: boolean;
  categoryId: number;
  categoryName: string;
}
