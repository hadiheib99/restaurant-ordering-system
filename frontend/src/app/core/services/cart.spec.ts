import { TestBed } from '@angular/core/testing';

import { CartService } from './cart';
import { Meal } from '../models/meal';

describe('CartService', () => {
  let service: CartService;

  const meal: Meal = {
    id: 1,
    name: 'Margherita',
    description: 'Classic pizza',
    price: 40,
    categoryId: 1,
    categoryName: 'Pizza',
    available: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartService);
  });

  it('adds a meal and calculates count and total', () => {
    service.add(meal);

    expect(service.items().length).toBe(1);
    expect(service.count()).toBe(1);
    expect(service.total()).toBe(40);
  });

  it('increments an existing meal instead of duplicating it', () => {
    service.add(meal);
    service.add(meal);

    expect(service.items().length).toBe(1);
    expect(service.items()[0].quantity).toBe(2);
    expect(service.total()).toBe(80);
  });

  it('caps a meal at five units when adding or increasing', () => {
    for (let i = 0; i < 7; i += 1) service.add(meal);

    expect(service.items()[0].quantity).toBe(5);
    expect(service.count()).toBe(5);
    expect(service.total()).toBe(200);
    expect(service.isAtMax(meal.id)).toBe(true);

    service.increase(meal.id);
    expect(service.items()[0].quantity).toBe(5);
  });

  it('decreasing quantity to zero removes the item', () => {
    service.add(meal);
    service.decrease(meal.id);

    expect(service.items()).toEqual([]);
    expect(service.count()).toBe(0);
  });

  it('can increase, remove and clear cart items', () => {
    service.add(meal);
    service.increase(meal.id);
    expect(service.count()).toBe(2);

    service.remove(meal.id);
    expect(service.items()).toEqual([]);

    service.add(meal);
    service.clear();
    expect(service.total()).toBe(0);
  });
});
