import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login')
        .then(component => component.Login)
  },
  {
    path: 'menu',
    canActivate: [roleGuard],
    data: { roles: ['CUSTOMER'] },
    loadComponent: () =>
      import('./pages/menu/menu')
        .then(component => component.Menu)
  },
  {
    path: 'orders',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'WAITER', 'CHEF', 'CUSTOMER'] },
    loadComponent: () =>
      import('./pages/orders/orders')
        .then(component => component.Orders)
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./pages/admin/admin')
        .then(component => component.Admin)
  },
  {
    path: 'admin/meals',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./pages/admin-meals/admin-meals')
        .then(component => component.AdminMeals)
  },
  {
    path: 'admin/categories',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./pages/admin-categories/admin-categories')
        .then(component => component.AdminCategories)
  },
  {
    path: 'admin/users',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
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
