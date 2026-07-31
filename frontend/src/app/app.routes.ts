import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(component => component.Login)
  },
  {
    path: 'menu',
    loadComponent: () =>
      import('./pages/menu/menu').then(component => component.Menu)
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
