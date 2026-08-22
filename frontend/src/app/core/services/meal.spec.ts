import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { MealService } from './meal';

describe('MealService', () => {
  let service: MealService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MealService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads meals', () => {
    service.getMeals().subscribe(meals => expect(meals.length).toBe(1));
    const request = http.expectOne('http://localhost:8080/api/meals');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, name: 'Pizza', description: '', price: 40, categoryId: 1, categoryName: 'Pizza', available: true }]);
  });

  it('creates, updates and deletes meals through the API', () => {
    const payload = { name: 'Pizza', description: 'Classic', price: 40, categoryId: 1, available: true };

    service.createMeal(payload).subscribe();
    const create = http.expectOne('http://localhost:8080/api/meals');
    expect(create.request.method).toBe('POST');
    create.flush({ id: 3, categoryName: 'Pizza', ...payload });

    service.updateMeal(3, payload).subscribe();
    const update = http.expectOne('http://localhost:8080/api/meals/3');
    expect(update.request.method).toBe('PUT');
    update.flush({ id: 3, categoryName: 'Pizza', ...payload });

    service.deleteMeal(3).subscribe();
    const remove = http.expectOne('http://localhost:8080/api/meals/3');
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
