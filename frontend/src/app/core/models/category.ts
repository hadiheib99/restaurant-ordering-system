/** Category returned by the backend and displayed in menu/admin views. */
export interface Category {
  /** Database identifier. */
  id: number;
  /** Unique display name. */
  name: string;
  /** Human-readable category description. */
  description: string;
}

/** Editable category fields sent by the admin UI. */
export interface CategoryRequest {
  name: string;
  description: string;
}
