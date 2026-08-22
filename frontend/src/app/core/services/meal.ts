import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Meal } from '../models/meal';

export interface MealRequest {
  name: string;
  description: string;
  price: number;
  categoryId: number;
  available: boolean;
  imageUrl?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class MealService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/meals';

  getMeals(): Observable<Meal[]> {
    return this.http.get<Meal[]>(this.apiUrl);
  }

  createMeal(request: MealRequest): Observable<Meal> {
    return this.http.post<Meal>(this.apiUrl, request);
  }

  updateMeal(id: number, request: MealRequest): Observable<Meal> {
    return this.http.put<Meal>(`${this.apiUrl}/${id}`, request);
  }

  deleteMeal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
