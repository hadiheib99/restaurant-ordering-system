import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminMeals } from './admin-meals';

describe('AdminMeals', () => {
  let component: AdminMeals;
  let fixture: ComponentFixture<AdminMeals>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMeals],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMeals);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
