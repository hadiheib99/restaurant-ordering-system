import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Meal } from '../models/meal';

/** Data sent by the admin UI when creating or updating a meal. */
export interface MealRequest {
  name: string;
  description: string;
  price: number;
  categoryId: number;
  available: boolean;
  imageUrl?: string | null;
}

/** REST client for reading the public menu and performing admin meal CRUD operations. */
@Injectable({ providedIn: 'root' })
export class MealService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/meals';

  /** @returns observable containing all meals */
  getMeals(): Observable<Meal[]> {
    return this.http.get<Meal[]>(this.apiUrl);
  }

  /** @param request new meal data @returns created meal */
  createMeal(request: MealRequest): Observable<Meal> {
    return this.http.post<Meal>(this.apiUrl, request);
  }

  /** @param id meal identifier @param request replacement data @returns updated meal */
  updateMeal(id: number, request: MealRequest): Observable<Meal> {
    return this.http.put<Meal>(`${this.apiUrl}/${id}`, request);
  }

  /** @param id meal identifier @returns completion observable for deletion */
  deleteMeal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
