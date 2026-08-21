import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login')
        .then(component => component.Login)
  },

  {
    path: 'menu',
    loadComponent: () =>
      import('./pages/menu/menu')
        .then(component => component.Menu)
  },

  {
    path: 'orders',
    loadComponent: () =>
      import('./pages/orders/orders')
        .then(component => component.Orders)
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./pages/admin/admin')
        .then(component => component.Admin)
  },
  {
    path: 'admin/meals',
    loadComponent: () =>
      import('./pages/admin-meals/admin-meals')
        .then(component => component.AdminMeals)
  },
  {
    path: 'admin/categories',
    loadComponent: () =>
      import('./pages/admin-categories/admin-categories')
        .then(component => component.AdminCategories)
  },
  {
    path: 'admin/users',
    loadComponent: () =>
      import('./pages/admin-users/admin-users')
        .then(component => component.AdminUsers)
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];
