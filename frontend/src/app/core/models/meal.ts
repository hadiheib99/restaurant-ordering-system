/** Meal representation consumed by customer and administrator Angular views. */
export interface Meal {
  id: number;
  name: string;
  description: string;
  price: number;
  /** Whether the meal may currently be ordered. */
  available: boolean;
  categoryId: number;
  categoryName: string;
  /** Optional local or remote image URL used by menu cards. */
  imageUrl?: string | null;
}
